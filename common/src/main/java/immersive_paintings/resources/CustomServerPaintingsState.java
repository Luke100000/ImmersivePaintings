package immersive_paintings.resources;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;

public class CustomServerPaintingsState extends PersistentState {

    final Map<Identifier, Painting> customServerPaintings = new HashMap<>();

    public static CustomServerPaintingsState fromNbt(NbtCompound nbt) {
        CustomServerPaintingsState c = new CustomServerPaintingsState();

        for (String key : nbt.getKeys()) {
            c.customServerPaintings.put(new Identifier(key), Painting.fromNbt(nbt.getCompound(key)));
        }

        return c;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound c = new NbtCompound();

        for (Map.Entry<Identifier, Painting> entry : customServerPaintings.entrySet()) {
            c.put(entry.getKey().toString(), entry.getValue().toNbt());
        }

        return c;
    }

    public Map<Identifier, Painting> getCustomServerPaintings() {
        return customServerPaintings;
    }

    public static PersistentState.Type<CustomServerPaintingsState> getPersistentStateType() {
        return new PersistentState.Type<>(CustomServerPaintingsState::new, CustomServerPaintingsState::fromNbt, null);
    }

}
