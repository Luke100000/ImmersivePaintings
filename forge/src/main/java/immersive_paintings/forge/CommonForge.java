package immersive_paintings.forge;

import immersive_paintings.Entities;
import immersive_paintings.Items;
import immersive_paintings.Main;
import immersive_paintings.Messages;
import immersive_paintings.forge.cobalt.network.NetworkHandlerImpl;
import immersive_paintings.forge.cobalt.registration.RegistrationImpl;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod(Main.MOD_ID)
@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Bus.MOD)
public final class CommonForge {
    public CommonForge() {
        new RegistrationImpl();
        new NetworkHandlerImpl();

        Items.bootstrap();
        Entities.bootstrap();
        Messages.loadMessages();

        RegistrationImpl.bootstrap();
    }

    @SubscribeEvent
    public static void onCreateEntityAttributes(EntityAttributeCreationEvent event) {
        RegistrationImpl.ENTITY_ATTRIBUTES.forEach((type, attributes) -> event.put(type, attributes.get().build()));
    }
}
