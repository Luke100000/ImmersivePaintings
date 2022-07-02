package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.network.PaintingDataMessage;
import net.minecraft.entity.player.PlayerEntity;

public class PaintingModifyMessage extends PaintingDataMessage {
    public PaintingModifyMessage(ImmersivePaintingEntity painting) {
        super(painting);
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handlePaintingModifyMessage(this);
    }
}
