package immersive_paintings.network;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.io.Serial;

public abstract class PaintingDataMessage implements Message {
    @Serial
    private static final long serialVersionUID = -6510034100878125474L;

    final String motive;
    final String frame;
    final String material;
    final int entityId;
    private final int facing;

    public PaintingDataMessage(ImmersivePaintingEntity painting) {
        entityId = painting.getId();
        this.motive = painting.getMotive().toString();
        this.frame = painting.getFrame().toString();
        this.material = painting.getMaterial().toString();
        this.facing = painting.getHorizontalFacing().ordinal();
    }

    public Identifier getMotive() {
        return new Identifier(motive);
    }

    public Identifier getFrame() {
        return new Identifier(frame);
    }

    public Identifier getMaterial() {
        return new Identifier(material);
    }

    public int getEntityId() {
        return entityId;
    }

    public Direction getFacing() {
        return Direction.values()[facing];
    }
}
