package net.conczin.immersive_paintings.entity;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.util.Utils.RegisterHelper;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class Entities {
    public static final EntityType<ImmersivePaintingEntity> PAINTING = createEntityType(ImmersivePaintingEntity::new, "painting");
    public static final EntityType<ImmersiveGlowPaintingEntity> GLOW_PAINTING = createEntityType(ImmersiveGlowPaintingEntity::new, "glow_painting");
    public static final EntityType<ImmersiveGraffitiEntity> GRAFFITI = createEntityType(ImmersiveGraffitiEntity::new, "graffiti");
    public static final EntityType<ImmersiveGlowGraffitiEntity> GLOW_GRAFFITI = createEntityType(ImmersiveGlowGraffitiEntity::new, "glow_graffiti");

    public static final EntityDataSerializer<ResourceLocation> TRACKED_IDENTIFIER = EntityDataSerializer.forValueType(ResourceLocation.STREAM_CODEC);

    private static <T extends ImmersivePaintingEntity> EntityType<T> createEntityType(EntityType.EntityFactory<T> factory, String name) {
        return EntityType.Builder.of(factory, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .clientTrackingRange(10)
            .updateInterval(Integer.MAX_VALUE)
            .fireImmune()
            .build(name);
    }

    public static void registerEntities(RegisterHelper<EntityType<?>> helper) {
        helper.register(Main.locate("painting"), PAINTING);
        helper.register(Main.locate("glow_painting"), GLOW_PAINTING);
        helper.register(Main.locate("graffiti"), GRAFFITI);
        helper.register(Main.locate("glow_graffiti"), GLOW_GRAFFITI);
    }

    public static void registerEntitySerializers(RegisterHelper<EntityDataSerializer<?>> helper) {
        helper.register(Main.locate("resource"), TRACKED_IDENTIFIER);
    }
}
