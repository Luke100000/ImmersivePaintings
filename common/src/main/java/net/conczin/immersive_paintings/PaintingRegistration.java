package net.conczin.immersive_paintings;

import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingSyncPayload;
import net.conczin.immersive_paintings.registry.Config;
import net.conczin.immersive_paintings.util.ImageManipulations;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;

public class PaintingRegistration {
    private static void addSettingsToHash(MessageDigest md5, Painting painting) {
        md5.update(("settings:" + painting.width() + ":" + painting.height() + ":" + painting.resolution() + ":" + painting.name() + ":").getBytes(StandardCharsets.UTF_8));
        for (Painting.Flag flag : Painting.Flag.values()) {
            if (painting.has(flag)) {
                md5.update((flag.name() + ":").getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    @Nullable
    public static Identifier register(ServerPlayer player, @Nullable BufferedImage image, Painting painting, Consumer<String> onError) {
        int width = painting.width();
        int height = painting.height();
        int resolution = painting.resolution();
        String name = painting.name();

        if (!player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(Config.COMMON.uploadPermissionLevel)))) {
            onError.accept("no_permission");
            return null;
        }

        if (image == null) {
            onError.accept("image_load_failed");
            return null;
        }

        long count = ServerPaintingManager.getCustomPaintings(player.level().getServer()).values().stream().filter(p -> p.authorUUID().equals(player.getUUID())).count();
        if (count >= Config.COMMON.maxUserImages) {
            onError.accept("limit_reached");
            return null;
        }

        if (width < 1 || width > 16 || height < 1 || height > 16 ||
            resolution < Config.COMMON.minPaintingResolution || resolution > Config.COMMON.maxPaintingResolution || name.length() > 256) {
            onError.accept("image_load_failed");
            return null;
        }

        ImmersivePaintings.LOGGER.debug("{}, {}, {}, {}", image.getWidth(), Config.COMMON.maxUserImageWidth, image.getHeight(), Config.COMMON.maxUserImageHeight);
        if (image.getWidth() > Config.COMMON.maxUserImageWidth || image.getHeight() > Config.COMMON.maxUserImageHeight) {
            if (!Config.COMMON.automaticImageResizing) {
                onError.accept("too_large");
                return null;
            }

            float z = Math.min(
                    (float) Config.COMMON.maxUserImageWidth / image.getWidth(),
                    (float) Config.COMMON.maxUserImageHeight / image.getHeight()
            );

            BufferedImage newImage = new BufferedImage(Math.max(1, (int) (image.getWidth() * z)), Math.max(1, (int) (image.getHeight() * z)), BufferedImage.TYPE_INT_ARGB);
            ImageManipulations.resize(newImage, image, 1 / z, 0, 0);
            image = newImage;
        }

        try {
            byte[] encodedImage = ImageManipulations.encode(image);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(encodedImage);
            addSettingsToHash(md5, painting);
            String hash = String.format("%032x", new BigInteger(1, md5.digest()));
            painting = painting.withHash(hash);
            Identifier identifier = painting.location();

            MinecraftServer server = player.level().getServer();
            ServerPaintingManager.registerPainting(server, identifier, painting, image);
            NetworkHandler.sendToAllClients(server, new PaintingSyncPayload(identifier, painting));
            return identifier;
        } catch (NoSuchAlgorithmException e) {
            ImmersivePaintings.LOGGER.error("failed to hash painting {}", painting.location(), e);
            onError.accept("hash_failed");
        } catch (IOException e) {
            ImmersivePaintings.LOGGER.error("failed to encode painting {}", painting.location(), e);
            onError.accept("hash_failed");
        }

        return null;
    }
}
