package immersive_paintings.mixin;

import immersive_paintings.Entities;
import immersive_paintings.entity.ImmersiveGlowPaintingEntity;
import immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    public abstract ClientWorld getWorld();

    @Inject(method = "onEntitySpawn(Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;)V", at = @At("TAIL"))
    private void onEntitySpawnInject(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        if (packet.getEntityTypeId() == Entities.PAINTING || packet.getEntityTypeId() == Entities.GLOW_PAINTING) {
            ImmersivePaintingEntity entity;
            if (packet.getEntityTypeId() == Entities.PAINTING) {
                entity = new ImmersivePaintingEntity(Entities.PAINTING, getWorld());
            } else {
                entity = new ImmersiveGlowPaintingEntity(Entities.GLOW_PAINTING, getWorld());
            }
            int i = packet.getId();

            entity.updateTrackedPosition(packet.getX(), packet.getY(), packet.getZ());
            entity.refreshPositionAfterTeleport(packet.getX(), packet.getY(), packet.getZ());
            entity.pitch = (float)(packet.getPitch() * 360) / 256.0F;
            entity.yaw = (float)(packet.getYaw() * 360) / 256.0F;
            entity.setEntityId(i);
            entity.setUuid(packet.getUuid());
            getWorld().addEntity(i, entity);
        }
    }
}
