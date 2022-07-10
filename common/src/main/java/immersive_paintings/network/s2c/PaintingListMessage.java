package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.resources.Painting;
import immersive_paintings.resources.ServerPaintingManager;
import immersive_paintings.util.SerializableNbt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PaintingListMessage implements Message {
    private static final long serialVersionUID = 2240894186943896681L;

    private final Map<String, SerializableNbt> paintings = new HashMap<>();
    private final boolean clear;

    public PaintingListMessage() {
        //datapack paintings
        for (Map.Entry<Identifier, Painting> entry : ServerPaintingManager.getDatapackPaintings().entrySet()) {
            this.paintings.put(entry.getKey().toString(), new SerializableNbt(entry.getValue().toNbt()));
        }

        //custom paintings
        for (Map.Entry<Identifier, Painting> entry : ServerPaintingManager.get().getCustomServerPaintings().entrySet()) {
            this.paintings.put(entry.getKey().toString(), new SerializableNbt(entry.getValue().toNbt()));
        }

        clear = true;
    }

    public PaintingListMessage(Identifier identifier, Painting painting) {
        this.paintings.put(identifier.toString(), painting == null ? null : new SerializableNbt(painting.toNbt()));
        clear = false;
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handlePaintingListResponse(this);
    }

    public Map<Identifier, Painting> getPaintings() {
        Map<Identifier, Painting> paintings = new HashMap<>();
        for (Map.Entry<String, SerializableNbt> entry : this.paintings.entrySet()) {
            Identifier identifier = new Identifier(entry.getKey());
            if (entry.getValue() == null) {
                paintings.put(identifier, null);
            } else {
                paintings.put(identifier, Painting.fromNbt(entry.getValue().getNbt()));
            }
        }
        return paintings;
    }

    public boolean shouldClear() {
        return clear;
    }
}
