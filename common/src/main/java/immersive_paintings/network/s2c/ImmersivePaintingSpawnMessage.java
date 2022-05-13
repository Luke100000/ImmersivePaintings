package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.io.Serial;
import java.util.Objects;
import java.util.UUID;

public final class ImmersivePaintingSpawnMessage implements Message, Packet<ClientPlayPacketListener> {
    @Serial
    private static final long serialVersionUID = 0L;
    private final int id;
    private final UUID uuid;
    private final BlockPos pos;
    private final Direction facing;
    private final Identifier motive;

    public ImmersivePaintingSpawnMessage(ImmersivePaintingEntity entity) {
        this.id = entity.getId();
        this.uuid = entity.getUuid();
        this.pos = entity.getDecorationBlockPos();
        this.facing = entity.getHorizontalFacing();
        this.motive = new Identifier("");
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handleImmersivePaintingSpawnMessage(this);
    }

    public int id() {
        return id;
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

    public Identifier motive() {
        return motive;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ImmersivePaintingSpawnMessage)obj;
        return this.id == that.id &&
                Objects.equals(this.uuid, that.uuid) &&
                Objects.equals(this.pos, that.pos) &&
                Objects.equals(this.facing, that.facing) &&
                Objects.equals(this.motive, that.motive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid, pos, facing, motive);
    }

    @Override
    public String toString() {
        return "ImmersivePaintingSpawnMessage[" +
                "id=" + id + ", " +
                "uuid=" + uuid + ", " +
                "pos=" + pos + ", " +
                "facing=" + facing + ", " +
                "motive=" + motive + ']';
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
