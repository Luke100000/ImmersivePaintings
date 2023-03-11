package immersive_paintings.client.render.entity.renderer;

import immersive_paintings.entity.ImmersiveGlowPaintingEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class ImmersiveGlowPaintingEntityRenderer extends ImmersivePaintingEntityRenderer<ImmersiveGlowPaintingEntity> {
    public ImmersiveGlowPaintingEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    protected int getLight(int light) {
        return LightmapTextureManager.pack(
                (int)(LightmapTextureManager.getBlockLightCoordinates(light) * 0.25 + 15 * 0.75),
                LightmapTextureManager.getSkyLightCoordinates(light)
        );
    }

    @Override
    protected int getFrameLight(int light) {
        return LightmapTextureManager.pack(
                (int)(LightmapTextureManager.getBlockLightCoordinates(light) * 0.875 + 2),
                LightmapTextureManager.getSkyLightCoordinates(light)
        );
    }
}