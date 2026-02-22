package net.conczin.immersive_paintings.registration;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.item.ImmersiveGlowGraffitiItem;
import net.conczin.immersive_paintings.item.ImmersiveGlowPaintingItem;
import net.conczin.immersive_paintings.item.ImmersiveGraffitiItem;
import net.conczin.immersive_paintings.item.ImmersivePaintingItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class Items {
    public static ImmersivePaintingItem PAINTING;
    public static ImmersiveGlowPaintingItem GLOW_PAINTING;
    public static ImmersiveGraffitiItem GRAFFITI;
    public static ImmersiveGlowGraffitiItem GLOW_GRAFFITI;

    private static Item.Properties props(Identifier id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
    }

    public static void registerItems(RegisterHelper<Item> helper) {
        Identifier paintingId = Main.locate("painting");
        Identifier glowPaintingId = Main.locate("glow_painting");
        Identifier graffitiId = Main.locate("graffiti");
        Identifier glowGraffitiId = Main.locate("glow_graffiti");

        PAINTING = new ImmersivePaintingItem(props(paintingId));
        GLOW_PAINTING = new ImmersiveGlowPaintingItem(props(glowPaintingId));
        GRAFFITI = new ImmersiveGraffitiItem(props(graffitiId));
        GLOW_GRAFFITI = new ImmersiveGlowGraffitiItem(props(glowGraffitiId));

        helper.register(paintingId, PAINTING);
        helper.register(glowPaintingId, GLOW_PAINTING);
        helper.register(graffitiId, GRAFFITI);
        helper.register(glowGraffitiId, GLOW_GRAFFITI);
    }

    public static final CreativeModeTab PAINTING_TAB = CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, -1)
            .title(Component.translatable("itemGroup.immersive_paintings"))
            .icon(() -> new ItemStack(PAINTING))
            .displayItems((params, output) -> {
                output.accept(PAINTING);
                output.accept(GLOW_PAINTING);
                output.accept(GRAFFITI);
                output.accept(GLOW_GRAFFITI);
            })
            .build();

    public static void registerCreativeTabs(RegisterHelper<CreativeModeTab> helper) {
        helper.register(Main.locate("paintings"), PAINTING_TAB);
    }
}
