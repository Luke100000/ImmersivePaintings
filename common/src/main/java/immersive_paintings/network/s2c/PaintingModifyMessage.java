package immersive_paintings.network.s2c;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class PaintingModifyMessage implements Message {
    private final int id;
    private final String motive;

    public PaintingModifyMessage(ImmersivePaintingEntity painting) {
        this.id = painting.getId();
        this.motive = painting.getMotive().toString();
    }

    @Override
    public void receive(PlayerEntity e) {
        if (e.world.getEntityById(id) instanceof ImmersivePaintingEntity painting) {
            painting.setMotive(new Identifier(motive));
        }
    }
}
