package immersive_paintings.client.render.entity.renderer;

import immersive_paintings.entity.ImmersiveGraffitiEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class ImmersiveGraffitiEntityRenderer extends ImmersivePaintingEntityRenderer<ImmersiveGraffitiEntity> {
    public ImmersiveGraffitiEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    protected boolean isTranslucent() {
        return true;
    }
}
