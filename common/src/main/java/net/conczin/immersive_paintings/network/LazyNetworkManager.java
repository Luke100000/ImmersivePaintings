package net.conczin.immersive_paintings.network;

import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.registry.Configs;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedList;
import java.util.List;

public class LazyNetworkManager {
    private static final List<LazyPacket> serverQueue = new LinkedList<>();
    private static final List<LazyPacket> clientQueue = new LinkedList<>();

    private static double cooldownClient = 0.0;
    private static double cooldownServer = 0.0;

    public static void sendToServer(ImmersivePayload payload) {
        serverQueue.add(new LazyPacket(payload, null));
    }

    public static void sendToClient(ImmersivePayload payload, ServerPlayer e) {
        clientQueue.add(new LazyPacket(payload, e));
    }

    public static void tickClient() {
        cooldownClient = Math.max(cooldownClient - 1.0, 0.0);
        while (!serverQueue.isEmpty() && cooldownClient < 1.0) {
            LazyPacket packet = serverQueue.removeFirst();
            NetworkHandler.Client.sendToServer(packet.payload());
            cooldownClient += 20.0 / Configs.COMMON.maxPacketsPerSecond;
        }
    }

    public static void tickServer() {
        cooldownServer = Math.max(cooldownServer - 1.0, 0.0);
        while (!clientQueue.isEmpty() && cooldownServer < 1.0) {
            LazyPacket packet = clientQueue.removeFirst();
            NetworkHandler.sendToClient(packet.player, packet.payload());
            cooldownServer += 20.0 / Configs.COMMON.maxPacketsPerSecond;
        }
    }

    public static float getRemainingTime() {
        return (float)serverQueue.size() / Configs.COMMON.maxPacketsPerSecond;
    }

    record LazyPacket(ImmersivePayload payload, ServerPlayer player) {}
}
