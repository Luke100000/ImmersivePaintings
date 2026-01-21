package net.conczin.immersive_paintings.fabric.resources;

import net.conczin.immersive_paintings.resources.FrameLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;

public class FabricFrameLoader extends FrameLoader implements IdentifiableResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
