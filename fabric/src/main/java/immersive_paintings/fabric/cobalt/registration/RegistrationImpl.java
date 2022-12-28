package immersive_paintings.fabric.cobalt.registration;

import immersive_paintings.cobalt.registration.Registration;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class RegistrationImpl extends Registration.Impl {
    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj) {
        T register = Registry.register(registry, id, obj.get());
        return () -> register;
    }

    @Override
    public <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererFactory<T> constructor) {
        EntityRendererRegistry.register(type, constructor);
    }
}
