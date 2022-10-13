package immersive_paintings.network;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class PaintingDataMessage implements Message {
    private static final long serialVersionUID = -6510034100878125474L;

    final String motive;
    final String frame;
    final String material;
    final int entityId;
    private final Direction facing;
    private final int rotation;
    private final int x, y, z;

    public PaintingDataMessage(ImmersivePaintingEntity painting) {
        entityId = painting.getEntityId();
        this.motive = painting.getMotive().toString();
        this.frame = painting.getFrame().toString();
        this.material = painting.getMaterial().toString();
        this.facing = painting.getHorizontalFacing();
        this.rotation = painting.getRotation();
        this.x = painting.getAttachmentPos().getX();
        this.y = painting.getAttachmentPos().getY();
        this.z = painting.getAttachmentPos().getZ();
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
