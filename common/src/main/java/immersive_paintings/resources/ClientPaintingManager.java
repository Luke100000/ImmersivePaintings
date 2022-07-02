package immersive_paintings.resources;

import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.c2s.ImageRequest;
import immersive_paintings.util.ImageManipulations;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ClientPaintingManager {
    static final Painting DEFAULT = new Painting(new NativeImage(2, 2, false), 2);

    static Map<Identifier, Painting> paintings = new HashMap<>();

    public static Map<Identifier, Painting> getPaintings() {
        return paintings;
    }

    public static Painting getPainting(Identifier identifier) {
        return paintings.getOrDefault(identifier, DEFAULT);
    }

    public static Painting getPainting(Identifier identifier, Painting.Type type) {
        if (paintings.containsKey(identifier)) {
            Painting painting = paintings.get(identifier);
            Painting.Texture texture = painting.getTexture(type);
            if (texture.image == null && !texture.requested) {
                texture.requested = true;

                Cache.get(texture)
                        .ifPresentOrElse((image) -> loadImage(texture, image),
                                () -> NetworkHandler.sendToServer(new ImageRequest(identifier, type)));
            }
            return painting;
        } else {
            return DEFAULT;
        }
    }

    public static void loadImage(Identifier i, Painting.Type type, int[] ints, int width, int height) {
        Painting painting = ClientPaintingManager.getPaintings().get(i);
        Painting.Texture texture = painting.getTexture(type);
        NativeImage image = ImageManipulations.intsToImage(width, height, ints);
        loadImage(texture, image);
        Cache.set(texture);
    }

    public static void loadImage(Painting.Texture texture, @NotNull NativeImage image) {
        texture.image = image;
        texture.textureIdentifier = MinecraftClient.getInstance().getTextureManager()
                .registerDynamicTexture("immersive_painting/" + texture.hash, new NativeImageBackedTexture(texture.image));
        texture.image.upload(0, 0, 0, false);
    }
}
