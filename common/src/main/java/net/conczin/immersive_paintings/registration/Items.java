package net.conczin.immersive_paintings.registration;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.item.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class Items {
    public static final ImmersivePaintingItem PAINTING = new ImmersivePaintingItem();
    public static final ImmersiveGlowPaintingItem GLOW_PAINTING = new ImmersiveGlowPaintingItem();
    public static final ImmersiveGraffitiItem GRAFFITI = new ImmersiveGraffitiItem();
    public static final ImmersiveGlowGraffitiItem GLOW_GRAFFITI = new ImmersiveGlowGraffitiItem();

    public static final CreativeModeTab PAINTING_TAB = CreativeModeTab.builder(null, -1)
            .title(Component.translatable("itemGroup.immersive_paintings"))
            .icon(() -> new ItemStack(PAINTING))
            .displayItems((params, output) -> {
                output.accept(PAINTING);
                output.accept(GLOW_PAINTING);
                output.accept(GRAFFITI);
                output.accept(GLOW_GRAFFITI);
            })
            .build();

    public static void registerItems(RegisterHelper<Item> helper) {
        helper.register(Main.locate("painting"), PAINTING);
        helper.register(Main.locate("glow_painting"), GLOW_PAINTING);
        helper.register(Main.locate("graffiti"), GRAFFITI);
        helper.register(Main.locate("glow_graffiti"), GLOW_GRAFFITI);
    }

    public static void registerCreativeTabs(RegisterHelper<CreativeModeTab> helper) {
        helper.register(Main.locate("paintings"), PAINTING_TAB);
    }
}
