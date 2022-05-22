package immersive_paintings.network;

import immersive_paintings.network.s2c.ImmersivePaintingSpawnMessage;
import immersive_paintings.network.s2c.OpenGuiRequest;
import immersive_paintings.network.s2c.PaintingListMessage;

public interface NetworkManager {
    void handleImmersivePaintingSpawnMessage(ImmersivePaintingSpawnMessage message);

    void handleOpenGuiRequest(OpenGuiRequest request);

    void handlePaintingListResponse(PaintingListMessage response);
}
