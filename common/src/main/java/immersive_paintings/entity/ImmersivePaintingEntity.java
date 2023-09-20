package immersive_paintings.entity;

import immersive_paintings.*;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.compat.XercaPaintCompat;
import immersive_paintings.network.s2c.OpenGuiRequest;
import immersive_paintings.network.s2c.PaintingModifyMessage;
import immersive_paintings.resources.ClientPaintingManager;
import immersive_paintings.resources.Painting;
import immersive_paintings.resources.ServerPaintingManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
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
    private int width = 1;
    private int height = 1;

    public ImmersivePaintingEntity(World world, BlockPos pos, Direction direction, int rotation) {
        super(Entities.PAINTING.get(), world, pos);

        setFacing(direction, rotation);
    }

    public ImmersivePaintingEntity(EntityType<Entity> type, World world) {
        super(type, world);
    }

    public ImmersivePaintingEntity(EntityType<?> painting, World world, BlockPos pos) {
        super(painting, world, pos);
    }

    @Override
    public int getWidthPixels() {
        return width * 16;
    }

    @Override
    public int getHeightPixels() {
        return height * 16;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putString("Motive", motive.toString());
        nbt.putString("Frame", frame.toString());
        nbt.putString("Material", material.toString());
        super.writeCustomDataToNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.motive = new Identifier(nbt.getString("Motive"));
        this.frame = new Identifier(nbt.getString("Frame"));
        this.material = new Identifier(nbt.getString("Material"));
        this.updateMotiveDimensions();
        super.readCustomDataFromNbt(nbt);
    }

    public Item getDrop() {
        return Items.PAINTING.get();
    }

    @Override
    public void onBreak(@Nullable Entity entity) {
        if (!getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            return;
        }
        playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0f, 1.0f);
        if (entity instanceof PlayerEntity playerEntity && (playerEntity.getAbilities().creativeMode)) {
            return;
        }
        dropItem(getDrop());
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
        BlockPos blockPos = this.attachmentPos.add(BlockPos.ofFloored(x - this.getX(), y - this.getY(), z - this.getZ()));
        this.setPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        ServerDataManager.playerRequestedImages(player);
        NetworkHandler.sendToPlayer(new PaintingModifyMessage(this), player);
        super.onStartedTrackingBy(player);
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(getDrop());
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!player.getWorld().isClient) {
            if (!XercaPaintCompat.interactWithPainting(this, player, hand)) {
                Config config = Config.getInstance();
                NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.EDITOR, getId(), config.minPaintingResolution, config.maxPaintingResolution, config.showOtherPlayersPaintings), (ServerPlayerEntity) player);
            }
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
        updateMotiveDimensions();
        updateAttachmentPosition();
    }

    public void updateMotiveDimensions() {
        Painting painting;
        if (getWorld().isClient) {
            painting = ClientPaintingManager.getPainting(motive);
        } else {
            painting = ServerPaintingManager.getPainting(motive);
        }
        if (painting != null) {
            this.width = painting.width;
            this.height = painting.height;
        }
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

    public boolean isGraffiti() {
        return false;
    }
}
