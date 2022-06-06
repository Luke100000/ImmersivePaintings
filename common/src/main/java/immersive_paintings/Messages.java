package immersive_paintings;


import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.c2s.PaintingDeleteRequest;
import immersive_paintings.network.c2s.PaintingModifyRequest;
import immersive_paintings.network.c2s.ImageRequest;
import immersive_paintings.network.c2s.RegisterPaintingRequest;
import immersive_paintings.network.s2c.ImmersivePaintingSpawnMessage;
import immersive_paintings.network.s2c.PaintingListMessage;
import immersive_paintings.network.s2c.PaintingModifyMessage;
import immersive_paintings.network.s2c.ImageResponse;

public class Messages {
    public static void bootstrap() {
        NetworkHandler.registerMessage(ImmersivePaintingSpawnMessage.class);
        NetworkHandler.registerMessage(ImageRequest.class);
        NetworkHandler.registerMessage(PaintingListMessage.class);
        NetworkHandler.registerMessage(ImageResponse.class);
        NetworkHandler.registerMessage(PaintingModifyRequest.class);
        NetworkHandler.registerMessage(PaintingModifyMessage.class);
        NetworkHandler.registerMessage(RegisterPaintingRequest.class);
        NetworkHandler.registerMessage(PaintingDeleteRequest.class);
    }
}
