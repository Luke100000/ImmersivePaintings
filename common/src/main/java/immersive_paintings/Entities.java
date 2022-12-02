package immersive_paintings;

import immersive_paintings.cobalt.registration.Registration;
import immersive_paintings.entity.ImmersiveGlowPaintingEntity;
import immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;



public interface Entities {
    EntityType<Entity> PAINTING = register("painting", EntityType.Builder
            .create(ImmersivePaintingEntity::new, SpawnGroup.MISC)
            .setDimensions(0.5f, 0.5f)
            .maxTrackingRange(10)
            .trackingTickInterval(Integer.MAX_VALUE)
            .makeFireImmune()
    );

    EntityType<Entity> GLOW_PAINTING = register("glow_painting", EntityType.Builder
            .create(ImmersiveGlowPaintingEntity::new, SpawnGroup.MISC)
            .setDimensions(0.5f, 0.5f)
            .maxTrackingRange(10)
            .trackingTickInterval(Integer.MAX_VALUE)
            .makeFireImmune()
    );

    static void bootstrap() {

    }

    static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        Identifier id = new Identifier(Main.MOD_ID, name);
        return Registration.registerEntityRenderer(Registry.ENTITY_TYPE, id, builder.build(id.toString()));
    }
}
