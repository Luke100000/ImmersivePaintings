package immersive_paintings;

import immersive_paintings.cobalt.registration.Registration;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface Entities {

    static void bootstrap() {

    }

    static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        Identifier id = new Identifier(Main.MOD_ID, name);
        return Registration.registerEntityRenderer(Registry.ENTITY_TYPE, id, builder.build(id.toString()));
    }
}
