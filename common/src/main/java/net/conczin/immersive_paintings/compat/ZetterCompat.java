package net.conczin.immersive_paintings.compat;

import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.mixin.DimensionDataStorageAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ZetterCompat implements PaintingImporter {
    private static final ResourceLocation PAINTING_ITEM = ResourceLocation.fromNamespaceAndPath("zetter", "painting");

    @Override
    public Result read(ItemStack stack, ServerPlayer player) {
        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(PAINTING_ITEM)) return null;

        CustomData itemData = stack.get(DataComponents.CUSTOM_DATA);
        if (itemData == null) return null;

        String code = itemData.copyTag().getString("PaintingCode");
        if (!code.matches("zetter_painting_[0-9]+")) return null;

        DimensionDataStorage storage = player.getServer().overworld().getDataStorage();
        SavedData cached = ((DimensionDataStorageAccessor) storage).immersivePaintings$getCache().get(code);
        CompoundTag data;
        // Read unsaved artwork too, without replacing Zetter's own cached SavedData instance.
        if (cached != null) {
            data = cached.save(new CompoundTag(), player.registryAccess());
        } else {
            try {
                data = storage.readTagFromDisk(code, DataFixTypes.LEVEL, SharedConstants.getCurrentVersion().getDataVersion().getVersion()).getCompound("data");
            } catch (IOException e) {
                return null;
            }
        }

        int ordinal = data.getInt("resolution");
        if (data.getBoolean("Banned") || ordinal < 0 || ordinal > 2) return null;

        int resolution = 16 << ordinal;
        int width = data.getInt("width");
        int height = data.getInt("height");
        byte[] colors = data.getByteArray("color");
        if (width < resolution || width > 16 * resolution || height < resolution || height > 16 * resolution ||
            width % resolution != 0 || height % resolution != 0 || colors.length != (long) width * height * 4) {
            return null;
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ByteBuffer pixels = ByteBuffer.wrap(colors);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, pixels.getInt());
            }
        }

        return new Result(image, resolution, data.getString("title"), data.getString("author_name"), Painting.Type.PAINTING);
    }
}
