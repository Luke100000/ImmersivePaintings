package immersive_paintings.item;

import immersive_paintings.entity.AbstractImmersiveDecorationEntity;
import immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ImmersivePaintingItem extends Item {
    public ImmersivePaintingItem(Settings settings) {
        super(settings);
    }

     @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockPos attachmentPosition = blockPos.offset(direction);
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();
        if (playerEntity != null && !this.canPlaceOn(playerEntity, direction, itemStack, attachmentPosition)) {
            return ActionResult.FAIL;
        } else {
            int rotation = 0;
            if (playerEntity != null && direction.getAxis().isVertical()) {
                rotation = (int)(playerEntity.getYaw(1.0f) / 90 + (direction == Direction.UP ? 2.5 : 0.5)) * 90;
            }
            World world = context.getWorld();
            ImmersivePaintingEntity paintingEntity = new ImmersivePaintingEntity(world, attachmentPosition, direction, rotation);

            NbtCompound nbtCompound = itemStack.getOrCreateTag();
            if (nbtCompound != null) {
                EntityType.loadFromEntityNbt(world, playerEntity, paintingEntity, nbtCompound);
            }

            if (paintingEntity.canStayAttached()) {
                if (!world.isClient) {
                    ((AbstractImmersiveDecorationEntity)paintingEntity).onPlace();
                    world.spawnEntity(paintingEntity);
                }

                itemStack.decrement(1);
                return ActionResult.success(world.isClient);
            } else {
                return ActionResult.CONSUME;
            }
        }
    }

    protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
        return player.canPlaceOn(pos, side, stack);
    }
}
