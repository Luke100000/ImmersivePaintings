package net.conczin.immersive_paintings.network.payload.c2s;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingSyncPayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingRegisterErrorPayload;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.conczin.immersive_paintings.registration.Configs;
import net.conczin.immersive_paintings.util.ImageManipulations;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.awt.image.BufferedImage;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;

public record PaintingRegisterPayload(int width, int height, int resolution, String name, EnumSet<Painting.Flag> flags) implements ImmersivePayload {
    public static final Type<PaintingRegisterPayload> TYPE = new Type<>(Main.locate("painting_register"));

    public static final StreamCodec<FriendlyByteBuf, PaintingRegisterPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PaintingRegisterPayload::width,
            ByteBufCodecs.INT, PaintingRegisterPayload::height,
            ByteBufCodecs.INT, PaintingRegisterPayload::resolution,
            ByteBufCodecs.STRING_UTF8, PaintingRegisterPayload::name,
            Painting.Flag.STREAM_CODEC, PaintingRegisterPayload::flags,
            PaintingRegisterPayload::new
    );

    private static void paintingRegisterError(Player player, String error, ResourceLocation i) {
        NetworkHandler.sendToClient((ServerPlayer)player, new PaintingRegisterErrorPayload(i, error));
    }

    // Separate method to allow for Xerca compatibility
    public static ResourceLocation handle(Player player, BufferedImage image, Painting painting) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String hash = String.format("%032x", new BigInteger(1, md5.digest(ImageManipulations.encode(image))));
            painting = painting.withHash(hash);
            ResourceLocation identifier = painting.location();

            ServerPaintingManager.registerPainting(player.getServer(), identifier, painting, image);
            NetworkHandler.sendToAllClients(player.getServer(), new PaintingSyncPayload(identifier, painting));
            paintingRegisterError(player, "", identifier);
            return identifier;
        } catch (NoSuchAlgorithmException e) {
            Main.LOGGER.error("failed to hash painting {}", painting.location(), e);
            paintingRegisterError(player, "hash_failed", painting.location());
        }

        return null;
    }

    @Override
    public void handle(Player player, Runner runner) {
        int width = width();
        int height = height();
        int resolution = resolution();
        String name = name();
        EnumSet<Painting.Flag> flags = flags();

        runner.run(() -> {
            BufferedImage image = ImageUploadPayload.uploaded.remove(player.getStringUUID());

            if (!player.hasPermissions(Configs.COMMON.uploadPermissionLevel)) {
                paintingRegisterError(player, "no_permission", null);
                return;
            }

            if (image.getWidth() > Configs.COMMON.maxUserImageWidth || image.getHeight() > Configs.COMMON.maxUserImageHeight) {
                paintingRegisterError(player, "too_large", null);
                return;
            }

            long count = ServerPaintingManager.getCustomPaintings(player.getServer()).values().stream().filter(p -> p.authorUUID().equals(player.getUUID())).count();
            if (count > Configs.COMMON.maxUserImages) {
                paintingRegisterError(player, "limit_reached", null);
                return;
            }

            Painting p = new Painting(width, height, resolution, name, player.getGameProfile().getName(), player.getUUID(), Painting.Type.PAINTING, flags, "");
            handle(player, image, p);
        });
    }

    @Override
    public Type<PaintingRegisterPayload> type() {
        return TYPE;
    }
}
