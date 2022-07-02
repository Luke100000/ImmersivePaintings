package immersive_paintings.fabric;

import dev.architectury.event.events.client.ClientTickEvent;
import immersive_paintings.Entities;
import immersive_paintings.Items;
import immersive_paintings.Messages;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.fabric.cobalt.network.NetworkHandlerImpl;
import immersive_paintings.fabric.cobalt.registration.RegistrationImpl;
import immersive_paintings.fabric.resources.FabricPaintings;
import immersive_paintings.network.LazyNetworkManager;
import immersive_paintings.network.s2c.PaintingListMessage;
import immersive_paintings.resources.ServerPaintingManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public final class CommonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new RegistrationImpl();
        new NetworkHandlerImpl();

        Items.bootstrap();
        Entities.bootstrap();
        Messages.bootstrap();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new FabricPaintings());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                NetworkHandler.sendToPlayer(new PaintingListMessage(), handler.player)
        );

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> ServerPaintingManager.server = server);

        ServerTickEvents.START_SERVER_TICK.register((server) -> LazyNetworkManager.tickServer());
        ClientTickEvents.START_CLIENT_TICK.register((server) -> LazyNetworkManager.tickClient());
    }
}

