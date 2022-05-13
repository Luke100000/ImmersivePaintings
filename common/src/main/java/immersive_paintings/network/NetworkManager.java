package immersive_paintings.network;

import immersive_paintings.network.s2c.ImmersivePaintingSpawnMessage;
import immersive_paintings.network.s2c.OpenGuiRequest;

public interface NetworkManager {
    void handleImmersivePaintingSpawnMessage(ImmersivePaintingSpawnMessage message);

    void handleOpenGuiRequest(OpenGuiRequest request);
}
