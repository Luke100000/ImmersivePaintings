package immersive_paintings.fabric;

import immersive_paintings.ClientMain;
import immersive_paintings.Renderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public final class ClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register((event) -> ClientMain.postLoad());

        Renderer.bootstrap();
    }
}
