package immersive_paintings;

import immersive_paintings.cobalt.registration.Registration;
import immersive_paintings.item.ImmersiveGlowPaintingItem;
import immersive_paintings.item.ImmersiveGraffitiItem;
import immersive_paintings.item.ImmersivePaintingItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public interface Items {
    Item PAINTING = register("painting", new ImmersivePaintingItem(baseProps()));
    Item GLOW_PAINTING = register("glow_painting", new ImmersiveGlowPaintingItem(baseProps()));
    Item GRAFFITI = register("graffiti", new ImmersiveGraffitiItem(baseProps()));

    static Item register(String name, Item item) {
        return Registration.register(Registry.ITEM, Main.locate(name), item);
    }

    static void bootstrap() {
    }

    static Item.Settings baseProps() {
        return new Item.Settings().group(ItemGroups.GROUP);
    }
}
