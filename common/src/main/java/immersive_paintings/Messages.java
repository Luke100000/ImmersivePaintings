package immersive_paintings;


import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.c2s.PaintingListRequest;
import immersive_paintings.network.s2c.ImmersivePaintingSpawnMessage;
import immersive_paintings.network.s2c.PaintingListResponse;

public class Messages {
    public static void bootstrap() {
        NetworkHandler.registerMessage(ImmersivePaintingSpawnMessage.class);
        NetworkHandler.registerMessage(PaintingListRequest.class);
        NetworkHandler.registerMessage(PaintingListResponse.class);
    }
}
