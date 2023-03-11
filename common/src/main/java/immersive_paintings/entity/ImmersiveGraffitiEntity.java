package immersive_paintings.entity;

import immersive_paintings.Entities;
import immersive_paintings.Items;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ImmersiveGraffitiEntity extends ImmersivePaintingEntity {
    public ImmersiveGraffitiEntity(World world, BlockPos pos, Direction direction, int rotation) {
        super(Entities.GRAFFITI, world, pos);

        setFacing(direction, rotation);
    }

    public ImmersiveGraffitiEntity(EntityType<Entity> type, World world) {
        super(type, world);
    }

    @Override
    public Item getDrop() {
        return Items.GRAFFITI;
    }
}
