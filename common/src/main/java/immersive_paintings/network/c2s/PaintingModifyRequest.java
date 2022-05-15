package immersive_paintings.network.c2s;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.network.s2c.PaintingModifyMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PaintingModifyRequest implements Message {
    private final String identifier;
    private final int entityId;

    public PaintingModifyRequest(int id, Identifier identifier) {
        entityId = id;
        this.identifier = identifier.toString();
    }

    @Override
    public void receive(PlayerEntity e) {
        Entity entity = e.world.getEntityById(entityId);
        if (entity instanceof ImmersivePaintingEntity painting) {
            painting.setMotive(new Identifier(identifier));
            e.getWorld().getPlayers().forEach(p -> NetworkHandler.sendToPlayer(new PaintingModifyMessage(painting), (ServerPlayerEntity)p));
        }
    }
}
