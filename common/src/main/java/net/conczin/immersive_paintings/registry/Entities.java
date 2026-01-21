package net.conczin.immersive_paintings.registry;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class Entities {
    private static final Identifier PAINTING_LOCATION = Main.locate("painting");
    private static final Identifier GLOW_PAINTING_LOCATION = Main.locate("glow_painting");
    private static final Identifier GRAFFITI_LOCATION = Main.locate("graffiti");
    private static final Identifier GLOW_GRAFFITI_LOCATION = Main.locate("glow_graffiti");

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

    public static void registerEntities(RegisterHelper<EntityType<?>> helper) {
        helper.register(PAINTING_LOCATION, PAINTING);
        helper.register(GLOW_PAINTING_LOCATION, GLOW_PAINTING);
        helper.register(GRAFFITI_LOCATION, GRAFFITI);
        helper.register(GLOW_GRAFFITI_LOCATION, GLOW_GRAFFITI);
    }

    public static void registerEntitySerializers(RegisterHelper<EntityDataSerializer<?>> helper) {
        helper.register(Main.locate("resource"), TRACKED_IDENTIFIER);
    }
}
