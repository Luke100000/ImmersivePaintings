package net.conczin.immersive_paintings.fabric.resources;

import net.conczin.immersive_paintings.resources.ObjectLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class FabricObjectLoader extends ObjectLoader implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }
}
