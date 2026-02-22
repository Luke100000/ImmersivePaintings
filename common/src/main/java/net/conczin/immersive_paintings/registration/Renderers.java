package net.conczin.immersive_paintings.registration;

import net.conczin.immersive_paintings.client.render.ImmersivePaintingEntityRenderer;
import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;

public class Renderers {
    public static void register(Handler handler) {
        handler.handle(Entities.PAINTING, provider());
        handler.handle(Entities.GLOW_PAINTING, provider());
        handler.handle(Entities.GRAFFITI, provider());
        handler.handle(Entities.GLOW_GRAFFITI, provider());
    }

    @SuppressWarnings("unchecked")
    private static <T extends ImmersivePaintingEntity> EntityRendererProvider<T> provider() {
        return ctx -> (EntityRenderer<T, ?>) new ImmersivePaintingEntityRenderer(ctx);
    }

    public interface Handler {
        <T extends ImmersivePaintingEntity> void handle(EntityType<T> entityType, EntityRendererProvider<T> factory);
    }
}
