package immersive_paintings.forge;

import immersive_paintings.*;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.forge.cobalt.network.NetworkHandlerImpl;
import immersive_paintings.forge.cobalt.registration.RegistrationImpl;
import immersive_paintings.network.s2c.PaintingListResponse;
import immersive_paintings.resources.PaintingManager;
import immersive_paintings.resources.Paintings;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
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
    private void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new Paintings());
    }

    @SubscribeEvent
    public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().world.isClient) {
            NetworkHandler.sendToPlayer(new PaintingListResponse(PaintingManager.getServerPaintings()), (ServerPlayerEntity)event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onCreateEntityAttributes(EntityAttributeCreationEvent event) {
        RegistrationImpl.ENTITY_ATTRIBUTES.forEach((type, attributes) -> event.put(type, attributes.get().build()));
    }
    public static boolean firstLoad = true;

    @SubscribeEvent
    public static void onClientStart(TickEvent.ClientTickEvent event) {
        //forge decided to be funny and won't trigger the client load event
        if (firstLoad) {
            ClientMain.postLoad();
            firstLoad = false;
        }
    }
}
