package net.conczin.immersive_paintings.fabric;

import net.conczin.immersive_paintings.registry.Renderer;
import net.conczin.immersive_paintings.network.LazyNetworkManager;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.resources.FrameLoader;
import net.conczin.immersive_paintings.resources.ObjectLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.server.packs.PackType;

public final class ClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NetworkHandler.Client.registerSender(ClientPlayNetworking::send);
        Renderer.register(EntityRenderers::register);

        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(ObjectLoader.ID, new ObjectLoader());
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(FrameLoader.ID, new FrameLoader());

        ClientTickEvents.START_CLIENT_TICK.register(_ -> LazyNetworkManager.tickClient());
    }
}
