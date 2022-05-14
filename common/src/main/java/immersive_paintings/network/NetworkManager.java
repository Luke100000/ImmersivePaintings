package immersive_paintings.network;

import immersive_paintings.network.s2c.ImmersivePaintingSpawnMessage;
import immersive_paintings.network.s2c.OpenGuiRequest;
import immersive_paintings.network.s2c.PaintingListResponse;

public interface NetworkManager {
    void handleImmersivePaintingSpawnMessage(ImmersivePaintingSpawnMessage message);

    void handleOpenGuiRequest(OpenGuiRequest request);

    void handlePaintingListResponse(PaintingListResponse response);
}
