package net.conczin.immersive_paintings.compat;

import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.mixin.SavedDataStorageAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ZetterCompat implements PaintingImporter {
    private static final Identifier PAINTING_ITEM = Identifier.fromNamespaceAndPath("zetter", "painting");

    @Override
    public Result read(ItemStack stack, ServerPlayer player) {
        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(PAINTING_ITEM)) return null;

        CustomData itemData = stack.get(DataComponents.CUSTOM_DATA);
        if (itemData == null) return null;

        String code = itemData.copyTag().getStringOr("PaintingCode", "");
        if (!code.matches("zetter_painting_[0-9]+")) return null;

        SavedDataStorage storage = player.level().getServer().overworld().getDataStorage();
        SavedDataStorageAccessor accessor = (SavedDataStorageAccessor) storage;
        Identifier id = Identifier.withDefaultNamespace(code);
        CompoundTag data = null;

        // Read unsaved artwork too, without replacing Zetter's own cached SavedData instance.
        for (var entry : accessor.immersivePaintings$getCache().entrySet()) {
            if (entry.getKey().id().equals(id) && entry.getValue().isPresent()) {
                data = accessor.immersivePaintings$encode(entry.getKey(), entry.getValue().get(),
                        player.registryAccess().createSerializationContext(NbtOps.INSTANCE)).getCompoundOrEmpty("data");
                break;
            }
        }

        if (data == null) {
            try {
                data = storage.readTagFromDisk(accessor.immersivePaintings$getDataFile(id), DataFixTypes.LEVEL,
                        SharedConstants.getCurrentVersion().dataVersion().version()).getCompoundOrEmpty("data");
            } catch (IOException e) {
                return null;
            }
        }

        int ordinal = data.getIntOr("resolution", -1);
        if (data.getBooleanOr("Banned", false) || ordinal < 0 || ordinal > 2) return null;

        int resolution = 16 << ordinal;
        int width = data.getIntOr("width", 0);
        int height = data.getIntOr("height", 0);
        byte[] colors = data.getByteArray("color").orElse(new byte[0]);
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

        return new Result(image, resolution, data.getStringOr("title", ""), data.getStringOr("author_name", ""), Painting.Type.PAINTING);
    }
}
