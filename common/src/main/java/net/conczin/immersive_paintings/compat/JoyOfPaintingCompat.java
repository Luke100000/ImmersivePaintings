package net.conczin.immersive_paintings.compat;

import net.conczin.immersive_paintings.Painting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.awt.image.BufferedImage;
import java.util.List;

public class JoyOfPaintingCompat implements PaintingImporter {
    @Override
    public Result read(ItemStack stack, ServerPlayer player) {
        Identifier location = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!location.getNamespace().equals("xercapaint")) return null;

        int width = switch (location.getPath()) {
            case "item_canvas", "item_canvas_tall", "item_canvas_glass", "item_canvas_glass_tall" -> 16;
            case "item_canvas_large", "item_canvas_long", "item_canvas_glass_large", "item_canvas_glass_long" -> 32;
            default -> 0;
        };
        int height = switch (location.getPath()) {
            case "item_canvas", "item_canvas_long", "item_canvas_glass", "item_canvas_glass_long" -> 16;
            case "item_canvas_large", "item_canvas_tall", "item_canvas_glass_large", "item_canvas_glass_tall" -> 32;
            default -> 0;
        };
        if (width == 0 || !(getComponent(stack, "canvas_pixels") instanceof List<?> pixels) || pixels.size() != width * height) {
            return null;
        }

        boolean glass = location.getPath().startsWith("item_canvas_glass");
        BufferedImage image = new BufferedImage(width, height, glass ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < pixels.size(); i++) {
            if (!(pixels.get(i) instanceof Integer color)) return null;
            image.setRGB(i % width, i / width, color);
        }

        Object title = getComponent(stack, "canvas_title");
        Object author = getComponent(stack, "canvas_author");
        return new Result(image, 16,
                title instanceof String text ? text : "Unnamed Painting #" + player.getRandom().nextInt(1048576),
                author instanceof String text ? text : "Unknown Author", Painting.Type.XERCA
        );
    }

    private static Object getComponent(ItemStack stack, String name) {
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(Identifier.fromNamespaceAndPath("xercapaint", name));
        return type == null ? null : stack.get(type);
    }
}
