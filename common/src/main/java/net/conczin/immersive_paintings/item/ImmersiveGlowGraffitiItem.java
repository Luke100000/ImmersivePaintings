package net.conczin.immersive_paintings.item;

import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.conczin.immersive_paintings.registration.Entities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class ImmersiveGlowGraffitiItem extends ImmersivePaintingItem {
    public ImmersiveGlowGraffitiItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    protected EntityType<? extends ImmersivePaintingEntity> getEntityType() {
        return Entities.GLOW_GRAFFITI;
    }
}
