package net.conczin.immersive_paintings.network.payload.c2s;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingSyncPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public record PaintingFlagPayload(ResourceLocation identifier, Painting.Flag flag, boolean value) implements ImmersivePayload {
    public static final Type<PaintingFlagPayload> TYPE = new Type<>(Main.locate("painting_flag"));
    public static final StreamCodec<FriendlyByteBuf, PaintingFlagPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, PaintingFlagPayload::identifier,
            ByteBufCodecs.idMapper(id -> Painting.Flag.values()[id], Painting.Flag::ordinal), PaintingFlagPayload::flag,
            ByteBufCodecs.BOOL, PaintingFlagPayload::value,
            PaintingFlagPayload::new
    );

    @Override
    public void handle(Player player, Runner runner) {
        runner.run(() -> {
            MinecraftServer server = player.getServer();
            if (server == null || flag != Painting.Flag.HIDDEN && flag != Painting.Flag.NSFW) return;

            Painting painting = ServerPaintingManager.getCustomPaintings(server).get(identifier);
            if (painting == null || !(painting.authorUUID().equals(player.getUUID()) || player.hasPermissions(4))) return;

            EnumSet<Painting.Flag> flags = EnumSet.copyOf(painting.flags());
            if (value) {
                flags.add(flag);
            } else {
                flags.remove(flag);
            }

            Painting updated = new Painting(painting.version(), painting.width(), painting.height(), painting.resolution(),
                    painting.name(), painting.author(), painting.authorUUID(), painting.type(), flags, painting.hash());
            ServerPaintingManager.updatePainting(server, identifier, updated);
            NetworkHandler.sendToAllClients(server, new PaintingSyncPayload(identifier, updated));
        });
    }

    @Override
    public Type<PaintingFlagPayload> type() {
        return TYPE;
    }
}
