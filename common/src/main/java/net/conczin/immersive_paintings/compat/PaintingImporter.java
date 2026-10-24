package net.conczin.immersive_paintings.compat;

import net.conczin.immersive_paintings.Painting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

public interface PaintingImporter {
    @Nullable
    Result read(ItemStack stack, ServerPlayer player);

    record Result(BufferedImage image, int resolution, String name, String author, Painting.Type type) {
    }
}
