package immersive_paintings.forge;

import immersive_paintings.*;
import immersive_paintings.forge.cobalt.network.NetworkHandlerImpl;
import immersive_paintings.forge.cobalt.registration.RegistrationImpl;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.RegisterEvent;

@Mod(Main.MOD_ID)
@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Bus.MOD)
public final class CommonForge {
    public CommonForge() {
        RegistrationImpl.bootstrap();
        new NetworkHandlerImpl();
    }

    @SubscribeEvent
    public static void onRegistryEvent(RegisterEvent event) {
        Items.bootstrap();
        Entities.bootstrap();
        Messages.bootstrap();
    }

    @SubscribeEvent
    public static void register(CreativeModeTabEvent.Register event) {
        ItemGroups.PAINTINGS = event.registerCreativeModeTab(ItemGroups.getIdentifier(), builder -> builder
                .displayName(ItemGroups.getDisplayName())
                .icon(ItemGroups::getIcon)
                .entries((featureFlags, output, hasOp) -> output.addAll(Items.items.stream().map(i -> i.get().getDefaultStack()).toList()))
        );
    }
}
