package net.conczin.immersive_paintings.mixin;

import net.conczin.immersive_paintings.registry.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public class EntityTypeMixin {
    // Check for our custom type and disable trackDelta to avoid a lot of unnecessary updates.
    // This is the same logic the vanilla paintings use.
    @Inject(method = "trackDeltas()Z", at = @At("RETURN"), cancellable = true)
    private void trackDelta(CallbackInfoReturnable<Boolean> cir) {
        EntityType<?> thisObject = (EntityType<?>)(Object)this;
        cir.setReturnValue(cir.getReturnValue()
                && thisObject != Entity.PAINTING
                && thisObject != Entity.GLOW_PAINTING
                && thisObject != Entity.GRAFFITI
                && thisObject != Entity.GLOW_GRAFFITI
        );
    }
}
