package immersive_paintings.forge;

import immersive_paintings.Main;
import immersive_paintings.Renderer;
import immersive_paintings.resources.FrameLoader;
import immersive_paintings.resources.ObjectLoader;
import immersive_paintings.forge.cobalt.registration.RegistrationImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public final class ClientForge {
    @SubscribeEvent
    public static void data(FMLConstructModEvent event) {
        ((ReloadableResourceManagerImpl) MinecraftClient.getInstance().getResourceManager()).registerReloader(new ObjectLoader());
        ((ReloadableResourceManagerImpl) MinecraftClient.getInstance().getResourceManager()).registerReloader(new FrameLoader());
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        RegistrationImpl.bootstrap();

        Renderer.bootstrap();
    }
}
