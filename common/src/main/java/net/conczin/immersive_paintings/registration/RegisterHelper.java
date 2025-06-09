package net.conczin.immersive_paintings.registration;

import net.minecraft.resources.ResourceLocation;

public interface RegisterHelper<T> {
    void register(ResourceLocation name, T value);
}
