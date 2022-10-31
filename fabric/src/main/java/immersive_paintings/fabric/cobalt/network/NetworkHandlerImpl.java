package immersive_paintings.fabric.cobalt.network;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.cobalt.network.NetworkHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    private final Map<Message, Identifier> cache = new HashMap<>();

    private Identifier getMessageIdentifier(Message msg) {
        return cache.computeIfAbsent(msg, m -> getMessageIdentifier(m.getClass()));
    }

    private <T> Identifier getMessageIdentifier(Class<T> msg) {
        return new Identifier(Main.MOD_ID, msg.getSimpleName().toLowerCase(Locale.ROOT));
    }

    @Override
    public <T extends Message> void registerMessage(Class<T> msg) {
        Identifier id = getMessageIdentifier(msg);

        ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buffer, responder) -> {
            Message m = Message.decode(buffer);
            server.execute(() -> m.receive(player));
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientProxy.register(id);
        }
    }

    @Override
    public void sendToServer(Message msg) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        msg.encode(buf);
        ClientPlayNetworking.send(getMessageIdentifier(msg), buf);
    }

    @Override
    public void sendToPlayer(Message msg, ServerPlayerEntity e) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        msg.encode(buf);
        ServerPlayNetworking.send(e, getMessageIdentifier(msg), buf);
    }

    // Fabric's APIs are not side-agnostic.
    // We punt this to a separate class file to keep it from being eager-loaded on a server environment.
    private static final class ClientProxy {
        private ClientProxy() {
            throw new RuntimeException("new ClientProxy()");
        }

        public static void register(Identifier id) {
            ClientPlayNetworking.registerGlobalReceiver(id, (client, ignore1, buffer, ignore2) -> {
                Message m = Message.decode(buffer);
                client.execute(() -> m.receive(client.player));
            });
        }
    }
}
