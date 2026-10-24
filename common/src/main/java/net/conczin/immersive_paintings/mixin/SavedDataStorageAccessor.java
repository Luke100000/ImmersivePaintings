package net.conczin.immersive_paintings.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@Mixin(SavedDataStorage.class)
public interface SavedDataStorageAccessor {
    @Accessor("cache")
    Map<SavedDataType<?>, Optional<SavedData>> immersivePaintings$getCache();

    @Invoker("getDataFile")
    Path immersivePaintings$getDataFile(Identifier id);

    @Invoker("encodeUnchecked")
    CompoundTag immersivePaintings$encode(SavedDataType<?> type, SavedData data, RegistryOps<Tag> ops);
}
