package immersive_paintings.network;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class PaintingDataMessage extends Message {
    final String motive;
    final String frame;
    final String material;
    final int entityId;
    private final Direction facing;
    private final int rotation;
    private final int x, y, z;

    public PaintingDataMessage(ImmersivePaintingEntity painting) {
        entityId = painting.getId();
        this.motive = painting.getMotive().toString();
        this.frame = painting.getFrame().toString();
        this.material = painting.getMaterial().toString();
        this.facing = painting.getHorizontalFacing();
        this.rotation = painting.getRotation();
        this.x = painting.getAttachmentPos().getX();
        this.y = painting.getAttachmentPos().getY();
        this.z = painting.getAttachmentPos().getZ();
    }

    public PaintingDataMessage(PacketByteBuf b) {
        this.entityId = b.readInt();
        this.motive = b.readString();
        this.frame = b.readString();
        this.material = b.readString();
        this.facing = b.readEnumConstant(Direction.class);
        this.rotation = b.readInt();
        this.x = b.readInt();
        this.y = b.readInt();
        this.z = b.readInt();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeInt(entityId);
        b.writeString(motive);
        b.writeString(frame);
        b.writeString(material);
        b.writeEnumConstant(facing);
        b.writeInt(rotation);
        b.writeInt(x);
        b.writeInt(y);
        b.writeInt(z);
    }

    public Identifier getMotive() {
        return new Identifier(motive);
    }

    public Identifier getFrame() {
        return new Identifier(frame);
    }

    public int getRotation() {
        return rotation;
    }

    public Identifier getMaterial() {
        return new Identifier(material);
    }

    public int getEntityId() {
        return entityId;
    }

    public Direction getFacing() {
        return facing;
    }

    public BlockPos getPos() {
        return new BlockPos(x, y, z);
    }
}
