package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.network.PaintingDataMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.io.Serial;
import java.util.UUID;

public final class ImmersivePaintingSpawnMessage extends PaintingDataMessage implements Message, Packet<ClientPlayPacketListener> {
    @Serial
    private static final long serialVersionUID = 0L;
    private final UUID uuid;
    private final BlockPos pos;
    private final Direction facing;

    public ImmersivePaintingSpawnMessage(ImmersivePaintingEntity entity) {
        super(entity);
        this.uuid = entity.getUuid();
        this.pos = entity.getDecorationBlockPos();
        this.facing = entity.getHorizontalFacing();
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handleImmersivePaintingSpawnMessage(this);
    }

    public UUID uuid() {
        return uuid;
    }

    public BlockPos pos() {
        return pos;
    }

    public Direction facing() {
        return facing;
    }

    @Override
    public void write(PacketByteBuf buf) {
        encode(buf);
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        receive(MinecraftClient.getInstance().player);
    }
}
