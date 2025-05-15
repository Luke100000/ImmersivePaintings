package net.conczin.immersive_paintings.network;

import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.c2s.*;
import net.conczin.immersive_paintings.network.payload.s2c.ImageResponsePayload;
import net.conczin.immersive_paintings.network.payload.s2c.OpenGuiPayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingListPayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingRegisterErrorPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class Network {
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

    public static void register(Registrar c) {
        c.register(ImageRequestPayload.TYPE, ImageRequestPayload.STREAM_CODEC, true);
        c.register(ImageUploadPayload.TYPE, ImageUploadPayload.STREAM_CODEC, true);
        c.register(PaintingRegisterPayload.TYPE, PaintingRegisterPayload.STREAM_CODEC, true);
        c.register(PaintingEditPayload.TYPE, PaintingEditPayload.STREAM_CODEC, true);
        c.register(PaintingDeletePayload.TYPE, PaintingDeletePayload.STREAM_CODEC, true);

        c.register(ImageResponsePayload.TYPE, ImageResponsePayload.STREAM_CODEC, false);
        c.register(OpenGuiPayload.TYPE, OpenGuiPayload.STREAM_CODEC, false);
        c.register(PaintingListPayload.TYPE, PaintingListPayload.STREAM_CODEC, false);
        c.register(PaintingRegisterErrorPayload.TYPE, PaintingRegisterErrorPayload.STREAM_CODEC, false);
    }

    public interface Registrar {
        <T extends ImmersivePayload> void register(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, boolean isServer);
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
