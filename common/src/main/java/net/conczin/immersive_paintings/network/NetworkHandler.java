package net.conczin.immersive_paintings.network;

import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class NetworkHandler {
    private static Sender sender;

    public static void registerSender(Sender s) {
        sender = s;
    }

    public static void sendToClient(ServerPlayer player, ImmersivePayload payload) {
        sender.send(player, payload);
    }

    public static void sendToAllClients(MinecraftServer server, ImmersivePayload payload) {
        server.getPlayerList().getPlayers().forEach(p -> sendToClient(p, payload));
    }

    public interface Sender {
        void send(ServerPlayer player, ImmersivePayload payload);
    }

    public static class Client {
        private static Sender sender;

        public static void registerSender(Sender s) {
            sender = s;
        }

        public static void sendToServer(ImmersivePayload payload) {
            sender.send(payload);
        }

        public interface Sender {
            void send(ImmersivePayload payload);
        }
    }
}
