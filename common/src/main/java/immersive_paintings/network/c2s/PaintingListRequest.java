package immersive_paintings.network.c2s;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.s2c.PaintingListResponse;
import immersive_paintings.resources.Paintings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class PaintingListRequest implements Message {
    @Override
    public void receive(PlayerEntity e) {
        //todo
        //NetworkHandler.sendToPlayer(new PaintingListResponse(Paintings.paintings.values()), (ServerPlayerEntity)e);
    }
}
