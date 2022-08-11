package immersive_paintings.entity;

import immersive_paintings.Config;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public abstract class AbstractImmersiveDecorationEntity extends Entity {
    protected static final Predicate<Entity> PREDICATE = entity -> entity instanceof AbstractImmersiveDecorationEntity;
    private int obstructionCheckCounter;
    protected BlockPos attachmentPos;
    protected Direction facing = Direction.SOUTH;

    protected AbstractImmersiveDecorationEntity(EntityType<? extends Entity> entityType, World world) {
        super(entityType, world);
    }

    protected AbstractImmersiveDecorationEntity(EntityType<? extends Entity> type, World world, BlockPos pos) {
        this(type, world);
        this.attachmentPos = pos;
    }

    @Override
    protected void initDataTracker() {
    }

    public void setFacing(Direction facing) {
        Validate.notNull(facing);
        Validate.isTrue(facing.getAxis().isHorizontal());
        this.facing = facing;
        this.setYaw(this.facing.getHorizontal() * 90);
        this.prevYaw = this.getYaw();
        this.updateAttachmentPosition();
    }

    protected void updateAttachmentPosition() {
        if (this.facing == null) {
            return;
        }
        double x = (double)this.attachmentPos.getX() + 0.5;
        double y = (double)this.attachmentPos.getY() + 0.5;
        double z = (double)this.attachmentPos.getZ() + 0.5;
        double oz = this.isEven(this.getWidthPixels());
        double oy = this.isEven(this.getHeightPixels());
        x -= (double)this.facing.getOffsetX() * 7.5 / 16.0;
        z -= (double)this.facing.getOffsetZ() * 7.5 / 16.0;
        Direction direction = this.facing.rotateYCounterclockwise();
        this.setPos(x += oz * (double)direction.getOffsetX(), y += oy, z += oz * (double)direction.getOffsetZ());
        double offsetX = this.getWidthPixels();
        double offsetY = this.getHeightPixels();
        double offsetZ = this.getWidthPixels();
        if (this.facing.getAxis() == Direction.Axis.Z) {
            offsetZ = 1.0;
        } else {
            offsetX = 1.0;
        }
        this.setBoundingBox(new Box(x - (offsetX /= 32.0), y - (offsetY /= 32.0), z - (offsetZ /= 32.0), x + offsetX, y + offsetY, z + offsetZ));
    }

    private double isEven(int i) {
        return i % 32 == 0 ? 0.5 : 0.0;
    }

    @Override
    public void tick() {
        if (!this.world.isClient) {
            this.attemptTickInVoid();
            if (this.obstructionCheckCounter++ == 100) {
                this.obstructionCheckCounter = 0;
                if (!this.isRemoved() && !this.canStayAttached()) {
                    this.discard();
                    this.onBreak(null);
                }
            }
        }
    }

    public boolean canStayAttached() {
        if (Config.getInstance().testIfSpaceEmpty && !this.world.isSpaceEmpty(this)) {
            return false;
        }

        BlockPos blockPos = this.attachmentPos.offset(this.facing.getOpposite());
        BlockState blockState = this.world.getBlockState(blockPos);
        if (!blockState.getMaterial().isSolid() && !AbstractRedstoneGateBlock.isRedstoneGate(blockState)) {
            return false;
        }

        return this.world.getOtherEntities(this, this.getBoundingBox(), PREDICATE).isEmpty();
    }

    @Override
    public boolean collides() {
        return true;
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        if (attacker instanceof PlayerEntity playerEntity) {
            if (!this.world.canPlayerModifyAt(playerEntity, this.attachmentPos)) {
                return true;
            }
            return this.damage(DamageSource.player(playerEntity), 0.0f);
        }
        return false;
    }

    @Override
    public Direction getHorizontalFacing() {
        return this.facing;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!this.isRemoved() && !this.world.isClient) {
            this.kill();
            this.scheduleVelocityUpdate();
            this.onBreak(source.getAttacker());
        }
        return true;
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {
        if (!this.world.isClient && !this.isRemoved() && movement.lengthSquared() > 0.0) {
            this.kill();
            this.onBreak(null);
        }
    }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
        if (!this.world.isClient && !this.isRemoved() && deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 0.0) {
            this.kill();
            this.onBreak(null);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("TileX", attachmentPos.getX());
        nbt.putInt("TileY", attachmentPos.getY());
        nbt.putInt("TileZ", attachmentPos.getZ());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.attachmentPos = new BlockPos(nbt.getInt("TileX"), nbt.getInt("TileY"), nbt.getInt("TileZ"));
    }

    public abstract int getWidthPixels();

    public abstract int getHeightPixels();

    public abstract void onBreak(@Nullable Entity var1);

    public abstract void onPlace();

    @Override
    public ItemEntity dropStack(ItemStack stack, float yOffset) {
        ItemEntity itemEntity = new ItemEntity(this.world, this.getX() + (double)((float)this.facing.getOffsetX() * 0.15f), this.getY() + (double)yOffset, this.getZ() + (double)((float)this.facing.getOffsetZ() * 0.15f), stack);
        itemEntity.setToDefaultPickupDelay();
        this.world.spawnEntity(itemEntity);
        return itemEntity;
    }

    @Override
    protected boolean shouldSetPositionOnLoad() {
        return false;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.attachmentPos = new BlockPos(x, y, z);
        this.updateAttachmentPosition();
        this.velocityDirty = true;
    }

    @Override
    public float applyRotation(BlockRotation rotation) {
        if (this.facing.getAxis() != Direction.Axis.Y) {
            switch (rotation) {
                case CLOCKWISE_180 -> this.facing = this.facing.getOpposite();
                case COUNTERCLOCKWISE_90 -> this.facing = this.facing.rotateYCounterclockwise();
                case CLOCKWISE_90 -> this.facing = this.facing.rotateYClockwise();
            }
        }
        float f = MathHelper.wrapDegrees(this.getYaw());
        switch (rotation) {
            case CLOCKWISE_180 -> {
                return f + 180.0f;
            }
            case COUNTERCLOCKWISE_90 -> {
                return f + 90.0f;
            }
            case CLOCKWISE_90 -> {
                return f + 270.0f;
            }
        }
        return f;
    }

    @Override
    public float applyMirror(BlockMirror mirror) {
        return this.applyRotation(mirror.getRotation(this.facing));
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
    }

    @Override
    public void calculateDimensions() {
    }

    public BlockPos getAttachmentPos() {
        return attachmentPos;
    }

    public void setAttachmentPos(BlockPos pos) {
        attachmentPos = pos;
        updateAttachmentPosition();
    }
}

