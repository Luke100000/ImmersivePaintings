package net.conczin.immersive_paintings.compat;

import java.util.List;
import java.awt.image.BufferedImage;

import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.conczin.immersive_paintings.network.payload.c2s.PaintingRegisterPayload;
import net.conczin.immersive_paintings.painting.Painting;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class XercaPaintCompat {
    public static boolean interactWithPainting(ImmersivePaintingEntity painting, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ResourceLocation location = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (location.getNamespace().equals("xercapaint")) {
            int w = 0, h = 0;
            switch (location.getPath()) {
                case "item_canvas" -> {
                    w = 16;
                    h = 16;
                }
                case "item_canvas_large" -> {
                    w = 32;
                    h = 32;
                }
                case "item_canvas_long" -> {
                    w = 32;
                    h = 16;
                }
                case "item_canvas_tall" -> {
                    w = 16;
                    h = 32;
                }
            }

            DataComponentType<?> CANVAS_PIXELS = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("xercapaint", "canvas_pixels"));            
            
            if (w > 0 && stack.has(CANVAS_PIXELS)) {
                DataComponentMap map = stack.getComponents();

                // convert
                List<Integer> pixels = (List<Integer>)map.get(CANVAS_PIXELS);
                BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                int x = 0, y = 0;
                for (int n : pixels) {
                    bufferedImage.setRGB(x, y, n);
                    x++;
                    if (x >= w) {
                        x = 0;
                        y++;
                    }
                }

                DataComponentType<?> CANVAS_TITLE = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("xercapaint", "canvas_title"));
                DataComponentType<?> CANVAS_AUTHOR = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("xercapaint", "canvas_author"));

                // title
                String title = map.has(CANVAS_TITLE) ? (String)stack.getComponents().get(CANVAS_TITLE) : "Unnamed Painting #" + player.getRandom().nextInt(1048576);
                String author = map.has(CANVAS_AUTHOR) ? (String)stack.getComponents().get(CANVAS_AUTHOR) : "Unknown Author";

                // upload
                // Lazy server-side way of handling this, but it allows us to skip the LazyNetwork delay
                ResourceLocation identifier = (new PaintingRegisterPayload(w / 16, h / 16, 16, title, false, false, false)).handle(player, bufferedImage, author, Painting.Type.XERCA);

                // apply
                if (identifier != null)
                    painting.setMotive(identifier);

                return true;
            }
        }

        return false;
    }
}
