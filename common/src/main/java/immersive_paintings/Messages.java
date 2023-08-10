package immersive_paintings;


import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.c2s.*;
import immersive_paintings.network.s2c.*;

public class Messages {
    public static void bootstrap() {

    }

    static {
        NetworkHandler.registerMessage(ImageRequest.class, ImageRequest::new);
        NetworkHandler.registerMessage(PaintingListMessage.class, PaintingListMessage::new);
        NetworkHandler.registerMessage(ImageResponse.class, ImageResponse::new);
        NetworkHandler.registerMessage(PaintingModifyRequest.class, PaintingModifyRequest::new);
        NetworkHandler.registerMessage(PaintingModifyMessage.class, PaintingModifyMessage::new);
        NetworkHandler.registerMessage(RegisterPaintingRequest.class, RegisterPaintingRequest::new);
        NetworkHandler.registerMessage(PaintingDeleteRequest.class, PaintingDeleteRequest::new);
        NetworkHandler.registerMessage(UploadPaintingRequest.class, UploadPaintingRequest::new);
        NetworkHandler.registerMessage(RegisterPaintingResponse.class, RegisterPaintingResponse::new);
        NetworkHandler.registerMessage(OpenGuiRequest.class, OpenGuiRequest::new);
    }
}
