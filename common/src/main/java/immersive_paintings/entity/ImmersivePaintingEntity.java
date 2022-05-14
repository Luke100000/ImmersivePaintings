package immersive_paintings.entity;

import immersive_paintings.Entities;
import immersive_paintings.client.render.entity.gui.ImmersivePaintingScreen;
import immersive_paintings.network.s2c.ImmersivePaintingSpawnMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
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
    private int width;
    private int height;
    private Identifier motive;

    public ImmersivePaintingEntity(World world, BlockPos pos, Direction direction) {
        super(Entities.PAINTING, world, pos);

        width = 3;
        height = 3;

        setFacing(direction);
    }

    public ImmersivePaintingEntity(EntityType<Entity> type, World world) {
        super(EntityType.PAINTING, world);
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
        return new ImmersivePaintingSpawnMessage(this);
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
        MinecraftClient.getInstance().setScreen(new ImmersivePaintingScreen(getUuid()));
        return ActionResult.CONSUME;
    }
}
