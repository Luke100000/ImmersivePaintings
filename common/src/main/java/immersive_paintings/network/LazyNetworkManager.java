package immersive_paintings.network;

import immersive_paintings.Config;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.cobalt.network.NetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.LinkedList;
import java.util.List;

public class LazyNetworkManager {
    private static final List<LazyPacket> serverQueue = new LinkedList<>();
    private static final List<LazyPacket> clientQueue = new LinkedList<>();

    private static double cooldownClient = 0.0;
    private static double cooldownServer = 0.0;

    public static void sendToServer(Message message) {
        serverQueue.add(new LazyPacket(message, null));
    }

    public static void sendToClient(Message message, ServerPlayerEntity e) {
        clientQueue.add(new LazyPacket(message, e));
    }

    public static void tickClient() {
        cooldownClient = Math.max(cooldownClient - 1.0, 0.0);
        if (!serverQueue.isEmpty() && cooldownClient < 1.0) {
            LazyPacket packet = serverQueue.remove(0);
            NetworkHandler.sendToServer(packet.message);
            cooldownClient += 20.0 / Config.getInstance().maxPacketsPerSecond;
        }
    }

    public static void tickServer() {
        cooldownServer = Math.max(cooldownServer - 1.0, 0.0);
        if (!clientQueue.isEmpty() && cooldownServer < 1.0) {
            LazyPacket packet = clientQueue.remove(0);
            NetworkHandler.sendToPlayer(packet.message, packet.player);
            cooldownServer += 20.0 / Config.getInstance().maxPacketsPerSecond;
        }
    }

    public static float getRemainingTime() {
        return (float)serverQueue.size() / Config.getInstance().maxPacketsPerSecond;
    }

    record LazyPacket(Message message, ServerPlayerEntity player) {
    }
}
