package net.conczin.immersive_paintings.neoforge;

import net.conczin.immersive_paintings.ImmersivePaintings;
import net.conczin.immersive_paintings.network.LazyNetworkManager;
import net.conczin.immersive_paintings.registry.Renderer;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.resources.FrameLoader;
import net.conczin.immersive_paintings.resources.ObjectLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@Mod(value = ImmersivePaintings.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ImmersivePaintings.MOD_ID, value = Dist.CLIENT)
public final class ClientNeoForge {
    @SubscribeEvent
    public static void registerNetwork(final RegisterClientPayloadHandlersEvent event) {
        NetworkHandler.Client.registerSender(ClientPacketDistributor::sendToServer);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        Renderer.register(event::registerEntityRenderer);
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(ObjectLoader.ID, new ObjectLoader());
        event.addListener(FrameLoader.ID, new FrameLoader());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LazyNetworkManager.tickClient();
    }
}
