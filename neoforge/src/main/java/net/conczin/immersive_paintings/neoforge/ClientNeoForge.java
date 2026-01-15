package net.conczin.immersive_paintings.neoforge;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.registration.Renderers;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.resources.FrameLoader;
import net.conczin.immersive_paintings.resources.ObjectLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(value = Main.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public final class ClientNeoForge {
    @SubscribeEvent
    public static void registerNetwork(final RegisterPayloadHandlersEvent event) {
        NetworkHandler.Client.registerSender(ClientPacketDistributor::sendToServer);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        Renderers.register(event::registerEntityRenderer);
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(Main.locate("resource_reloader_object"), new ObjectLoader());
        event.addListener(Main.locate("resource_reloader_frame"), new FrameLoader());
    }
}
