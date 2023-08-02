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

import java.util.function.Function;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    @Override
    public <T extends Message> void registerMessage(Class<T> msg, Function<PacketByteBuf, T> constructor) {
        Identifier identifier = new Identifier(Main.MOD_ID, msg.getName().toLowerCase());

        ServerPlayNetworking.registerGlobalReceiver(identifier, (server, player, handler, buffer, responder) -> {
            Message m = constructor.apply(buffer);
            server.execute(() -> m.receive(player));
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientProxy.register(identifier, constructor);
        }
    }

    @Override
    public void sendToServer(Message m) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        m.encode(buf);
        ClientPlayNetworking.send(new Identifier(Main.MOD_ID, m.getClass().getName().toLowerCase()), buf);
    }

    @Override
    public void sendToPlayer(Message m, ServerPlayerEntity e) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        m.encode(buf);
        ServerPlayNetworking.send(e, new Identifier(Main.MOD_ID, m.getClass().getName().toLowerCase()), buf);
    }

    // Fabric's APIs are not side-agnostic.
    // We punt this to a separate class file to keep it from being eager-loaded on a server environment.
    private static final class ClientProxy {
        private ClientProxy() {
            throw new RuntimeException("new ClientProxy()");
        }

        public static <T extends Message> void register(Identifier id, Function<PacketByteBuf, T> constructor) {
            ClientPlayNetworking.registerGlobalReceiver(id, (client, ignore1, buffer, ignore2) -> {
                Message m = constructor.apply(buffer);
                client.execute(() -> m.receive(client.player));
            });
        }
    }
}

