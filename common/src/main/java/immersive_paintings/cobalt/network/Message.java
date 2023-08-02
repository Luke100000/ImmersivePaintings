package immersive_paintings.cobalt.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

public abstract class Message {
    protected Message() {

    }

    public abstract void encode(PacketByteBuf b);

    public abstract void receive(PlayerEntity e);
}
