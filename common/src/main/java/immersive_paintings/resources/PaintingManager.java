package immersive_paintings.resources;

import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.c2s.PaintingRequest;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PaintingManager {
    private static final Paintings.PaintingData DEFAULT = new Paintings.PaintingData(new NativeImage(2, 2, false), 1, 1, 2);
    private static Map<Identifier, Paintings.PaintingData> serverPaintings = new HashMap<>();
    private static Map<Identifier, Paintings.PaintingData> clientPaintings = new HashMap<>();

    public static Map<Identifier, Paintings.PaintingData> getServerPaintings() {
        return serverPaintings;
    }

    public static void setClientPaintings(Map<Identifier, Paintings.PaintingData> paintings) {
        PaintingManager.clientPaintings = paintings;
    }

    public static Map<Identifier, Paintings.PaintingData> getClientPaintings() {
        return clientPaintings;
    }

    public static void setServerPaintings(Map<Identifier, Paintings.PaintingData> paintings) {
        PaintingManager.serverPaintings = paintings;
    }

    public static Paintings.PaintingData getPainting(Identifier identifier) {
        if (clientPaintings.containsKey(identifier)) {
            Paintings.PaintingData data = clientPaintings.get(identifier);
            if (data.image == null && !data.requested) {
                data.requested = true;
                data.textureIdentifier = new Identifier("");
                NetworkHandler.sendToServer(new PaintingRequest(identifier));
            }
            return data;
        } else {
            return DEFAULT;
        }
    }
}
