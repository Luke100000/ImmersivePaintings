package immersive_paintings.forge.cobalt.network;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.cobalt.network.NetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.network.*;

import java.util.function.Function;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    private static final int PROTOCOL_VERSION = 1;

    private final SimpleChannel channel = ChannelBuilder.named(new Identifier(Main.SHORT_MOD_ID, "main"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .acceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .simpleChannel();

    private int id = 0;

    @Override
    public <T extends Message> void registerMessage(Class<T> msg, Function<PacketByteBuf, T> constructor) {
        this.channel.messageBuilder(msg, id++)
                .encoder(Message::encode)
                .decoder(constructor)
                .consumerNetworkThread((m, ctx) -> {
                    ctx.enqueueWork(() -> {
                        ServerPlayerEntity sender = ctx.getSender();
                        m.receive(sender);
                    });
                    ctx.setPacketHandled(true);
                })
                .add();
    }

    @Override
    public void sendToServer(Message m) {
        this.channel.send(m, PacketDistributor.SERVER.noArg());
    }

    @Override
    public void sendToPlayer(Message m, ServerPlayerEntity e) {
        this.channel.send(m, PacketDistributor.PLAYER.with(e));
    }
}
