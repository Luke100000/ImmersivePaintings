package immersive_paintings.resources;

import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.c2s.ImageRequest;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

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
                data.textureIdentifier = new Identifier("");
                NetworkHandler.sendToServer(new ImageRequest(identifier));
            }
            return data;
        } else {
            return DEFAULT;
        }
    }
}
