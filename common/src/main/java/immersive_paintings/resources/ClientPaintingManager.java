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
    static final Paintings.PaintingData DEFAULT = new Paintings.PaintingData(new NativeImage(2, 2, false), 2);

    static Map<Identifier, Paintings.PaintingData> paintings = new HashMap<>();

    public static Map<Identifier, Paintings.PaintingData> getPaintings() {
        return paintings;
    }

    public static Paintings.PaintingData getPainting(Identifier identifier) {
        if (paintings.containsKey(identifier)) {
            Paintings.PaintingData data = paintings.get(identifier);
            if (data.image == null && !data.requested) {
                data.requested = true;

                Cache.get(data)
                        .ifPresentOrElse((image) -> loadImage(identifier, image),
                                () -> NetworkHandler.sendToServer(new ImageRequest(identifier)));
            }
            return data;
        } else {
            return DEFAULT;
        }
    }

    public static void loadImage(Identifier i, int[] ints) {
        Paintings.PaintingData data = ClientPaintingManager.getPaintings().get(i);
        NativeImage image = ImageManipulations.intsToImage(data.getPixelWidth(), data.getPixelHeight(), ints);
        loadImage(i, image);
        Cache.set(data);
    }

    public static void loadImage(Identifier i, @NotNull NativeImage image) {
        Paintings.PaintingData data = ClientPaintingManager.getPaintings().get(i);
        data.image = image;
        data.textureIdentifier = MinecraftClient.getInstance().getTextureManager()
                .registerDynamicTexture("immersive_painting/" + i.getPath().replace(":", "_"), new NativeImageBackedTexture(data.image));
        data.image.upload(0, 0, 0, false);
    }
}
