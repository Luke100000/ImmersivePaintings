package net.conczin.immersive_paintings.network.payload.c2s;

import net.conczin.immersive_paintings.ImmersivePaintings;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingRegisterResponsePayload;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.PaintingRegistration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.Optional;

public record PaintingRegisterPayload(int width, int height, int resolution, String name, EnumSet<Painting.Flag> flags) implements ImmersivePayload {
    public static final Type<PaintingRegisterPayload> TYPE = new Type<>(ImmersivePaintings.locate("painting_register"));

    public static final StreamCodec<FriendlyByteBuf, PaintingRegisterPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PaintingRegisterPayload::width,
            ByteBufCodecs.INT, PaintingRegisterPayload::height,
            ByteBufCodecs.INT, PaintingRegisterPayload::resolution,
            ByteBufCodecs.STRING_UTF8, PaintingRegisterPayload::name,
            Painting.Flag.STREAM_CODEC, PaintingRegisterPayload::flags,
            PaintingRegisterPayload::new
    );

    private static void paintingRegisterError(ServerPlayer player, String error, Identifier i) {
        NetworkHandler.sendToClient(player, new PaintingRegisterResponsePayload(Optional.ofNullable(i), error));
    }

    @Override
    public void handle(Player player, Runner runner) {
        int width = width();
        int height = height();
        int resolution = resolution();
        String name = name();
        EnumSet<Painting.Flag> flags = flags();

        runner.run(() -> {
            if (player instanceof ServerPlayer serverPlayer) {
                BufferedImage image = ImageUploadPayload.uploaded.remove(player.getStringUUID());
                Painting p = new Painting(width, height, resolution, name, player.getGameProfile().name(), player.getUUID(), Painting.Type.PAINTING, flags, "");
                Identifier identifier = PaintingRegistration.register(serverPlayer, image, p, error -> paintingRegisterError(serverPlayer, error, null));
                if (identifier != null) {
                    paintingRegisterError(serverPlayer, "", identifier);
                }
            }
        });
    }

    @Override
    public Type<PaintingRegisterPayload> type() {
        return TYPE;
    }
}
