package net.conczin.immersive_paintings.painting;

import com.twelvemonkeys.image.ImageUtil;
import net.conczin.immersive_paintings.Config;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.client.gui.ImmersivePaintingScreen;
import net.conczin.immersive_paintings.network.Network;
import net.conczin.immersive_paintings.network.payload.c2s.ImageRequestPayload;
import net.conczin.immersive_paintings.painting.Painting.Size;
import net.conczin.immersive_paintings.util.ImageManipulations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

public class ClientPaintingManager {
    private static final Map<ResourceLocation, Painting> paintings = new HashMap<>();

    private static final Map<ResourceLocation, Map<Size, ResourceLocation>> textureMap = new HashMap<>();

    private static final Map<ResourceLocation, Boolean> requested = new HashMap<>();

    private static final ClientCache clientCache = new ClientCache();

    private static final String texturePrefix = "immersive_painting/";

    final static ExecutorService service = Executors.newFixedThreadPool(2);

    public static Future<?> runService(Runnable runnable) {
        return service.submit(runnable);
    }

    public static Map<ResourceLocation, Painting> getPaintings() {
        return paintings;
    }

    public static Painting getPainting(ResourceLocation identifier) {
        return paintings.getOrDefault(identifier, Painting.DEFAULT);
    }

    private static String paintingTextureIdentifier(ResourceLocation identifier, Size size) {
        if (size == Size.FULL) {
            return identifier.getPath();
        }
        return identifier.getPath() + "_" + size.name().toLowerCase();
    }

    public static ResourceLocation getImageIdentifier(ResourceLocation identifier, Size size) {
        if (!paintings.containsKey(identifier)) {
            return Painting.DEFAULT_IDENTIFIER;
        }

        // If there's no existing identifier map, first try to load it by going to the cache.
        // If cache does not contain the image, go to the server and return the default option
        if (!textureMap.containsKey(identifier)) {
            Optional<BufferedImage> image = clientCache.get(paintingTextureIdentifier(identifier, Size.FULL));
            if (image.isPresent()) {
                registerImage(identifier, image.get(), size == Size.NSFW);
            } else {
                if (!requested.containsKey(identifier)) {
                    requested.put(identifier, true);
                    Network.Client.sendToServer(new ImageRequestPayload(identifier));
                }
                return Painting.DEFAULT_IDENTIFIER;
            }
        }

        Map<Size, ResourceLocation> mapping = textureMap.get(identifier);
        Painting painting = paintings.get(identifier);

        if (!painting.nsfw() || Config.getInstance().showNSFWPaintings) {
            return mapping.get(size);
        }

        // Attempt to create NSFW images on the fly if it's needed
        if (mapping.containsKey(Size.NSFW)) {
            return mapping.get(Size.NSFW);
        }

        Optional<BufferedImage> image = clientCache.get(paintingTextureIdentifier(identifier, Size.FULL));
        if (image.isEmpty()) {
            Main.LOGGER.error("somehow identifier {} had no painting, which isn't possible", identifier);
            return Painting.DEFAULT_IDENTIFIER;
        }

        // Separate thread to avoid freezes when blurring image
        if (!requested.containsKey(identifier)) {
            requested.put(identifier, true);
            runService(() -> {
                registerImageType(identifier, image.get(), Size.NSFW, Size.NSFW, false);
                updateWidget(identifier);
                requested.remove(identifier);
            });
        }

        // Return the default, once the blurred image is registered it will automatically use it
        return Painting.DEFAULT_IDENTIFIER;
    }

    public static void registerPainting(ResourceLocation identifier, Painting painting) {
        paintings.put(identifier, painting);
    }

    public static void deregisterPainting(ResourceLocation identifier) {
        if (!paintings.containsKey(identifier))
            return;

        Map<Size, ResourceLocation> map = textureMap.remove(identifier);
        paintings.remove(identifier);

        // Fixes an issue where if a painting was deleted on the server and
        // the client attempts to delete the empty painting it would error
        if (map != null)
            map.forEach((size, id) -> clientCache.delete(paintingTextureIdentifier(identifier, size)));
    }

    private static void updateWidget(ResourceLocation identifier) {
        if (Minecraft.getInstance().screen instanceof ImmersivePaintingScreen screen) {
            screen.updateWidget(identifier);
        }
    }

    private static void registerImageType(ResourceLocation identifier, BufferedImage image, Size size, Size realSize, boolean alreadyCached) {
        if (!textureMap.containsKey(identifier))
            return;

        Map<Size, ResourceLocation> mapping = textureMap.get(identifier);

        // Register the type if it has a unique mapping
        if (mapping.containsKey(size)) {
            mapping.put(realSize, mapping.get(size));
            return;
        }

        // There is no scenario where the size here already exists in cache, so skip any cache checking
        int w = image.getWidth();
        int h = image.getHeight();
        switch (size) {
            case Size.THUMBNAIL -> {
                float zoom = Math.min(
                    (float)Config.getInstance().thumbnailSize / w,
                    (float)Config.getInstance().thumbnailSize / h
                );

                // The thumbnail would not be smaller than the actual painting
                if (zoom < 1.0f) {
                    w *= zoom;
                    h *= zoom;
                }
            }
            case Size.HALF ->  {
                w /= 2;
                h /= 2;
            }
            case Size.QUARTER  -> {
                w /= 4;
                h /= 4;
            }
            case Size.EIGHTH -> {
                w /= 8;
                h /= 8;
            }
            default -> {}
        }

        String path = paintingTextureIdentifier(identifier, realSize);
        BufferedImage target;

        Optional<BufferedImage> img = clientCache.get(path);
        if (img.isPresent()) {
            target = img.get();
            alreadyCached = true;
        } else {
            if (w == image.getWidth() && h == image.getHeight()) {
                if (realSize == Size.NSFW) {
                    double widthRatio = (double) Config.getInstance().thumbnailSize / 2 / w;
                    double heightRatio = (double) Config.getInstance().thumbnailSize / 2 / h;
                    double ratio = Math.min(widthRatio, heightRatio);

                    target = new BufferedImage((int)(w * ratio), (int)(h * ratio), BufferedImage.TYPE_INT_ARGB);
                    double zoom = (double)image.getWidth() / (w * ratio);

                    ImageManipulations.resize(target, image, zoom, 0, 0);
                    target = ImageUtil.blur(target, (float)zoom);
                } else {
                    target = image;
                }
            } else {
                target = new BufferedImage(w, h,BufferedImage.TYPE_INT_ARGB);
                ImageManipulations.resize(target, image, (double)image.getWidth() / w, 0, 0);
            }
        }

        if (!alreadyCached)
            clientCache.set(path, target);

        ResourceLocation id = Minecraft.getInstance().getTextureManager().register(texturePrefix + path, new DynamicTexture(ImageManipulations.bufferedToNative(target)));
        mapping.put(realSize, id);
    }

    // registers this textures and make it readable
    public static void registerImage(ResourceLocation identifier, BufferedImage image, boolean alreadyCached) {
        if (!paintings.containsKey(identifier)) {
            Main.LOGGER.error("no existing painting record for identifier {}", identifier);
            return;
        }

        if (textureMap.containsKey(identifier)) {
            return;
        }

        textureMap.put(identifier, new HashMap<>());

        Painting painting = paintings.get(identifier);

        registerImageType(identifier, image, Size.FULL, Size.FULL, alreadyCached);

        int res = Math.max(painting.width(), painting.height()) * painting.resolution();

        Size halfSize = res / 2 < Config.getInstance().lodResolutionMinimum ? Size.FULL : Size.HALF;
        registerImageType(identifier, image, halfSize, Size.HALF, alreadyCached);

        Size quarterSize = res / 4 < Config.getInstance().lodResolutionMinimum ? halfSize : Size.QUARTER;
        registerImageType(identifier, image, quarterSize, Size.QUARTER, alreadyCached);

        Size eighthSize = res / 8 < Config.getInstance().lodResolutionMinimum ? quarterSize : Size.EIGHTH;
        registerImageType(identifier, image, eighthSize, Size.EIGHTH, alreadyCached);

        Size thumbnailSize = res < Config.getInstance().thumbnailSize ? Size.FULL : Size.THUMBNAIL;
        registerImageType(identifier, image, thumbnailSize, Size.THUMBNAIL, alreadyCached);

        requested.remove(identifier);

        updateWidget(identifier);
    }

    private static class ClientCache extends Cache<String, BufferedImage> {
        @Override
        public String getCachePath(String key) {
            return key + ".png";
        }

        @Override
        public BufferedImage decode(byte[] bytes) {
            return ImageManipulations.decode(bytes);
        }

        @Override
        public byte[] encode(BufferedImage image) {
            return ImageManipulations.encode(image);
        }
    }
}
