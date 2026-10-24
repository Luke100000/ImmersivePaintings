package net.conczin.immersive_paintings.entity;

import net.conczin.immersive_paintings.ClientPaintingManager;
import net.conczin.immersive_paintings.ImmersivePaintings;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.conczin.immersive_paintings.client.gui.GuiWrapper;
import net.conczin.immersive_paintings.compat.PaintingImports;
import net.conczin.immersive_paintings.registry.Config;
import net.conczin.immersive_paintings.registry.Entity;
import net.conczin.immersive_paintings.registry.Item;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public class ImmersivePaintingEntity extends HangingEntity {
    public static final int MAX_DEPTH_OFFSET = 16;

    private static final int HIT_WINDOW_TICKS = 10;

    protected static final Predicate<net.minecraft.world.entity.Entity> PREDICATE = entity -> entity instanceof ImmersivePaintingEntity;

    private static final EntityDataAccessor<Identifier> MOTIVE = SynchedEntityData.defineId(ImmersivePaintingEntity.class, Entity.TRACKED_IDENTIFIER);
    private static final EntityDataAccessor<Identifier> FRAME = SynchedEntityData.defineId(ImmersivePaintingEntity.class, Entity.TRACKED_IDENTIFIER);
    private static final EntityDataAccessor<Identifier> MATERIAL = SynchedEntityData.defineId(ImmersivePaintingEntity.class, Entity.TRACKED_IDENTIFIER);

    private static final EntityDataAccessor<Integer> WIDTH = SynchedEntityData.defineId(ImmersivePaintingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HEIGHT = SynchedEntityData.defineId(ImmersivePaintingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DEPTH_OFFSET = SynchedEntityData.defineId(ImmersivePaintingEntity.class, EntityDataSerializers.INT);

    private int rotation;
    private long wobbleUntil;

    public ImmersivePaintingEntity(EntityType<? extends HangingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void setDirection(Direction direction) {
        setDirection(direction, rotation);
    }

    public void setDirection(Direction direction, int rotation) {
        if (direction == null)
            return;

        setDirectionRaw(direction);
        this.rotation = rotation;

        if (direction.getAxis().isHorizontal()) {
            absSnapRotationTo(direction.get2DDataValue() * 90, 0);
        } else {
            absSnapRotationTo(rotation, direction == Direction.UP ? 90.0f : -90.0f);
        }

        recalculateBoundingBox();
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && !level().isLoaded(pos.relative(getDirection().getOpposite()))) {
            return;
        }
        super.tick();
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction side) {
        Vec3 front = Vec3.atLowerCornerOf(side.getUnitVec3i());
        Vec3 up = side.getAxis().isVertical() ? new Vec3(0, 0, 1) : new Vec3(0, 1, 0);
        Vec3 cross = up.cross(front);

        if (rotation != 0) {
            float radians = (float) (rotation * Math.PI / 180.0);
            up = up.yRot(radians);
            cross = cross.yRot(radians);
        }

        // TODO: Add option to disable this offset logic and have paintings center on the block instead of the corner
        double dx = offsetForPaintingSize(getPaintingWidth());
        double dy = offsetForPaintingSize(getPaintingHeight());

        Direction facing;
        Direction counter;
        if (side.getAxis().isVertical()) {
            facing = Direction.fromYRot(rotation);
            if (side.equals(Direction.UP)) {
                facing = facing.getOpposite();
                counter = facing.getClockWise();
            } else { // Direction.DOWN
                counter = facing.getCounterClockWise();
            }
        } else {
            facing = Direction.UP;
            counter = side.getCounterClockWise();
        }

        Vec3 vec3d = Vec3.atCenterOf(pos)
                .relative(side, -0.46875 + getDepthOffset() / 16.0)
                .relative(counter, dx)
                .relative(facing, dy);

        Vec3 shift = up.scale(getPaintingHeight())
                .add(cross.scale(getPaintingWidth()))
                .add(front.scale(0.0625)); // 1 / 16

        return AABB.ofSize(vec3d, shift.x(), shift.y(), shift.z());
    }

    private double offsetForPaintingSize(int size) {
        return size % 2 == 0 ? (double) 0.5F : (double) 0.0F;
    }

    @Override
    public boolean survives() {
        if (Config.COMMON.testIfSpaceEmpty && !level().noCollision(this)) {
            return false;
        }

        BlockPos blockPos = pos.relative(getDirection().getOpposite());
        BlockState blockState = level().getBlockState(blockPos);
        if (!blockState.isSolid() && !DiodeBlock.isDiode(blockState)) {
            return false;
        }

        return level().getEntities(this, getBoundingBox(), PREDICATE).stream().noneMatch(v -> v.getDirection() == getDirection());
    }

    @Override
    public boolean canBeCollidedWith(@Nullable net.minecraft.world.entity.Entity entity) {
        return Config.COMMON.paintingsHaveCollision;
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        if (Config.COMMON.canOnlyBeDestroyedByPlayer) return;

        super.move(type, movement);
    }

    @Override
    public void push(double x, double y, double z) {
        if (Config.COMMON.canOnlyBeDestroyedByPlayer) return;

        super.push(x, y, z);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isInvulnerableToBase(source)) return false;
        if (Config.COMMON.canOnlyBeDestroyedByPlayer && !(source.getDirectEntity() instanceof Player)) return false;

        long gameTime = level.getGameTime();
        if (gameTime < wobbleUntil) {
            return super.hurtServer(level, source, amount);
        }

        wobbleUntil = gameTime + HIT_WINDOW_TICKS;
        level.broadcastEntityEvent(this, EntityEvent.ARMORSTAND_WOBBLE);
        return true;
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == EntityEvent.ARMORSTAND_WOBBLE) {
            wobbleUntil = level().getGameTime() + HIT_WINDOW_TICKS;
        } else {
            super.handleEntityEvent(event);
        }
    }

    public float getWobble(float tickDelta) {
        float remaining = (float) (wobbleUntil - level().getGameTime()) - tickDelta;
        if (remaining <= 0) return 0;
        return Mth.sin(remaining * Mth.HALF_PI) * remaining * 0.2F;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (!player.mayBuild()) return InteractionResult.PASS;

        if (player instanceof ServerPlayer serverPlayer && PaintingImports.interact(this, serverPlayer, hand)) {
            return InteractionResult.CONSUME;
        }

        if (level().isClientSide()) {
            GuiWrapper.open(getUUID());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(getItem());
    }

    @Override
    public void dropItem(ServerLevel serverLevel, @Nullable net.minecraft.world.entity.Entity entity) {
        if (serverLevel.getGameRules().get(GameRules.ENTITY_DROPS)) {
            playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (entity instanceof Player playerEntity && playerEntity.hasInfiniteMaterials()) {
                return;
            }

            spawnAtLocation(serverLevel, getItem());
        }
    }

    @Override
    public void snapTo(double x, double y, double z, float yRot, float xRot) {
        // Client position packets already contain attachment coordinates.
        if (level().isClientSide()) {
            setPos(x, y, z);
            return;
        }

        // Placement supplies the visible center; round back to the nearest attachment block.
        Vec3 offset = calculateBoundingBox(BlockPos.ZERO, getDirection()).getCenter();
        setPos(x - offset.x + 0.5, y - offset.y + 0.5, z - offset.z + 0.5);
    }

    @Override
    public Vec3 trackingPosition() {
        return Vec3.atLowerCornerOf(this.pos);
    }

    @Override
    public void playPlacementSound() {
        playSound(SoundEvents.PAINTING_PLACE, 1.0f, 1.0f);
    }

    @Override
    public void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MOTIVE, ImmersivePaintings.NONE_LOCATION);
        builder.define(FRAME, ImmersivePaintings.NONE_LOCATION);
        builder.define(MATERIAL, ImmersivePaintings.NONE_LOCATION);
        builder.define(WIDTH, 1);
        builder.define(HEIGHT, 1);
        builder.define(DEPTH_OFFSET, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        if (MOTIVE.equals(data)) {
            Optional<Painting> painting;
            if (level().isClientSide()) {
                painting = ClientPaintingManager.getPainting(getMotive());
            } else {
                painting = ServerPaintingManager.getPainting(level().getServer(), getMotive());
            }

            painting.ifPresent((p) -> {
                getEntityData().set(WIDTH, Math.max(p.width(), 1));
                getEntityData().set(HEIGHT, Math.max(p.height(), 1));

                recalculateBoundingBox();
            });
        } else if (DEPTH_OFFSET.equals(data)) {
            recalculateBoundingBox();
        }

        super.onSyncedDataUpdated(data);
    }

    /*
     * Both direction and rotation need to be present in the spawn packet for the client
     * To avoid any issues with yaw/pitch being set externally, pack the values into entityData
     *
     *
     * NOTE: This will probably never need a change, but should direction become
     * larger than a byte there will need to be a way to future-proof this
     */
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entityTrackerEntry) {
        return new ClientboundAddEntityPacket(this, (rotation << 4) | (byte)getDirection().get3DDataValue(), getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        setPos(BlockPos.containing(packet.getX(), packet.getY(), packet.getZ()));
        int data = packet.getData();
        setDirection(Direction.from3DDataValue(data & 15), data >> 4);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        SynchedEntityData tracker = getEntityData();
        tag.putString("Motive", tracker.get(MOTIVE).toString());
        tag.putString("Frame", tracker.get(FRAME).toString());
        tag.putString("Material", tracker.get(MATERIAL).toString());
        tag.putInt("Facing", getDirection().get3DDataValue());
        tag.putInt("VRotation", rotation);
        tag.putInt("DepthOffset", getDepthOffset());
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        setFrame(Identifier.parse(input.getStringOr("Frame", ImmersivePaintings.NONE_LOCATION.toString())));
        setMaterial(Identifier.parse(input.getStringOr("Material", ImmersivePaintings.NONE_LOCATION.toString())));
        Direction direction = Direction.from3DDataValue(input.getIntOr("Facing", Direction.SOUTH.get3DDataValue()));

        setDepthOffset(input.getIntOr("DepthOffset", 0));
        setDirection(direction, input.getIntOr("VRotation", input.getIntOr("Rotation", 0)));
        setMotive(Identifier.parse(input.getStringOr("Motive", ImmersivePaintings.NONE_LOCATION.toString())));
    }

    public net.minecraft.world.item.Item getItem() {
        return Item.PAINTING;
    }

    public boolean isGraffiti() {
        return false;
    }

    public boolean isGlowing() {
        return false;
    }

    public int getDepthOffset() {
        return getEntityData().get(DEPTH_OFFSET);
    }

    public void setDepthOffset(int offset) {
        getEntityData().set(DEPTH_OFFSET, Mth.clamp(offset, -MAX_DEPTH_OFFSET, MAX_DEPTH_OFFSET));
    }

    public int getPaintingWidth() {
        return getEntityData().get(WIDTH);
    }

    public int getPaintingHeight() {
        return getEntityData().get(HEIGHT);
    }

    public Identifier getMotive() {
        return getEntityData().get(MOTIVE);
    }

    public Identifier getFrame() {
        return getEntityData().get(FRAME);
    }

    public Identifier getMaterial() {
        return getEntityData().get(MATERIAL);
    }

    public void setMotive(Identifier motive) {
        getEntityData().set(MOTIVE, motive);
    }

    public void setFrame(Identifier frame) {
        getEntityData().set(FRAME, frame);
    }

    public void setMaterial(Identifier material) {
        getEntityData().set(MATERIAL, material);
    }
}
