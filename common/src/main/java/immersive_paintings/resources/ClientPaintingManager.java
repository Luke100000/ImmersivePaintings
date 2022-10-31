package immersive_paintings.resources;

import immersive_paintings.client.ClientUtils;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.c2s.ImageRequest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

import static immersive_paintings.resources.Painting.DEFAULT;

public class ClientPaintingManager {
    static final Map<Identifier, Painting> paintings = new HashMap<>();

    public static Map<Identifier, Painting> getPaintings() {
        return paintings;
    }

    public static Painting getPainting(Identifier identifier) {
        return paintings.getOrDefault(identifier, DEFAULT);
    }

    public static Painting.Texture getPaintingTexture(Identifier identifier, Painting.Type type) {
        if (paintings.containsKey(identifier)) {
            Painting painting = paintings.get(identifier);
            Painting.Texture textureOriginal = painting.getTexture(type);
            Painting.Texture texture = painting.getTexture(textureOriginal.link);
            if (texture.image == null && !texture.requested) {
                texture.requested = true;

                Cache.get(texture).ifPresentOrElse((image) -> {
                            texture.image = image;
                            registerImage(texture);
                        },
                        () -> NetworkHandler.sendToServer(new ImageRequest(identifier, textureOriginal.link)));
            }

            //fall back to the highest resolution if image does not exist yet
            if (texture.image == null) {
                for (Painting.Type t : Painting.Type.values()) {
                    Painting.Texture temporaryTexture = painting.getTexture(t);
                    if (temporaryTexture.image != null) {
                        return temporaryTexture;
                    }
                }
            }

            return texture;
        } else {
            return DEFAULT.texture;
        }
    }

    // registers this textures and make it readable
    public static void registerImage(Painting.Texture texture) {
        NativeImage nativeImage = ClientUtils.byteImageToNativeImage(texture.image);
        texture.textureIdentifier = MinecraftClient.getInstance().getTextureManager()
                .registerDynamicTexture("immersive_painting/" + texture.hash, new NativeImageBackedTexture(nativeImage));
    }
}
