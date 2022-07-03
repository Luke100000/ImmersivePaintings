package immersive_paintings.forge;

import immersive_paintings.*;
import immersive_paintings.forge.cobalt.network.NetworkHandlerImpl;
import immersive_paintings.forge.cobalt.registration.RegistrationImpl;
import immersive_paintings.resources.PaintingsLoader;
import immersive_paintings.resources.ServerPaintingManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod(Main.MOD_ID)
@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Bus.MOD)
public final class CommonForge {
    public CommonForge() {
        RegistrationImpl.bootstrap();
        new NetworkHandlerImpl();
    }

    @SubscribeEvent
    public static void onRegistryEvent(RegistryEvent<?> event) {
        Items.bootstrap();
        Entities.bootstrap();
        Messages.bootstrap();
    }

    @SubscribeEvent
    public static void onCreateEntityAttributes(EntityAttributeCreationEvent event) {
        RegistrationImpl.ENTITY_ATTRIBUTES.forEach((type, attributes) -> event.put(type, attributes.get().build()));
    }
}
