package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.resources.Paintings;
import immersive_paintings.resources.ServerPaintingManager;
import immersive_paintings.util.SerializableNbt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PaintingListMessage implements Message {
    private final Map<String, SerializableNbt> paintings;

    public PaintingListMessage() {
        this.paintings = new HashMap<>();

        //datapack paintings
        for (Map.Entry<Identifier, Paintings.PaintingData> entry : ServerPaintingManager.getDatapackPaintings().entrySet()) {
            this.paintings.put(entry.getKey().toString(), new SerializableNbt(entry.getValue().toNbt()));
        }

        //custom paintings
        for (Map.Entry<Identifier, Paintings.PaintingData> entry : ServerPaintingManager.get().getCustomServerPaintings().entrySet()) {
            this.paintings.put(entry.getKey().toString(), new SerializableNbt(entry.getValue().toNbt()));
        }
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handlePaintingListResponse(this);
    }

    public Map<Identifier, Paintings.PaintingData> getPaintings() {
        Map<Identifier, Paintings.PaintingData> paintings = new HashMap<>();
        for (Map.Entry<String, SerializableNbt> entry : this.paintings.entrySet()) {
            paintings.put(new Identifier(entry.getKey()), Paintings.PaintingData.fromNbt(entry.getValue().getNbt()));
        }
        return paintings;
    }
}
