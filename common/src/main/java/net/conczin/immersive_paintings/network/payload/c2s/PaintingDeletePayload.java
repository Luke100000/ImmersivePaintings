package net.conczin.immersive_paintings.network.payload.c2s;

import java.util.*;
import java.util.stream.Collectors;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.network.Network;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingListPayload;
import net.conczin.immersive_paintings.painting.Painting;
import net.conczin.immersive_paintings.painting.ServerPaintingManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public record PaintingDeletePayload(ResourceLocation identifier, boolean adminDelete) implements ImmersivePayload {
	public static final Type<PaintingDeletePayload> TYPE = new Type<>(Main.locate("painting_delete"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PaintingDeletePayload> STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, PaintingDeletePayload::identifier,
		ByteBufCodecs.BOOL, PaintingDeletePayload::adminDelete,
		PaintingDeletePayload::new
	);

    private static void deletePainting(MinecraftServer server, Player player, ResourceLocation painting) {
        ServerPaintingManager.deregisterPainting(server, painting);
        Main.LOGGER.info("Player {} deleted painting {}", player, painting);
    }

    @Override
    public void handle(Player player) {
        ResourceLocation identifier = identifier();
        Painting painting = ServerPaintingManager.getCustomPaintings(player.getServer()).get(identifier);
        UUID authorUUID = painting.authorUUID();

        if (!(authorUUID.equals(player.getUUID()) || player.hasPermissions(4))) {
            Main.LOGGER.warn("Player {} tried to delete painting {}, which they do not own", player, identifier);
            return;
        }

        MinecraftServer server = player.getServer();

        if (adminDelete()) {
            Map<ResourceLocation, Optional<Painting>> deletedPaintings = ServerPaintingManager.getCustomPaintings(server)
                    .entrySet().stream()
                    .filter(p -> p.getValue().authorUUID().equals(authorUUID) && !p.getValue().isDatapack())
                    .collect(Collectors.toMap(Map.Entry::getKey, (e) -> Optional.empty()));

            deletedPaintings.forEach((id, o) -> deletePainting(server, player, id));

            // Even though it's probably not necessary, split the delete packets just in case
            List<PaintingListPayload> payloads = PaintingListPayload.splitPaintings(deletedPaintings, false);

            server.getPlayerList().getPlayers().forEach(plr -> {
                payloads.forEach(payload -> Network.sendToClient(plr, payload));
            });
        } else {
            deletePainting(server, player, identifier);

            PaintingListPayload payload = new PaintingListPayload(identifier, null);
            server.getPlayerList().getPlayers().forEach(plr -> Network.sendToClient(plr, payload));
        }
    }

	@Override
	public Type<PaintingDeletePayload> type() {
		return TYPE;
	}
}
