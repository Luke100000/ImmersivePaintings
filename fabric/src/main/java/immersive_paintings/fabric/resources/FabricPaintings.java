package immersive_paintings.fabric.resources;

import immersive_paintings.Main;
import immersive_paintings.resources.Paintings;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.util.Identifier;

public class FabricPaintings extends Paintings implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Main.locate("paintings");

    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
