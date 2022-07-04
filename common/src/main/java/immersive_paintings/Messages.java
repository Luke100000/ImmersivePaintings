package immersive_paintings;


import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.c2s.*;
import immersive_paintings.network.s2c.*;

public class Messages {
    public static void bootstrap() {
        NetworkHandler.registerMessage(ImageRequest.class);
        NetworkHandler.registerMessage(PaintingListMessage.class);
        NetworkHandler.registerMessage(ImageResponse.class);
        NetworkHandler.registerMessage(PaintingModifyRequest.class);
        NetworkHandler.registerMessage(PaintingModifyMessage.class);
        NetworkHandler.registerMessage(RegisterPaintingRequest.class);
        NetworkHandler.registerMessage(PaintingDeleteRequest.class);
        NetworkHandler.registerMessage(UploadPaintingRequest.class);
        NetworkHandler.registerMessage(RegisterPaintingResponse.class);
        NetworkHandler.registerMessage(OpenGuiRequest.class);
    }
}
