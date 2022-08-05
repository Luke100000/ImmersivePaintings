package immersive_paintings.forge;

import immersive_paintings.ClientMain;
import immersive_paintings.Main;
import immersive_paintings.ServerDataManager;
import immersive_paintings.network.LazyNetworkManager;
import immersive_paintings.resources.PaintingsLoader;
import immersive_paintings.resources.ServerPaintingManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventBus {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        LazyNetworkManager.tickServer();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        LazyNetworkManager.tickClient();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.getEntity().world.isClient) {
            ServerDataManager.playerLoggedOff((ServerPlayerEntity)event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        ServerPaintingManager.server = event.getServer();
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

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new PaintingsLoader());
    }
}
