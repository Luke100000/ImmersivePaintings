package net.conczin.immersive_paintings.fabric;

import net.conczin.immersive_paintings.client.render.Renderers;
import net.conczin.immersive_paintings.fabric.resources.FabricFrameLoader;
import net.conczin.immersive_paintings.fabric.resources.FabricObjectLoader;
import net.conczin.immersive_paintings.network.LazyNetworkManager;
import net.conczin.immersive_paintings.network.Network;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public final class ClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Network.Client.registerSender(ClientPlayNetworking::send);
        Renderers.register(EntityRendererRegistry::register);

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new FabricObjectLoader());
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new FabricFrameLoader());

        ClientTickEvents.START_CLIENT_TICK.register((client) -> LazyNetworkManager.tickClient());
    }
}
