package immersive_paintings.network.s2c;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.network.PaintingDataMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class PaintingModifyMessage extends PaintingDataMessage {
    public PaintingModifyMessage(ImmersivePaintingEntity painting) {
        super(painting);
    }

    @Override
    public void receive(PlayerEntity e) {
        if (e.world.getEntityById(getEntityId()) instanceof ImmersivePaintingEntity painting) {
            painting.setMotive(getMotive());
            painting.setFrame(getFrame());
            painting.setMaterial(getMaterial());
        }
    }
}
