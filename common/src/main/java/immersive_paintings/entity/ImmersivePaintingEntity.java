package immersive_paintings.entity;

import immersive_paintings.Entities;
import immersive_paintings.Items;
import immersive_paintings.Main;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.s2c.OpenGuiRequest;
import immersive_paintings.network.s2c.PaintingModifyMessage;
import immersive_paintings.resources.ClientPaintingManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ImmersivePaintingEntity extends AbstractImmersiveDecorationEntity {
    private Identifier motive = Main.locate("none");
    private Identifier frame = Main.locate("none");
    private Identifier material = Main.locate("none");

    public ImmersivePaintingEntity(World world, BlockPos pos, Direction direction) {
        super(Entities.PAINTING, world, pos);

        setFacing(direction);
    }

    public ImmersivePaintingEntity(EntityType<Entity> type, World world) {
        super(type, world);
    }

    @Override
    public int getWidthPixels() {
        return ClientPaintingManager.getPainting(motive).width * 16;
    }

    @Override
    public int getHeightPixels() {
        return ClientPaintingManager.getPainting(motive).height * 16;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putString("Motive", motive.toString());
        nbt.putString("Frame", frame.toString());
        nbt.putString("Material", material.toString());
        nbt.putByte("Facing", (byte)this.facing.getHorizontal());
        super.writeCustomDataToNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.motive = new Identifier(nbt.getString("Motive"));
        this.frame = new Identifier(nbt.getString("Frame"));
        this.material = new Identifier(nbt.getString("Material"));
        this.facing = Direction.fromHorizontal(nbt.getByte("Facing"));
        super.readCustomDataFromNbt(nbt);
        this.setFacing(this.facing);
    }

    @Override
    public void onBreak(@Nullable Entity entity) {
        if (!world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            return;
        }
        playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0f, 1.0f);
        if (entity instanceof PlayerEntity playerEntity) {
            if (playerEntity.getAbilities().creativeMode) {
                return;
            }
        }
        dropItem(Items.PAINTING);
    }

    @Override
    public void onPlace() {
        this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0f, 1.0f);
    }

    @Override
    public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        this.setPosition(x, y, z);
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
        BlockPos blockPos = this.attachmentPos.add(x - this.getX(), y - this.getY(), z - this.getZ());
        this.setPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public Packet<?> createSpawnPacket() {
        BlockPos pos = getDecorationBlockPos();
        return new EntitySpawnS2CPacket(
                getId(),
                getUuid(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                getPitch(),
                getYaw(),
                getType(),
                0,
                getVelocity());
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        NetworkHandler.sendToPlayer(new PaintingModifyMessage(this), player);
        super.onStartedTrackingBy(player);
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(Items.PAINTING);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!player.world.isClient) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.EDITOR, getId()), (ServerPlayerEntity)player);
            return ActionResult.CONSUME;
        } else {
            return ActionResult.PASS;
        }
    }

    public Identifier getMotive() {
        return motive;
    }

    public void setMotive(Identifier motive) {
        this.motive = motive;
        updateAttachmentPosition();
    }

    public Identifier getFrame() {
        return frame;
    }

    public void setFrame(Identifier frame) {
        this.frame = frame;
    }

    public Identifier getMaterial() {
        return material;
    }

    public void setMaterial(Identifier material) {
        this.material = material;
    }
}
