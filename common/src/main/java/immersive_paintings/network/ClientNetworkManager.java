package immersive_paintings.network;

import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.network.s2c.ImmersivePaintingSpawnMessage;
import immersive_paintings.network.s2c.OpenGuiRequest;
import immersive_paintings.network.s2c.PaintingListResponse;
import immersive_paintings.resources.PaintingManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.OffThreadException;

public class ClientNetworkManager implements NetworkManager {
    @Override
    public void handleImmersivePaintingSpawnMessage(ImmersivePaintingSpawnMessage message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.isOnThread()) {
            client.executeSync(() -> {
                handleImmersivePaintingSpawnMessage(message);
            });
            throw OffThreadException.INSTANCE;
        } else {
            ClientWorld world = client.world;
            assert world != null;

            ImmersivePaintingEntity painting = new ImmersivePaintingEntity(world, message.pos(), message.facing());
            painting.setId(message.id());
            painting.setUuid(message.uuid());
            painting.setMotive(message.motive());

            world.addEntity(message.id(), painting);
        }
    }

    @Override
    public void handleOpenGuiRequest(OpenGuiRequest request) {

    }

    @Override
    public void handlePaintingListResponse(PaintingListResponse response) {
        PaintingManager.setClientPaintings(response.getPaintings());
    }
}
