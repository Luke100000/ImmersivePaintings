package net.conczin.immersive_paintings.registry;

import net.minecraft.resources.ResourceLocation;

public interface RegisterHelper<T> {
    void register(ResourceLocation name, T value);
}
