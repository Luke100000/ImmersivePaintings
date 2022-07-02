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

    private static double coolDown = 0.0;

    public static void sendServer(Message message) {
        serverQueue.add(new LazyPacket(message, null));
    }

    public static void sendClient(Message message, ServerPlayerEntity e) {
        clientQueue.add(new LazyPacket(message, e));
    }

    public static void tickClient() {
        coolDown = Math.max(coolDown - 1.0, 0.0);
        if (!serverQueue.isEmpty() && coolDown < 1.0) {
            LazyPacket packet = serverQueue.remove(0);
            NetworkHandler.sendToServer(packet.message);
            coolDown += 20.0 / Config.getInstance().maxPacketsPerSecond;
        }
    }

    public static void tickServer() {
        coolDown = Math.max(coolDown - 1.0, 0.0);
        if (!clientQueue.isEmpty() && coolDown < 1.0) {
            LazyPacket packet = clientQueue.remove(0);
            NetworkHandler.sendToPlayer(packet.message, packet.player);
            coolDown += 20.0 / Config.getInstance().maxPacketsPerSecond;
        }
    }

    public static float getRemainingTime() {
        return (float)serverQueue.size() / Config.getInstance().maxPacketsPerSecond;
    }

    record LazyPacket(Message message, ServerPlayerEntity player) {
    }
}
