package immersive_paintings;

import immersive_paintings.cobalt.registration.Registration;
import immersive_paintings.item.PaintingItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public interface Items {
    Item PAINTING = register("painting", new PaintingItem(baseProps()));

    static Item register(String name, Item item) {
        return Registration.registerEntityRenderer(Registry.ITEM, Main.locate(name), item);
    }

    static void bootstrap() {
    }

    static Item.Settings baseProps() {
        return new Item.Settings().group(ItemGroups.GROUP);
    }
}
