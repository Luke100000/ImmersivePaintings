package immersive_paintings.forge;

import immersive_paintings.Main;
import immersive_paintings.network.LazyNetworkManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventBus {
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        LazyNetworkManager.tickServer();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        LazyNetworkManager.tickClient();
    }
}
