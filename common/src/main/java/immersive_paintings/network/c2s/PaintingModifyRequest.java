package immersive_paintings.network.c2s;

import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.network.PaintingDataMessage;
import immersive_paintings.network.s2c.PaintingModifyMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class PaintingModifyRequest extends PaintingDataMessage {
    public PaintingModifyRequest(ImmersivePaintingEntity painting) {
        super(painting);
    }

    public PaintingModifyRequest(PacketByteBuf b) {
        super(b);
    }

    @Override
    public void receive(PlayerEntity e) {
        Entity entity = e.world.getEntityById(getEntityId());
        if (entity instanceof ImmersivePaintingEntity) {
            ImmersivePaintingEntity painting = (ImmersivePaintingEntity)entity;
            painting.setMotive(getMotive());
            painting.setFrame(getFrame());
            painting.setMaterial(getMaterial());
            e.world.getPlayers().forEach(p -> NetworkHandler.sendToPlayer(new PaintingModifyMessage(painting), (ServerPlayerEntity)p));
        }
    }
}
