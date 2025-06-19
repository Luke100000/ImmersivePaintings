package net.conczin.immersive_paintings.item;

import net.conczin.immersive_paintings.registration.Entities;
import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.world.entity.EntityType;

public class ImmersiveGlowGraffitiItem extends ImmersivePaintingItem {
    @Override
    protected EntityType<? extends ImmersivePaintingEntity> getEntityType() {
        return Entities.GLOW_GRAFFITI;
    }
}
