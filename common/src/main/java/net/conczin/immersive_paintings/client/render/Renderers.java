package net.conczin.immersive_paintings.client.render;

import net.conczin.immersive_paintings.entity.Entities;
import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;

public class Renderers {
    public interface Handler {
        <T extends ImmersivePaintingEntity> void handle(EntityType<T> entityType, EntityRendererProvider<T> factory);
    }

    public static void register(Handler handler) {
        handler.handle(Entities.PAINTING, ImmersivePaintingEntityRenderer::new);
        handler.handle(Entities.GLOW_PAINTING, ImmersivePaintingEntityRenderer::new);
        handler.handle(Entities.GRAFFITI, ImmersivePaintingEntityRenderer::new);
        handler.handle(Entities.GLOW_GRAFFITI, ImmersivePaintingEntityRenderer::new);
    }
}
