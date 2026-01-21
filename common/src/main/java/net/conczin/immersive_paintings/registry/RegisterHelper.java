package net.conczin.immersive_paintings.registry;

import net.minecraft.resources.Identifier;

public interface RegisterHelper<T> {
    void register(Identifier name, T value);
}
