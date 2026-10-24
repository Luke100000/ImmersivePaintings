package net.conczin.immersive_paintings.mixin;

import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DimensionDataStorage.class)
public interface DimensionDataStorageAccessor {
    @Accessor("cache")
    Map<String, SavedData> immersivePaintings$getCache();
}
