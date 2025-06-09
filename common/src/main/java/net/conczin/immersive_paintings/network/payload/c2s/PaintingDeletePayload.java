package net.conczin.immersive_paintings.network.payload.c2s;

import java.util.*;
import java.util.stream.Collectors;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingSyncPayload;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public record PaintingDeletePayload(ResourceLocation identifier, boolean adminDelete) implements ImmersivePayload {
	public static final Type<PaintingDeletePayload> TYPE = new Type<>(Main.locate("painting_delete"));
	public static final StreamCodec<FriendlyByteBuf, PaintingDeletePayload> STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, PaintingDeletePayload::identifier,
		ByteBufCodecs.BOOL, PaintingDeletePayload::adminDelete,
		PaintingDeletePayload::new
	);

    private static void deletePainting(MinecraftServer server, Player player, ResourceLocation painting) {
        ServerPaintingManager.deregisterPainting(server, painting);
        Main.LOGGER.info("Player {} deleted painting {}", player, painting);
    }

    @Override
    public void handle(Player player, Runner runner) {
        ResourceLocation identifier = identifier();
        boolean adminDelete = adminDelete();

        runner.run(() -> {
            Painting painting = ServerPaintingManager.getCustomPaintings(player.getServer()).get(identifier);
            UUID authorUUID = painting.authorUUID();

            if (!(authorUUID.equals(player.getUUID()) || player.hasPermissions(4))) {
                Main.LOGGER.warn("Player {} tried to delete painting {}, which they do not own", player, identifier);
                return;
            }

            MinecraftServer server = player.getServer();

            PaintingSyncPayload payload;
            if (adminDelete) {
                Map<ResourceLocation, Optional<Painting>> deletedPaintings = ServerPaintingManager.getCustomPaintings(server)
                        .entrySet().stream()
                        .filter(p -> p.getValue().authorUUID().equals(authorUUID) && !p.getValue().is(Painting.Type.DATAPACK))
                        .collect(Collectors.toMap(Map.Entry::getKey, (e) -> Optional.empty()));

                deletedPaintings.forEach((id, o) -> deletePainting(server, player, id));

                // All deleted paintings are 1 byte (Optional.empty()) in the payload, so a player would need to have millions
                // of paintings deleted in order to go over the max packet size, which is realistically impossible
                payload = new PaintingSyncPayload(deletedPaintings, false);
            } else {
                deletePainting(server, player, identifier);
                payload = new PaintingSyncPayload(identifier, null);
            }

            NetworkHandler.sendToAllClients(server, payload);
        });
    }

	@Override
	public Type<PaintingDeletePayload> type() {
		return TYPE;
	}
}
