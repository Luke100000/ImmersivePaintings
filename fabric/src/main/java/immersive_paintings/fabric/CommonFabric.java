package immersive_paintings.fabric;

import immersive_paintings.*;
import immersive_paintings.fabric.cobalt.network.NetworkHandlerImpl;
import immersive_paintings.fabric.cobalt.registration.RegistrationImpl;
import immersive_paintings.fabric.resources.FabricPaintings;
import immersive_paintings.network.LazyNetworkManager;
import immersive_paintings.resources.ServerPaintingManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
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

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                ServerDataManager.playerLoggedOff(handler.player)
        );

        ServerLifecycleEvents.SERVER_STARTING.register((server) -> ServerPaintingManager.server = server);

        ServerTickEvents.START_SERVER_TICK.register((server) -> LazyNetworkManager.tickServer());

        ItemGroups.PAINTINGS = FabricItemGroup.builder(ItemGroups.getIdentifier())
                .displayName(ItemGroups.getDisplayName())
                .icon(ItemGroups::getIcon)
                .entries((enabledFeatures, entries) -> entries.addAll(Items.items.stream().map(i -> i.get().getDefaultStack()).toList()))
                .build();
    }
}

