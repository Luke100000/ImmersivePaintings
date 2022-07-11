package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.network.PaintingDataMessage;
import net.minecraft.entity.player.PlayerEntity;

import java.io.Serial;

public class PaintingModifyMessage extends PaintingDataMessage {
    @Serial
    private static final long serialVersionUID = -1508169419753605236L;

    public PaintingModifyMessage(ImmersivePaintingEntity painting) {
        super(painting);
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handlePaintingModifyMessage(this);
    }
}
