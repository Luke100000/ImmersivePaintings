package immersive_paintings.network;

import immersive_paintings.network.s2c.*;

public interface NetworkManager {
    void handleImmersivePaintingSpawnMessage(ImmersivePaintingSpawnMessage message);

    void handleOpenGuiRequest(OpenGuiRequest request);

    void handlePaintingListResponse(PaintingListMessage response);

    void handlePaintingModifyMessage(PaintingModifyMessage message);

    void handleRegisterPaintingResponse(RegisterPaintingResponse response);
}
