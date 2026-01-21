package net.conczin.immersive_paintings.neoforge;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.network.LazyNetworkManager;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.conczin.immersive_paintings.registry.Command;
import net.conczin.immersive_paintings.resources.PaintingsLoader;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Main.MOD_ID)
public class EventBus {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        Command.registerCommands(event.getDispatcher()::register);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        LazyNetworkManager.tickServer();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LazyNetworkManager.tickClient();
    }

    @SubscribeEvent
    public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            ServerPaintingManager.playerLoggedIn((ServerPlayer)event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(Main.locate("resources"), new PaintingsLoader());
    }
}
