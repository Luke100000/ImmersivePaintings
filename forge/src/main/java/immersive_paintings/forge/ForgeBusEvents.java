package immersive_paintings.forge;

import immersive_paintings.ClientMain;
import immersive_paintings.Main;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Main.MOD_ID)
public class ForgeBusEvents {
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
