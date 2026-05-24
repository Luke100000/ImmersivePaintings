package net.conczin.immersive_paintings.registry;

import net.conczin.immersive_paintings.item.*;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.BiConsumer;

public class Item {
    public static final ImmersivePaintingItem PAINTING = new ImmersivePaintingItem();
    public static final ImmersiveGlowPaintingItem GLOW_PAINTING = new ImmersiveGlowPaintingItem();
    public static final ImmersiveGraffitiItem GRAFFITI = new ImmersiveGraffitiItem();
    public static final ImmersiveGlowGraffitiItem GLOW_GRAFFITI = new ImmersiveGlowGraffitiItem();

    public static <T extends ImmersivePaintingItem> void register(BiConsumer<Identifier, ImmersivePaintingItem> consumer) {
        consumer.accept(ImmersivePaintingItem.KEY.identifier(), PAINTING);
        consumer.accept(ImmersiveGlowPaintingItem.KEY.identifier(), GLOW_PAINTING);
        consumer.accept(ImmersiveGraffitiItem.KEY.identifier(), GRAFFITI);
        consumer.accept(ImmersiveGlowGraffitiItem.KEY.identifier(), GLOW_GRAFFITI);
    }

    public static List<net.minecraft.world.item.Item> getItems() {
        return List.of(PAINTING.asItem(), GLOW_PAINTING.asItem(),GRAFFITI.asItem(),GLOW_GRAFFITI.asItem());
    }
}
