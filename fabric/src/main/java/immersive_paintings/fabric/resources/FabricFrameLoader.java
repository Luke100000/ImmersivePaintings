package immersive_paintings.fabric.resources;

import immersive_paintings.resources.FrameLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.util.Identifier;

public class FabricFrameLoader extends FrameLoader implements IdentifiableResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
