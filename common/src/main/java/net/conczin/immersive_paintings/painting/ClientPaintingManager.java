package net.conczin.immersive_paintings.painting;

import net.conczin.immersive_paintings.Config;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.client.gui.ImmersivePaintingScreen;
import net.conczin.immersive_paintings.network.Network;
import net.conczin.immersive_paintings.network.payload.c2s.ImageRequestPayload;
import net.conczin.immersive_paintings.painting.Painting.Size;
import net.conczin.immersive_paintings.util.ByteImage;
import net.conczin.immersive_paintings.util.ImageManipulations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientPaintingManager {
    private static final Map<ResourceLocation, Painting> paintings = new HashMap<>();

    private static final Map<ResourceLocation, Map<Size, ResourceLocation>> textureMap = new HashMap<>();

    private static final Map<ResourceLocation, Boolean> requested = new HashMap<>();

    private static final ClientCache clientCache = new ClientCache();

    private static final String texturePrefix = "immersive_painting/";

    public static Map<ResourceLocation, Painting> getPaintings() {
        return paintings;
    }

    public static Painting getPainting(ResourceLocation identifier) {
        return paintings.getOrDefault(identifier, Painting.DEFAULT);
    }

    private static String paintingIdentifier(ResourceLocation identifier, Size size) {
        if (size == Size.FULL) {
            return identifier.getPath();
        }
        return identifier.getPath() + "_" + size.name().toLowerCase();
    }

    public static ResourceLocation getImageIdentifier(ResourceLocation identifier, Size size) {
        // Since we register all types for a single texture at the same time in registerImage
        // this can be simplified by not having to do a check for the type
        if (textureMap.containsKey(identifier))
            return textureMap.get(identifier).get(size);

        if (paintings.containsKey(identifier)) {
            // Before going to the server for the texture, first check if it's cached locally
            Painting painting = paintings.get(identifier);
            Optional<ByteImage> image = clientCache.get(paintingIdentifier(identifier, Size.FULL));
            if (image.isPresent()) {
                registerImage(identifier, image.get());
                return textureMap.get(identifier).get(size);
            } else {
                // If we can't find the image locally then get it from the server
                if (!requested.containsKey(identifier)) {
                    requested.put(identifier, true);
                    Network.Client.sendToServer(new ImageRequestPayload(identifier));
                }
            }
        }

        return Painting.DEFAULT_IDENTIFIER;
    }

    public static ByteImage getThumbnail(ResourceLocation identifier) {
        ResourceLocation id = getImageIdentifier(identifier, Size.THUMBNAIL);

        // We assume the painting is still being registered so return the default until then
        if (id.equals(Painting.DEFAULT_IDENTIFIER)) {
            return Painting.DEFAULT_IMAGE;
        }

        // In theory this should always return something that exists, but to be safe we check regardless
        Optional<ByteImage> optionalImage = clientCache.get(paintingIdentifier(identifier, Size.THUMBNAIL));
        return optionalImage.orElse(Painting.DEFAULT_IMAGE);
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
            map.forEach((size, id) -> clientCache.delete(paintingIdentifier(identifier, size)));
    }

    private static void registerImageType(Map<Size, ResourceLocation> mapping, ByteImage image, Size size, Size realSize, String path) {
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

        ByteImage target;
        if (w == image.getWidth() && h == image.getHeight()) {
            target = image;
        } else {
            target = new ByteImage(w, h);
            ImageManipulations.resize(target, image, (double)image.getWidth() / w, 0, 0);
        }

        clientCache.set(path, target);

        ResourceLocation id = Minecraft.getInstance().getTextureManager().register(texturePrefix + path, new DynamicTexture(target.toNativeImage()));
        mapping.put(realSize, id);
    }

    // registers this textures and make it readable
    public static void registerImage(ResourceLocation identifier, ByteImage image) {
        if (!paintings.containsKey(identifier)) {
            Main.LOGGER.error("no existing painting record for identifier {}", identifier);
            return;
        }

        if (textureMap.containsKey(identifier)) {
            return;
        }

        Painting painting = paintings.get(identifier);
        Map<Size, ResourceLocation> map = new HashMap<>();

        registerImageType(map, image, Size.FULL, Size.FULL, paintingIdentifier(identifier, Size.FULL));

        int res = Math.max(painting.width(), painting.height()) * painting.resolution();

        Size halfSize = res / 2 < Config.getInstance().lodResolutionMinimum ? Size.FULL : Size.HALF;
        registerImageType(map, image, halfSize, Size.HALF, paintingIdentifier(identifier, Size.HALF));

        Size quarterSize = res / 4 < Config.getInstance().lodResolutionMinimum ? halfSize : Size.QUARTER;
        registerImageType(map, image, quarterSize, Size.QUARTER, paintingIdentifier(identifier, Size.QUARTER));

        Size eighthSize = res / 8 < Config.getInstance().lodResolutionMinimum ? quarterSize : Size.EIGHTH;
        registerImageType(map, image, eighthSize, Size.EIGHTH, paintingIdentifier(identifier, Size.EIGHTH));

        Size thumbnailSize = res < Config.getInstance().thumbnailSize ? Size.FULL : Size.THUMBNAIL;
        registerImageType(map, image, thumbnailSize, Size.THUMBNAIL, paintingIdentifier(identifier, Size.THUMBNAIL));

        textureMap.put(identifier, map);
        requested.remove(identifier);

        if (Minecraft.getInstance().screen instanceof ImmersivePaintingScreen screen) {
            screen.updateWidget(identifier);
        }
    }

    private static class ClientCache extends Cache<String, ByteImage> {
        @Override
        public String getCachePath(String key) {
            return key + ".png";
        }

        @Override
        public ByteImage decode(byte[] bytes) throws IOException {
            return ByteImage.read(bytes);
        }

        @Override
        public byte[] encode(ByteImage image) {
            return image.encode();
        }
    }
}
