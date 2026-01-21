package net.conczin.immersive_paintings.fabric.resources;

import net.conczin.immersive_paintings.resources.ObjectLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;

public class FabricObjectLoader extends ObjectLoader implements IdentifiableResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
