package immersive_paintings.network;

import immersive_paintings.client.gui.ImmersivePaintingScreen;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.network.c2s.PaintingModifyRequest;
import immersive_paintings.network.s2c.*;
import immersive_paintings.resources.ClientPaintingManager;
import immersive_paintings.resources.Painting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.OffThreadException;
import net.minecraft.text.TranslatableText;
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
        if (request.gui == OpenGuiRequest.Type.EDITOR) {
            MinecraftClient.getInstance().setScreen(new ImmersivePaintingScreen(request.entity));
        }
    }

    @Override
    public void handlePaintingListResponse(PaintingListMessage response) {
        if (response.shouldClear()) {
            ClientPaintingManager.getPaintings().clear();
        }
        for (Map.Entry<Identifier, Painting> entry : response.getPaintings().entrySet()) {
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

    @Override
    public void handlePaintingModifyMessage(PaintingModifyMessage message) {
        ClientPlayerEntity e = MinecraftClient.getInstance().player;
        if (e != null && e.world.getEntityById(message.getEntityId()) instanceof ImmersivePaintingEntity painting) {
            painting.setMotive(message.getMotive());
            painting.setFrame(message.getFrame());
            painting.setMaterial(message.getMaterial());
        }
    }

    @Override
    public void handleRegisterPaintingResponse(RegisterPaintingResponse response) {
        if (MinecraftClient.getInstance().currentScreen instanceof ImmersivePaintingScreen screen) {
            if (response.error == null) {

                if (screen.entity != null) {
                    screen.entity.setMotive(new Identifier(response.identifier));
                    NetworkHandler.sendToServer(new PaintingModifyRequest(screen.entity));
                    screen.setPage(ImmersivePaintingScreen.Page.FRAME);
                }
            } else {
                screen.setPage(ImmersivePaintingScreen.Page.CREATE);
                screen.setError(new TranslatableText("immersive_paintings.error." + response.error));
            }
        }
    }
}
