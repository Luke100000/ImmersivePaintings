package net.conczin.immersive_paintings;

import net.conczin.immersive_paintings.client.gui.ImmersivePaintingScreen;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.c2s.ImageRequestPayload;
import net.conczin.immersive_paintings.Painting.Size;
import net.conczin.immersive_paintings.registration.Configs;
import net.conczin.immersive_paintings.util.Cache;
import net.conczin.immersive_paintings.util.ImageManipulations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

public class ClientPaintingManager {
    private static final Map<ResourceLocation, Painting> paintings = Collections.synchronizedMap(new HashMap<>());

    private static final Map<ResourceLocation, Map<Size, ResourceLocation>> textureMap = Collections.synchronizedMap(new HashMap<>());

    private static final Map<String, Boolean> requested = Collections.synchronizedMap(new HashMap<>());

    private static final ClientCache paintingCache = new ClientCache();

    private final static ExecutorService service = Executors.newFixedThreadPool(2);

    public static Map<ResourceLocation, Painting> getPaintings() {
        return paintings;
    }

    public static Optional<Painting> getPainting(ResourceLocation identifier) {
        return Optional.ofNullable(paintings.get(identifier));
    }

    private static String textureIdentifier(ResourceLocation identifier, Size size) {
        if (size == Size.FULL)
            return identifier.getPath();
        return identifier.getPath() + "_" + size.name().toLowerCase();
    }

    private static void setImageRequest(ResourceLocation identifier, boolean thumbnail, boolean delete) {
        String id = textureIdentifier(identifier, thumbnail ? Size.THUMBNAIL : Size.FULL);
        if (requested.containsKey(id) == delete) {
            if (delete) {
                requested.remove(id);
            } else {
                requested.put(id, true);
                NetworkHandler.Client.sendToServer(new ImageRequestPayload(identifier, thumbnail));
            }
        }
    }

    private static ResourceLocation getOrNSFW(Map<Size, ResourceLocation> mapping, ResourceLocation identifier, Size size) {
        if (Configs.CLIENT.showNSFWPaintings)
            return mapping.get(size);

        Painting p = paintings.get(identifier);
        if (!p.has(Painting.Flag.NSFW))
            return mapping.get(size);

        if (mapping.containsKey(Size.NSFW))
            return mapping.get(Size.NSFW);

        // If it's not loaded we need to scale from a thumbnail specifically
        if (!mapping.containsKey(Size.THUMBNAIL)) {
            setImageRequest(identifier, true, false);
            return Painting.DEFAULT_IDENTIFIER;
        }

        Optional<BufferedImage> image = paintingCache.get(textureIdentifier(identifier, Size.THUMBNAIL));
        image.ifPresent(bufferedImage -> registerImageType(identifier, bufferedImage, Size.NSFW, Size.NSFW, false));
        return Painting.DEFAULT_IDENTIFIER;
    }

    public static ResourceLocation getImageIdentifier(ResourceLocation identifier, Size size) {
        if (!paintings.containsKey(identifier))
            return Painting.DEFAULT_IDENTIFIER;

        Map<Size, ResourceLocation> mapping = textureMap.get(identifier);

        if (mapping == null) {
            // Attempt to get the thumbnail first so there's at least something displayed
            setImageRequest(identifier, true, false);

            if (size != Size.THUMBNAIL)
                setImageRequest(identifier, false, false);

            return Painting.DEFAULT_IDENTIFIER;
        } else if (!mapping.containsKey(size)) {
            boolean isThumbnail = (size == Size.THUMBNAIL);

            setImageRequest(identifier, isThumbnail, false);

            // Request the full image and attempt to temporarily use a thumbnail identifier until it's ready
            if (!isThumbnail && mapping.containsKey(Size.THUMBNAIL)) {
                return getOrNSFW(mapping, identifier, Size.THUMBNAIL);
            }

            return Painting.DEFAULT_IDENTIFIER;
        }

        return getOrNSFW(mapping, identifier, size);
    }

    public static void registerPainting(ResourceLocation identifier, Painting painting) {
        String fullId = textureIdentifier(identifier, Size.FULL);
        String thumbId = textureIdentifier(identifier, Size.THUMBNAIL);

        // Set that they are being processed so we don't try to reach out to the network at the same time
        requested.put(fullId, true);
        requested.put(thumbId, true);

        paintings.put(identifier, painting);

        // When registering a painting we need to check the user's existing cache
        // Doing this allows us to quickly load and resize any necessary cached images
        // The only two sizes that can exist on their own are FULL and THUMBNAIL, all others are generated from them
        paintingCache.get(thumbId).ifPresentOrElse(
            bufferedImage -> registerThumbnail(identifier, bufferedImage, true),
            () -> requested.remove(thumbId)
        );

        paintingCache.get(fullId).ifPresentOrElse(
            bufferedImage -> registerImage(identifier, bufferedImage, true),
            () -> requested.remove(fullId)
        );
    }

    public static void deregisterPainting(ResourceLocation identifier) {
        if (!paintings.containsKey(identifier))
            return;

        paintings.remove(identifier);

        Map<Size, ResourceLocation> map = textureMap.remove(identifier);
        if (map != null) {
            TextureManager manager = Minecraft.getInstance().getTextureManager();
            map.forEach((size, id) -> {
                paintingCache.delete(textureIdentifier(identifier, size));
                manager.release(id);
            });
        }
    }

    private static void registerImageType(ResourceLocation identifier, BufferedImage fullImage, Size size, Size realSize, final boolean alreadyCached) {
        textureMap.putIfAbsent(identifier, new HashMap<>());
        Map<Size, ResourceLocation> mapping = textureMap.get(identifier);

        // Handle cases where an image is too small, and realSize == Size.HALF/QUARTER/EIGHTH but size == Size.FULL
        if (mapping.containsKey(size) && size != realSize) {
            mapping.put(realSize, mapping.get(size));
            return;
        }

        service.submit(() -> {
            BufferedImage target;

            String path = textureIdentifier(identifier, realSize);
            Optional<BufferedImage> img = paintingCache.get(path);
            if (img.isPresent()) {
                target = img.get();
            } else {
                if (realSize == Size.THUMBNAIL) {
                    target = fullImage;
                } else {
                    target = ImageManipulations.resizeImage(fullImage, size);
                }

                if (!alreadyCached)
                    paintingCache.set(path, target);
            }

            ResourceLocation name = Main.locate(path);
            Minecraft.getInstance().getTextureManager().register(name, new DynamicTexture(name::toString, ImageManipulations.bufferedToNative(target)));
            mapping.put(realSize, name);

            if (size == Size.THUMBNAIL && Minecraft.getInstance().screen instanceof ImmersivePaintingScreen screen)
                screen.updateWidget(identifier);
        });
    }

    public static void registerThumbnail(ResourceLocation identifier, BufferedImage image, boolean alreadyCached) {
        registerImageType(identifier, image, Size.THUMBNAIL, Size.THUMBNAIL, alreadyCached);
        setImageRequest(identifier, true, true);
    }

    // TODO: for datapacks, use FULL for all sizes that aren't thumbnail
    // registers this textures and make it readable
    public static void registerImage(ResourceLocation identifier, BufferedImage image, boolean alreadyCached) {
        if (!paintings.containsKey(identifier) && !alreadyCached) {
            Main.LOGGER.error("no existing painting record for identifier {}", identifier);
            return;
        }

        if (textureMap.containsKey(identifier))
            // Check needs to account for a thumbnail loading before the full-sized image
            if (textureMap.get(identifier).containsKey(Size.FULL))
                return;

        Painting painting = paintings.get(identifier);

        int res = Math.max(painting.width(), painting.height()) * painting.resolution();
        registerImageType(identifier, image, Size.FULL, Size.FULL, alreadyCached);

        Size halfSize = res / 2 < Configs.CLIENT.lodResolutionMinimum ? Size.FULL : Size.HALF;
        registerImageType(identifier, image, halfSize, Size.HALF, alreadyCached);

        Size quarterSize = res / 4 < Configs.CLIENT.lodResolutionMinimum ? halfSize : Size.QUARTER;
        registerImageType(identifier, image, quarterSize, Size.QUARTER, alreadyCached);

        Size eighthSize = res / 8 < Configs.CLIENT.lodResolutionMinimum ? quarterSize : Size.EIGHTH;
        registerImageType(identifier, image, eighthSize, Size.EIGHTH, alreadyCached);

        setImageRequest(identifier, false, true);
    }

    private static class ClientCache extends Cache<String, BufferedImage> {
        @Override
        public String getCachePath(String key) {
            return key + ".png";
        }

        @Override
        public BufferedImage decode(byte[] bytes) {
            try {
                return ImageManipulations.decode(bytes);
            } catch (IOException e) {
                Main.LOGGER.error("could not read image from client cache", e);
            }
            return null;
        }

        @Override
        public byte[] encode(BufferedImage image) {
            try {
                return ImageManipulations.encode(image);
            } catch (IOException e) {
                Main.LOGGER.error("could not write image to client cache", e);
            }
            return null;
        }
    }
}
