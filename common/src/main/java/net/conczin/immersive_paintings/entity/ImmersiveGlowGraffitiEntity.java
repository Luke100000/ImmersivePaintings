package net.conczin.immersive_paintings.entity;

import net.conczin.immersive_paintings.registry.Item;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ImmersiveGlowGraffitiEntity extends ImmersivePaintingEntity {
    public ImmersiveGlowGraffitiEntity(EntityType<? extends ImmersivePaintingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isGlowing() {
        return true;
    }

    @Override
    public boolean isGraffiti() {
        return true;
    }

    @Override
    public net.minecraft.world.item.Item getItem() {
        return Item.GLOW_GRAFFITI;
    }
}
