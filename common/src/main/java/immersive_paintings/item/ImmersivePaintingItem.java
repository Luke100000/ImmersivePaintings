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
import net.minecraft.world.event.GameEvent;

public class ImmersivePaintingItem extends Item {
    public ImmersivePaintingItem(Settings settings) {
        super(settings);
    }

    protected ImmersivePaintingEntity newPainting(World world, BlockPos attachmentPosition, Direction direction, int rotation) {
        return new ImmersivePaintingEntity(world, attachmentPosition, direction, rotation);
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
                rotation = Math.floorMod((int) Math.floor(playerEntity.getYaw() / 90.0f + 2.5) * 90, 360);
            }
            World world = context.getWorld();
            ImmersivePaintingEntity paintingEntity = newPainting(world, attachmentPosition, direction, rotation);

            NbtCompound nbtCompound = itemStack.getNbt();
            if (nbtCompound != null) {
                EntityType.loadFromEntityNbt(world, playerEntity, paintingEntity, nbtCompound);
            }

            if (paintingEntity.canStayAttached()) {
                if (!world.isClient) {
                    ((AbstractImmersiveDecorationEntity) paintingEntity).onPlace();
                    world.emitGameEvent(playerEntity, GameEvent.ENTITY_PLACE, blockPos);
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
