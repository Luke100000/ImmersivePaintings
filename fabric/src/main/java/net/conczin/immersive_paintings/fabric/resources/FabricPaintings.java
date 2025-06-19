package net.conczin.immersive_paintings.fabric.resources;

import net.conczin.immersive_paintings.resources.PaintingsLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class FabricPaintings extends PaintingsLoader implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }
}
