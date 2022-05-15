package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.resources.Paintings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PaintingListResponse implements Message {
    private final Map<String, Paintings.PaintingData> paintings;

    public PaintingListResponse(Map<Identifier, Paintings.PaintingData> paintings) {
        this.paintings = new HashMap<>();
        for (Map.Entry<Identifier, Paintings.PaintingData> entry : paintings.entrySet()) {
            this.paintings.put(entry.getKey().toString(), entry.getValue());
        }
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handlePaintingListResponse(this);
    }

    public Map<Identifier, Paintings.PaintingData> getPaintings() {
        Map<Identifier, Paintings.PaintingData> paintings = new HashMap<>();
        for (Map.Entry<String, Paintings.PaintingData> entry : this.paintings.entrySet()) {
            paintings.put(new Identifier(entry.getKey()), entry.getValue());
        }
        return paintings;
    }
}
