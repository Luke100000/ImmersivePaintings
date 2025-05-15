package net.conczin.immersive_paintings.neoforge;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.network.LazyNetworkManager;
import net.conczin.immersive_paintings.painting.PlayerManager;
import net.conczin.immersive_paintings.resources.PaintingsLoader;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Main.MOD_ID, bus = Bus.GAME)
public class EventBus {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        LazyNetworkManager.tickServer();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LazyNetworkManager.tickClient();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.getEntity().level().isClientSide) {
            PlayerManager.playerLoggedOff((ServerPlayer)event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new PaintingsLoader());
    }
}
