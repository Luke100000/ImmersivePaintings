package net.conczin.immersive_paintings.network.payload.c2s;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingSyncPayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingRegisterResponsePayload;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.conczin.immersive_paintings.registry.Configs;
import net.conczin.immersive_paintings.util.ImageManipulations;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.player.Player;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.Optional;

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

    private static void paintingRegisterError(Player player, String error, Identifier i) {
        NetworkHandler.sendToClient((ServerPlayer)player, new PaintingRegisterResponsePayload(Optional.ofNullable(i), error));
    }

    private static void addSettingsToHash(MessageDigest md5, Painting painting) {
        md5.update(("settings:" + painting.width() + ":" + painting.height() + ":" + painting.resolution() + ":" + painting.name() + ":").getBytes(StandardCharsets.UTF_8));
        for (Painting.Flag flag : Painting.Flag.values()) {
            if (painting.has(flag)) {
                md5.update((flag.name() + ":").getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    // Separate method to allow for Xerca compatibility
    public static Identifier handle(Player player, BufferedImage image, Painting painting) {
        try {
            byte[] encodedImage = ImageManipulations.encode(image);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(encodedImage);
            addSettingsToHash(md5, painting);
            String hash = String.format("%032x", new BigInteger(1, md5.digest()));
            painting = painting.withHash(hash);
            Identifier identifier = painting.location();

            MinecraftServer server = player.level().getServer();
            if (server != null) {
                ServerPaintingManager.registerPainting(server, identifier, painting, image);
                NetworkHandler.sendToAllClients(server, new PaintingSyncPayload(identifier, painting));
            }

            paintingRegisterError(player, "", identifier);
            return identifier;
        } catch (NoSuchAlgorithmException e) {
            Main.LOGGER.error("failed to hash painting {}", painting.location(), e);
            paintingRegisterError(player, "hash_failed", null);
        } catch (IOException e) {
            Main.LOGGER.error("failed to encode painting {}", painting.location(), e);
            paintingRegisterError(player, "hash_failed", null);
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

            if (!player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(Configs.COMMON.uploadPermissionLevel)))) {
                paintingRegisterError(player, "no_permission", null);
                return;
            }

            long count = ServerPaintingManager.getCustomPaintings(player.level().getServer()).values().stream().filter(p -> p.authorUUID().equals(player.getUUID())).count();
            if (count > Configs.COMMON.maxUserImages) {
                paintingRegisterError(player, "limit_reached", null);
                return;
            }

            Main.LOGGER.debug("{}, {}, {}, {}", image.getWidth(), Configs.COMMON.maxUserImageWidth, image.getHeight(), Configs.COMMON.maxUserImageHeight);
            if (image.getWidth() > Configs.COMMON.maxUserImageWidth || image.getHeight() > Configs.COMMON.maxUserImageHeight) {
                if (!Configs.COMMON.automaticImageResizing) {
                    paintingRegisterError(player, "too_large", null);
                    return;
                }

                float z = Math.min(
                        (float) Configs.COMMON.maxUserImageWidth / image.getWidth(),
                        (float) Configs.COMMON.maxUserImageHeight / image.getHeight()
                );

                BufferedImage newImage = new BufferedImage((int) (image.getWidth() * z), (int) (image.getHeight() * z), BufferedImage.TYPE_INT_ARGB);
                ImageManipulations.resize(newImage, image, 1 / z, 0, 0);
                image = newImage;
            }

            Painting p = new Painting(width, height, resolution, name, player.getGameProfile().name(), player.getUUID(), Painting.Type.PAINTING, flags, "");
            handle(player, image, p);
        });
    }

    @Override
    public Type<PaintingRegisterPayload> type() {
        return TYPE;
    }
}
