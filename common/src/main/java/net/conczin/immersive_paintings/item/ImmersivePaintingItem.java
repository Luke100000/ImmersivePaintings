package net.conczin.immersive_paintings.item;

import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.conczin.immersive_paintings.registration.Entities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class ImmersivePaintingItem extends Item {

    public ImmersivePaintingItem(Item.Properties properties) {
        super(properties);
    }

    protected boolean mayUseItemAt(Player player, Direction side, ItemStack stack, BlockPos pos) {
        return player.mayUseItemAt(pos, side, stack);
    }

    protected EntityType<? extends ImmersivePaintingEntity> getEntityType() {
        return Entities.PAINTING;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos blockPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos attachmentPosition = blockPos.relative(direction);
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        if (player == null || !mayUseItemAt(player, direction, itemStack, attachmentPosition))
            return InteractionResult.FAIL;

        Level level = context.getLevel();

        int rotation = 0;
        if (direction.getAxis().isVertical()) {
            rotation = Math.floorMod((int) Math.floor(player.getYRot() / 90.0f + 2.5) * 90, 360);
        }

        ImmersivePaintingEntity entity = getEntityType().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (entity == null) return InteractionResult.FAIL;
        entity.setPos(attachmentPosition);
        entity.setDirection(direction, rotation);

        if (entity.survives()) {
            if (!level.isClientSide()) {
                entity.playPlacementSound();
                level.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
                level.addFreshEntity(entity);
            }

            itemStack.shrink(1);
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.CONSUME;
        }
    }
}
