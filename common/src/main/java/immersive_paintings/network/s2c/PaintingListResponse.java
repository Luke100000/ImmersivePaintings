package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.resources.Paintings;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class PaintingListResponse implements Message {
    private final List<Paintings.PaintingData> paintings;

    public PaintingListResponse(List<Paintings.PaintingData> paintings) {
        this.paintings = paintings;
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handlePaintingListResponse(this);
    }

    public List<Paintings.PaintingData> getPaintings() {
        return paintings;
    }
}
