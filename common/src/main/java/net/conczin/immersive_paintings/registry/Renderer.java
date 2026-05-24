package net.conczin.immersive_paintings.registry;

import net.conczin.immersive_paintings.client.render.ImmersivePaintingRenderer;
import net.conczin.immersive_paintings.entity.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;

public class Renderer {
    public static void register(Handler handler) {
        handler.handle(Entity.PAINTING, ImmersivePaintingRenderer::new);
        handler.handle(Entity.GLOW_PAINTING, ImmersivePaintingRenderer::new);
        handler.handle(Entity.GRAFFITI, ImmersivePaintingRenderer::new);
        handler.handle(Entity.GLOW_GRAFFITI, ImmersivePaintingRenderer::new);
    }

    public interface Handler {
        <T extends ImmersivePaintingEntity> void handle(EntityType<T> entityType, EntityRendererProvider<T> factory);
    }
}
