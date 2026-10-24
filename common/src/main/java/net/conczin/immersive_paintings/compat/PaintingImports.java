package net.conczin.immersive_paintings.compat;

import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.PaintingRegistration;
import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.List;

public class PaintingImports {
    private static final List<PaintingImporter> IMPORTERS = List.of(new JoyOfPaintingCompat(), new ZetterCompat());

    public static boolean interact(ImmersivePaintingEntity entity, ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        for (PaintingImporter importer : IMPORTERS) {
            PaintingImporter.Result result = importer.read(stack, player);
            if (result == null) continue;

            Painting painting = new Painting(result.image().getWidth() / result.resolution(), result.image().getHeight() / result.resolution(),
                    result.resolution(), result.name(), result.author(), player.getUUID(), result.type(), EnumSet.noneOf(Painting.Flag.class), "");
            Identifier identifier = PaintingRegistration.register(player, result.image(), painting,
                    error -> player.sendOverlayMessage(Component.translatable("immersive_paintings.error." + error)));
            if (identifier != null) {
                entity.setMotive(identifier);
            }
            return true;
        }
        return false;
    }
}
