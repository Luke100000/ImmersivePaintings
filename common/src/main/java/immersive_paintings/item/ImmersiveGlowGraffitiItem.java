package immersive_paintings.item;

import immersive_paintings.entity.ImmersiveGlowGraffitiEntity;
import immersive_paintings.entity.ImmersiveGlowPaintingEntity;
import immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ImmersiveGlowGraffitiItem extends ImmersivePaintingItem {
    public ImmersiveGlowGraffitiItem(Settings settings) {
        super(settings);
    }

    @Override
    protected ImmersivePaintingEntity newPainting(World world, BlockPos attachmentPosition, Direction direction, int rotation) {
        return new ImmersiveGlowGraffitiEntity(world, attachmentPosition, direction, rotation);
    }
}
