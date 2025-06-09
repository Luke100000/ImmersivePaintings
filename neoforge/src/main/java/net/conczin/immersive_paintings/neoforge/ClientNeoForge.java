package net.conczin.immersive_paintings.neoforge;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.registration.Renderers;
import net.conczin.immersive_paintings.neoforge.compat.ClothConfig;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.resources.FrameLoader;
import net.conczin.immersive_paintings.resources.ObjectLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(value = Main.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientNeoForge {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (client, parent) -> ClothConfig.createConfigScreen(parent));
    }

    @SubscribeEvent
    public static void registerNetwork(final RegisterPayloadHandlersEvent event) {
        NetworkHandler.Client.registerSender(PacketDistributor::sendToServer);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        Renderers.register(event::registerEntityRenderer);
    }

    @SubscribeEvent
    public static void data(FMLConstructModEvent event) {
        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(new ObjectLoader());
        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(new FrameLoader());
    }
}
