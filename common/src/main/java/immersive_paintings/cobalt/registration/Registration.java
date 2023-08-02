package immersive_paintings.cobalt.registration;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public class Registration {
    private static Impl INSTANCE;

    public static <T> T registerEntityRenderer(Registry<? super T> registry, Identifier id, T obj) {
        return INSTANCE.register(registry, id, obj);
    }

    public static <T extends Entity> void registerEntityRenderer(EntityType<?> type, EntityRendererFactory<T> constructor) {
        //noinspection unchecked
        INSTANCE.registerEntityRenderer((EntityType<T>) type, constructor);
    }

    public static class ObjectBuilders {
        public static class ItemGroups {
            public static ItemGroup create(Identifier id, Supplier<ItemStack> icon) {
                return INSTANCE.itemGroup(id, icon);
            }
        }
    }

    public abstract static class Impl {
        protected Impl() {
            INSTANCE = this;
        }

        public abstract <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererFactory<T> constructor);

        public abstract <T> T register(Registry<? super T> registry, Identifier id, T obj);

        public abstract ItemGroup itemGroup(Identifier id, Supplier<ItemStack> icon);
    }
}
