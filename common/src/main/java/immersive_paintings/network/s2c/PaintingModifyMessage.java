package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.network.PaintingDataMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class PaintingModifyMessage extends PaintingDataMessage {
    public PaintingModifyMessage(ImmersivePaintingEntity painting) {
        super(painting);
    }

    public PaintingModifyMessage(PacketByteBuf b) {
        super(b);
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handlePaintingModifyMessage(this);
    }
}
