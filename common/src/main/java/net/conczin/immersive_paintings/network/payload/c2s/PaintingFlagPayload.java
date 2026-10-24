package net.conczin.immersive_paintings.network.payload.c2s;

import net.conczin.immersive_paintings.ImmersivePaintings;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingSyncPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public record PaintingFlagPayload(Identifier identifier, Painting.Flag flag, boolean value) implements ImmersivePayload {
    public static final Type<PaintingFlagPayload> TYPE = new Type<>(ImmersivePaintings.locate("painting_flag"));
    public static final StreamCodec<FriendlyByteBuf, PaintingFlagPayload> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, PaintingFlagPayload::identifier,
            ByteBufCodecs.idMapper(id -> Painting.Flag.values()[id], Painting.Flag::ordinal), PaintingFlagPayload::flag,
            ByteBufCodecs.BOOL, PaintingFlagPayload::value,
            PaintingFlagPayload::new
    );

    @Override
    public void handle(Player player, Runner runner) {
        runner.run(() -> {
            MinecraftServer server = player.level().getServer();
            if (server == null || flag != Painting.Flag.HIDDEN && flag != Painting.Flag.NSFW) return;

            Painting painting = ServerPaintingManager.getCustomPaintings(server).get(identifier);
            if (painting == null || !(painting.authorUUID().equals(player.getUUID()) || player.permissions().hasPermission(Permissions.COMMANDS_OWNER))) return;

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
