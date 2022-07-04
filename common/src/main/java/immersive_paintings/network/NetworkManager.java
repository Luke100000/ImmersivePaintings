package immersive_paintings.network;

import immersive_paintings.network.s2c.OpenGuiRequest;
import immersive_paintings.network.s2c.PaintingListMessage;
import immersive_paintings.network.s2c.PaintingModifyMessage;
import immersive_paintings.network.s2c.RegisterPaintingResponse;

public interface NetworkManager {
    void handleOpenGuiRequest(OpenGuiRequest request);

    void handlePaintingListResponse(PaintingListMessage response);

    void handlePaintingModifyMessage(PaintingModifyMessage message);

    void handleRegisterPaintingResponse(RegisterPaintingResponse response);
}
