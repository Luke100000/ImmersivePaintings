package immersive_paintings.item;

import immersive_paintings.entity.ImmersiveGraffitiEntity;
import immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ImmersiveGraffitiItem extends ImmersivePaintingItem {
    public ImmersiveGraffitiItem(Settings settings) {
        super(settings);
    }

    @Override
    protected ImmersivePaintingEntity newPainting(World world, BlockPos attachmentPosition, Direction direction, int rotation) {
        return new ImmersiveGraffitiEntity(world, attachmentPosition, direction, rotation);
    }
}
