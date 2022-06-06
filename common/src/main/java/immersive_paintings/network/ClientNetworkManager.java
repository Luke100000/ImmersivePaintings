package immersive_paintings.network;

import immersive_paintings.client.gui.ImmersivePaintingScreen;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.network.s2c.ImmersivePaintingSpawnMessage;
import immersive_paintings.network.s2c.OpenGuiRequest;
import immersive_paintings.network.s2c.PaintingListMessage;
import immersive_paintings.resources.ClientPaintingManager;
import immersive_paintings.resources.Paintings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.OffThreadException;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ClientNetworkManager implements NetworkManager {
    @Override
    public void handleImmersivePaintingSpawnMessage(ImmersivePaintingSpawnMessage message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.isOnThread()) {
            client.executeSync(() -> handleImmersivePaintingSpawnMessage(message));
            throw OffThreadException.INSTANCE;
        } else {
            ClientWorld world = client.world;
            assert world != null;

            ImmersivePaintingEntity painting = new ImmersivePaintingEntity(world, message.pos(), message.facing());
            painting.setId(message.getEntityId());
            painting.setUuid(message.uuid());
            painting.setMotive(message.getMotive());
            painting.setFrame(message.getFrame());
            painting.setMaterial(message.getMaterial());

            world.addEntity(message.getEntityId(), painting);
        }
    }

    @Override
    public void handleOpenGuiRequest(OpenGuiRequest request) {

    }

    @Override
    public void handlePaintingListResponse(PaintingListMessage response) {
        if (response.shouldClear()) {
            ClientPaintingManager.getPaintings().clear();
        }
        for (Map.Entry<Identifier, Paintings.PaintingData> entry : response.getPaintings().entrySet()) {
            if (entry.getValue() == null) {
                ClientPaintingManager.getPaintings().remove(entry.getKey());
            } else {
                ClientPaintingManager.getPaintings().put(entry.getKey(), entry.getValue());
            }
        }

        if (MinecraftClient.getInstance().currentScreen instanceof ImmersivePaintingScreen screen) {
            screen.refreshPage();
        }
    }
}
