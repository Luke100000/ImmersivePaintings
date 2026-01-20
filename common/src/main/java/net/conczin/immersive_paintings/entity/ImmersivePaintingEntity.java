package net.conczin.immersive_paintings.entity;

import net.conczin.immersive_paintings.ClientPaintingManager;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.conczin.immersive_paintings.client.gui.GuiWrapper;
import net.conczin.immersive_paintings.compat.XercaPaintCompat;
import net.conczin.immersive_paintings.registry.Configs;
import net.conczin.immersive_paintings.registry.Entities;
import net.conczin.immersive_paintings.registry.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public class ImmersivePaintingEntity extends HangingEntity {
    protected static final Predicate<Entity> PREDICATE = entity -> entity instanceof ImmersivePaintingEntity;

    private static final EntityDataAccessor<ResourceLocation> MOTIVE = SynchedEntityData.defineId(ImmersivePaintingEntity.class, Entities.TRACKED_IDENTIFIER);
    private static final EntityDataAccessor<ResourceLocation> FRAME = SynchedEntityData.defineId(ImmersivePaintingEntity.class, Entities.TRACKED_IDENTIFIER);
    private static final EntityDataAccessor<ResourceLocation> MATERIAL = SynchedEntityData.defineId(ImmersivePaintingEntity.class, Entities.TRACKED_IDENTIFIER);

    private static final EntityDataAccessor<Integer> WIDTH = SynchedEntityData.defineId(ImmersivePaintingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HEIGHT = SynchedEntityData.defineId(ImmersivePaintingEntity.class, EntityDataSerializers.INT);

    private int rotation;

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
    protected AABB calculateBoundingBox(BlockPos pos, Direction side) {
        Vec3 front = Vec3.atLowerCornerOf(side.getUnitVec3i());
        Vec3 up = side.getAxis().isVertical() ? new Vec3(0, 0, 1) : new Vec3(0, 1, 0);
        Vec3 cross = up.cross(front);

        if (rotation != 0) {
            float radians = (float)(rotation * Math.PI / 180.0);
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
                .relative(side, -0.46875f)
                .relative(counter, dx)
                .relative(facing, dy);

        Vec3 shift = up.scale(getPaintingHeight())
            .add(cross.scale(getPaintingWidth()))
            .add(front.scale(0.0625)); // 1 / 16

        return AABB.ofSize(vec3d, shift.x(), shift.y(), shift.z());
    }

    private double offsetForPaintingSize(int size) {
        return size % 2 == 0 ? (double)0.5F : (double)0.0F;
    }
    
    @Override
    public boolean survives() {
        if (Configs.COMMON.testIfSpaceEmpty && !level().noCollision(this)) {
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
    public boolean canBeCollidedWith(@Nullable Entity entity) {
        return Configs.COMMON.paintingsHaveCollision;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isCrouching() && !level().isClientSide) {
            XercaPaintCompat.interactWithPainting(this, player, hand);
        } else if (!player.isCrouching() && level().isClientSide) {
            GuiWrapper.open(getUUID());
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(getItem());
    }

    @Override
    public void dropItem(ServerLevel serverLevel, @Nullable Entity entity) {
        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (entity instanceof Player playerEntity && playerEntity.hasInfiniteMaterials()) {
                return;
            }

            spawnAtLocation(serverLevel, getItem());
        }
    }

    // When updating a motive, the motive is temporarily reset to "none" and the position gets messed up.
    // Fix this by fixing the tracking position to the lower corner.
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
        builder.define(MOTIVE, Main.NONE_LOCATION);
        builder.define(FRAME, Main.NONE_LOCATION);
        builder.define(MATERIAL, Main.NONE_LOCATION);
        builder.define(WIDTH, 1);
        builder.define(HEIGHT, 1);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        if (MOTIVE.equals(data)) {
            Optional<Painting> painting;
            if (level().isClientSide) {
                painting = ClientPaintingManager.getPainting(getMotive());
            } else {
                painting = ServerPaintingManager.getPainting(getServer(), getMotive());
            }

            painting.ifPresent((p) -> {
                getEntityData().set(WIDTH, Math.max(p.width(), 1));
                getEntityData().set(HEIGHT, Math.max(p.height(), 1));

                recalculateBoundingBox();
            });
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
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        setFrame(ResourceLocation.parse(input.getStringOr("Frame", Main.NONE_LOCATION.toString())));
        setMaterial(ResourceLocation.parse(input.getStringOr("Material", Main.NONE_LOCATION.toString())));
        Direction direction = Direction.from3DDataValue(input.getIntOr("Facing", Direction.SOUTH.get3DDataValue()));

        setDirection(direction, input.getIntOr("VRotation", 0));
        setMotive(ResourceLocation.parse(input.getStringOr("Motive", Main.NONE_LOCATION.toString())));
    }

    public Item getItem() {
        return Items.PAINTING;
    }

    public boolean isGraffiti() {
        return false;
    }

    public boolean isGlowing() {
        return false;
    }

    public int getPaintingWidth() {
        return getEntityData().get(WIDTH);
    }

    public int getPaintingHeight() {
        return getEntityData().get(HEIGHT);
    }

    public ResourceLocation getMotive() {
        return getEntityData().get(MOTIVE);
    }

    public ResourceLocation getFrame() {
        return getEntityData().get(FRAME);
    }

    public ResourceLocation getMaterial() {
        return getEntityData().get(MATERIAL);
    }

    public void setMotive(ResourceLocation motive) {
        getEntityData().set(MOTIVE, motive);
    }

    public void setFrame(ResourceLocation frame) {
        getEntityData().set(FRAME, frame);
    }

    public void setMaterial(ResourceLocation material) {
        getEntityData().set(MATERIAL, material);
    }
}
