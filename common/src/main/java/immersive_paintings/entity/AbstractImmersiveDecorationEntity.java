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
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;

public abstract class AbstractImmersiveDecorationEntity extends Entity {
    protected static final Predicate<Entity> PREDICATE = entity -> entity instanceof AbstractImmersiveDecorationEntity;
    private int obstructionCheckCounter;
    protected BlockPos attachmentPos;
    protected Direction facing = Direction.SOUTH;
    protected int rotation = 0;

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

    public void setFacing(Direction facing, int rotation) {
        this.facing = facing;
        this.rotation = rotation;

        if (this.facing.getAxis().isHorizontal()) {
            this.setYaw(this.facing.getHorizontal() * 90);
            this.setPitch(0);
        } else {
            this.setYaw(rotation);
            this.setPitch(this.facing == Direction.UP ? 90.0f : -90.0f);
        }
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();

        this.updateAttachmentPosition();
    }

    protected void updateAttachmentPosition() {
        if (this.facing == null) {
            return;
        }
        double x = (double)this.attachmentPos.getX() + 0.5;
        double y = (double)this.attachmentPos.getY() + 0.5;
        double z = (double)this.attachmentPos.getZ() + 0.5;
        double ox = this.isEven(this.getWidthPixels());
        double oy = this.isEven(this.getHeightPixels());

        Vec3i front = this.facing.getVector();
        Vec3i up = this.facing.getAxis().isVertical() ? new Vec3i(0, 0, 1) : new Vec3i(0, 1, 0);
        Vec3i side = up.crossProduct(front);

        if (rotation != 0) {
            double cos = Math.cos(rotation / 180.0 * Math.PI);
            double sin = Math.sin(rotation / 180.0 * Math.PI);
            up = new Vec3i((int)Math.round(up.getX() * cos - up.getZ() * sin), up.getY(), (int)Math.round(up.getX() * sin + up.getZ() * cos));
            side = new Vec3i((int)Math.round(side.getX() * cos - side.getZ() * sin), side.getY(), (int)Math.round(side.getX() * sin + side.getZ() * cos));
        }

        double w = getWidthPixels() / 32.0;
        double h = getHeightPixels() / 32.0;
        double d = 1.0 / 32.0;

        //move to the side of the respective wall
        x -= (double)this.facing.getOffsetX() * 7.5 / 16.0 - up.getX() * oy - side.getX() * ox;
        y -= (double)this.facing.getOffsetY() * 7.5 / 16.0 - up.getY() * oy - side.getY() * ox;
        z -= (double)this.facing.getOffsetZ() * 7.5 / 16.0 - up.getZ() * oy - side.getZ() * ox;
        this.setPos(x, y, z);

        this.setBoundingBox(new Box(
                x - up.getX() * h - side.getX() * w - front.getX() * d,
                y - up.getY() * h - side.getY() * w - front.getY() * d,
                z - up.getZ() * h - side.getZ() * w - front.getZ() * d,
                x + up.getX() * h + side.getX() * w + front.getX() * d,
                y + up.getY() * h + side.getY() * w + front.getY() * d,
                z + up.getZ() * h + side.getZ() * w + front.getZ() * d
        ));
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

        return this.world.getOtherEntities(this, this.getBoundingBox(), PREDICATE).stream().noneMatch(v -> ((AbstractImmersiveDecorationEntity)v).facing == this.facing);
    }

    public int getRotation() {
        return rotation;
    }

    @Override
    public boolean isCollidable() {
        return Config.getInstance().paintingsHaveCollision;
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        if (attacker instanceof PlayerEntity playerEntity) {
            if (!this.world.canPlayerModifyAt(playerEntity, this.attachmentPos)) {
                return true;
            }
            return this.damage(attacker.world.getDamageSources().playerAttack(playerEntity), 0.0f);
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

    private static final Map<Direction, Byte> DIRECTION_TO_ID = Map.of(
            Direction.DOWN, (byte)5,
            Direction.UP, (byte)4,
            Direction.NORTH, (byte)2,
            Direction.SOUTH, (byte)0,
            Direction.WEST, (byte)1,
            Direction.EAST, (byte)3
    );

    private static final Map<Byte, Direction> ID_TO_DIRECTION = Map.of(
            (byte)5, Direction.DOWN,
            (byte)4, Direction.UP,
            (byte)2, Direction.NORTH,
            (byte)0, Direction.SOUTH,
            (byte)1, Direction.WEST,
            (byte)3, Direction.EAST
    );

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("TileX", attachmentPos.getX());
        nbt.putInt("TileY", attachmentPos.getY());
        nbt.putInt("TileZ", attachmentPos.getZ());
        nbt.putByte("Facing", DIRECTION_TO_ID.get(this.facing));
        nbt.putInt("Rotation", this.rotation);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.attachmentPos = new BlockPos(nbt.getInt("TileX"), nbt.getInt("TileY"), nbt.getInt("TileZ"));
        this.facing = ID_TO_DIRECTION.get(nbt.getByte("Facing"));
        this.rotation = nbt.getInt("Rotation");
        this.setFacing(this.facing, this.rotation);
    }

    public abstract int getWidthPixels();

    public abstract int getHeightPixels();

    public abstract void onBreak(@Nullable Entity var1);

    public abstract void onPlace();

    @Override
    public ItemEntity dropStack(ItemStack stack, float yOffset) {
        ItemEntity itemEntity = new ItemEntity(this.world, this.getX() + ((double)this.facing.getOffsetX() * 0.3), this.getY() + ((double)this.facing.getOffsetY() * 0.3) + yOffset, this.getZ() + ((double)this.facing.getOffsetZ() * 0.3), stack);
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
        this.attachmentPos = BlockPos.ofFloored(x, y, z);
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

