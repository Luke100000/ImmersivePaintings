package net.conczin.immersive_paintings.registry;

import net.conczin.immersive_paintings.ImmersivePaintings;
import net.conczin.immersive_paintings.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.BiConsumer;

public class Entity {
    private static final Identifier PAINTING_LOCATION = ImmersivePaintings.locate("painting");
    private static final Identifier GLOW_PAINTING_LOCATION = ImmersivePaintings.locate("glow_painting");
    private static final Identifier GRAFFITI_LOCATION = ImmersivePaintings.locate("graffiti");
    private static final Identifier GLOW_GRAFFITI_LOCATION = ImmersivePaintings.locate("glow_graffiti");

    public static final EntityType<ImmersivePaintingEntity> PAINTING = createEntityType(ImmersivePaintingEntity::new, PAINTING_LOCATION);
    public static final EntityType<ImmersiveGlowPaintingEntity> GLOW_PAINTING = createEntityType(ImmersiveGlowPaintingEntity::new, GLOW_PAINTING_LOCATION);
    public static final EntityType<ImmersiveGraffitiEntity> GRAFFITI = createEntityType(ImmersiveGraffitiEntity::new, GRAFFITI_LOCATION);
    public static final EntityType<ImmersiveGlowGraffitiEntity> GLOW_GRAFFITI = createEntityType(ImmersiveGlowGraffitiEntity::new, GLOW_GRAFFITI_LOCATION);

    public static final EntityDataSerializer<Identifier> TRACKED_IDENTIFIER = EntityDataSerializer.forValueType(Identifier.STREAM_CODEC);

    private static <T extends ImmersivePaintingEntity> EntityType<T> createEntityType(EntityType.EntityFactory<T> factory, Identifier location) {
        return EntityType.Builder.of(factory, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .clientTrackingRange(10)
            .updateInterval(Integer.MAX_VALUE)
            .fireImmune()
            .build(ResourceKey.create(Registries.ENTITY_TYPE, location));
    }

    public static void register(BiConsumer<Identifier, EntityType<? extends ImmersivePaintingEntity>> consumer) {
        consumer.accept(PAINTING_LOCATION, PAINTING);
        consumer.accept(GLOW_PAINTING_LOCATION, GLOW_PAINTING);
        consumer.accept(GRAFFITI_LOCATION, GRAFFITI);
        consumer.accept(GLOW_GRAFFITI_LOCATION, GLOW_GRAFFITI);
    }

    public static void registerSerializers(BiConsumer<Identifier, EntityDataSerializer<?>> consumer) {
        consumer.accept(ImmersivePaintings.locate("resource_location"), TRACKED_IDENTIFIER);
    }
}
