package immersive_paintings.network.c2s;

import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.network.PaintingDataMessage;
import immersive_paintings.network.s2c.PaintingModifyMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class PaintingModifyRequest extends PaintingDataMessage {
    public PaintingModifyRequest(ImmersivePaintingEntity painting) {
        super(painting);
    }

    @Override
    public void receive(PlayerEntity e) {
        Entity entity = e.world.getEntityById(getEntityId());
        if (entity instanceof ImmersivePaintingEntity painting) {
            painting.setMotive(getMotive());
            painting.setFrame(getFrame());
            painting.setMaterial(getMaterial());
            e.getWorld().getPlayers().forEach(p -> NetworkHandler.sendToPlayer(new PaintingModifyMessage(painting), (ServerPlayerEntity)p));
        }
    }
}
