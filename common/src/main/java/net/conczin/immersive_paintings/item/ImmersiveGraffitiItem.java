package net.conczin.immersive_paintings.item;

import net.conczin.immersive_paintings.ImmersivePaintings;
import net.conczin.immersive_paintings.registry.Entity;
import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class ImmersiveGraffitiItem extends ImmersivePaintingItem {
    public static final ResourceKey<Item> KEY = ResourceKey.create(Registries.ITEM, ImmersivePaintings.locate("graffiti"));

    public ImmersiveGraffitiItem() {
        super(new Properties().setId(KEY));
    }

    @Override
    protected EntityType<? extends ImmersivePaintingEntity> getEntityType() {
        return Entity.GRAFFITI;
    }
}
