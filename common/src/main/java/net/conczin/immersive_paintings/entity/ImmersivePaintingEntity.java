package net.conczin.immersive_paintings.entity;

import net.conczin.immersive_paintings.*;
import net.conczin.immersive_paintings.compat.XercaPaintCompat;
import net.conczin.immersive_paintings.item.Items;
import net.conczin.immersive_paintings.network.Network;
import net.conczin.immersive_paintings.network.payload.s2c.OpenGuiPayload;
import net.conczin.immersive_paintings.painting.ClientPaintingManager;
import net.conczin.immersive_paintings.painting.Painting;
import net.conczin.immersive_paintings.painting.PlayerManager;
import net.conczin.immersive_paintings.painting.ServerPaintingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

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

        this.direction = direction;
        this.rotation = rotation;

        if (direction.getAxis().isHorizontal()) {
            absRotateTo(direction.get2DDataValue() * 90, 0);
        } else {
            absRotateTo(rotation, direction == Direction.UP ? 90.0f : -90.0f);
        }

        recalculateBoundingBox();
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction side) {
        Vec3 front = Vec3.atLowerCornerOf(side.getNormal());
        Vec3 up = side.getAxis().isVertical() ? new Vec3(0, 0, 1) : new Vec3(0, 1, 0);
        Vec3 cross = up.cross(front);

        if (rotation != 0) {
            float radians = (float)(rotation * Math.PI / 180.0);
            up = up.yRot(radians);
            cross = cross.yRot(radians);
        }

        Vec3 vec3d = Vec3.atCenterOf(pos).relative(side, -0.46875); // 7.5 / 16 == 15 / 32
        Vec3 shift = up.scale(getPaintingHeight())
            .add(cross.scale(getPaintingWidth()))
            .add(front.scale(0.0625)); // 1 / 16

        return AABB.ofSize(vec3d, shift.x(), shift.y(), shift.z());
    }
    
    @Override
    public boolean survives() {
        if (Config.getInstance().testIfSpaceEmpty && !level().noCollision(this)) {
            return false;
        }

        BlockPos blockPos = pos.relative(direction.getOpposite());
        BlockState blockState = level().getBlockState(blockPos);
        if (!blockState.isSolid() && !DiodeBlock.isDiode(blockState)) {
            return false;
        }

        return level().getEntities(this, getBoundingBox(), PREDICATE).stream().noneMatch(v -> ((ImmersivePaintingEntity)v).direction == direction);
    }

    @Override
    public boolean canBeCollidedWith() {
        return Config.getInstance().paintingsHaveCollision;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) {
            if (!XercaPaintCompat.interactWithPainting(this, player, hand)) {
                Config config = Config.getInstance();
                Network.sendToClient(serverPlayer, new OpenGuiPayload(
                        OpenGuiPayload.GuiType.EDITOR, getId(),
                        config.minPaintingResolution, config.maxPaintingResolution,
                        config.showOtherPlayersPaintings, config.uploadPermissionLevel
                ));
            }
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(getItem());
    }

    @Override
    public void dropItem(@Nullable Entity entity) {
        if (level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (entity instanceof Player playerEntity && playerEntity.hasInfiniteMaterials()) {
                return;
            }

            spawnAtLocation(getItem());
        }
    }

    @Override
    public void playPlacementSound() {
        playSound(SoundEvents.PAINTING_PLACE, 1.0f, 1.0f);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        PlayerManager.playerRequestedImages(player);
        super.startSeenByPlayer(player);
    }
    
    @Override
    public void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(MOTIVE, Main.locate("none"));
        builder.define(FRAME, Main.locate("none"));
        builder.define(MATERIAL, Main.locate("none"));
        builder.define(WIDTH, 1);
        builder.define(HEIGHT, 1);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        if (MOTIVE.equals(data)) {
            Painting painting;
            if (level().isClientSide) {
                painting = ClientPaintingManager.getPainting(getMotive());
            } else {
                painting = ServerPaintingManager.getPainting(getServer(), getMotive());
            }

            if (!painting.hash().equals(Painting.DEFAULT.hash())) {
                getEntityData().set(WIDTH, Math.max(painting.width(), 1));
                getEntityData().set(HEIGHT, Math.max(painting.height(), 1));
            }
        }

        // Width and height are always set at the same time
        // To avoid double updates only check for height changes
        if (HEIGHT.equals(data)) {
            recalculateBoundingBox();
        }

        super.onSyncedDataUpdated(data);
    }

    /*
     * Both direction and rotation need to be present in the spawn packet for the client
     * To avoid any issues with yaw/pitch being set externally, pack the values into entityData
     * 
     * 
     * NOTE: This wiill probably never need a change, but should direction become
     * larger than a byte there will need to be a way to future-proof this
     */
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entityTrackerEntry) {
        return new ClientboundAddEntityPacket(this, (rotation << 4) | (byte)direction.get3DDataValue(), getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        int data = packet.getData();
        setDirection(Direction.from3DDataValue(data & 15), data >> 4);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        SynchedEntityData tracker = getEntityData();
        nbt.putString("Motive", tracker.get(MOTIVE).toString());
        nbt.putString("Frame", tracker.get(FRAME).toString());
        nbt.putString("Material", tracker.get(MATERIAL).toString());
        nbt.putInt("Facing", direction.get3DDataValue());
        nbt.putInt("Rotation", rotation);
        super.addAdditionalSaveData(nbt);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        setMotive(ResourceLocation.parse(nbt.getString("Motive")));
        setFrame(ResourceLocation.parse(nbt.getString("Frame")));
        setMaterial(ResourceLocation.parse(nbt.getString("Material")));
        setDirection(Direction.from3DDataValue(nbt.getInt("Facing")), nbt.getInt("Rotation"));
        super.readAdditionalSaveData(nbt);
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

    public void setMotive(ResourceLocation motive) {
        getEntityData().set(MOTIVE, motive);
    }

    public ResourceLocation getFrame() {
        return getEntityData().get(FRAME);
    }

    public void setFrame(ResourceLocation frame) {
        getEntityData().set(FRAME, frame);
    }

    public ResourceLocation getMaterial() {
        return getEntityData().get(MATERIAL);
    }

    public void setMaterial(ResourceLocation material) {
        getEntityData().set(MATERIAL, material);
    }
}
