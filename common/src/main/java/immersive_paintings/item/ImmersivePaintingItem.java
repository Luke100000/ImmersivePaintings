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

    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockPos blockPos2 = blockPos.offset(direction);
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();
        if (playerEntity != null && !this.canPlaceOn(playerEntity, direction, itemStack, blockPos2)) {
            return ActionResult.FAIL;
        } else {
            World world = context.getWorld();
            ImmersivePaintingEntity paintingEntity = new ImmersivePaintingEntity(world, blockPos2, direction);

            NbtCompound nbtCompound = itemStack.getNbt();
            if (nbtCompound != null) {
                EntityType.loadFromEntityNbt(world, playerEntity, paintingEntity, nbtCompound);
            }

            if (paintingEntity.canStayAttached()) {
                if (!world.isClient) {
                    ((AbstractImmersiveDecorationEntity)paintingEntity).onPlace();
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
        return !side.getAxis().isVertical() && player.canPlaceOn(pos, side, stack);
    }
}
