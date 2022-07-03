package immersive_paintings.fabric;

import immersive_paintings.ClientMain;
import immersive_paintings.Renderer;
import immersive_paintings.fabric.resources.FabricFrameLoader;
import immersive_paintings.fabric.resources.FabricObjectLoader;
import immersive_paintings.network.LazyNetworkManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public final class ClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register((event) -> ClientMain.postLoad());

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new FabricObjectLoader());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new FabricFrameLoader());

        ClientTickEvents.START_CLIENT_TICK.register((server) -> LazyNetworkManager.tickClient());

        Renderer.bootstrap();
    }
}
