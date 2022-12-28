package immersive_paintings.entity;

import immersive_paintings.Entities;
import immersive_paintings.Items;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ImmersiveGlowPaintingEntity extends ImmersivePaintingEntity {
    public ImmersiveGlowPaintingEntity(World world, BlockPos pos, Direction direction, int rotation) {
        super(Entities.GLOW_PAINTING.get(), world, pos);

        setFacing(direction, rotation);
    }

    public ImmersiveGlowPaintingEntity(EntityType<Entity> type, World world) {
        super(type, world);
    }

    @Override
    public Item getDrop() {
        return Items.GLOW_PAINTING.get();
    }
}
