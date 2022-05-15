package immersive_paintings;


import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.c2s.PaintingModifyRequest;
import immersive_paintings.network.c2s.PaintingRequest;
import immersive_paintings.network.s2c.ImmersivePaintingSpawnMessage;
import immersive_paintings.network.s2c.PaintingListResponse;
import immersive_paintings.network.s2c.PaintingModifyMessage;
import immersive_paintings.network.s2c.PaintingResponse;

public class Messages {
    public static void bootstrap() {
        NetworkHandler.registerMessage(ImmersivePaintingSpawnMessage.class);
        NetworkHandler.registerMessage(PaintingRequest.class);
        NetworkHandler.registerMessage(PaintingListResponse.class);
        NetworkHandler.registerMessage(PaintingResponse.class);
        NetworkHandler.registerMessage(PaintingModifyRequest.class);
        NetworkHandler.registerMessage(PaintingModifyMessage.class);
    }
}
