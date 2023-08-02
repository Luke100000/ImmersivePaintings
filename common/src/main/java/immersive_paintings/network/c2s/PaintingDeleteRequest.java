package immersive_paintings.network.c2s;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.s2c.PaintingListMessage;
import immersive_paintings.resources.ServerPaintingManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class PaintingDeleteRequest extends Message {
    private final String identifier;

    public PaintingDeleteRequest(Identifier identifier) {
        this.identifier = identifier.toString();
    }

    public PaintingDeleteRequest(PacketByteBuf b) {
        this.identifier = b.readString();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeString(identifier);
    }

    @Override
    public void receive(PlayerEntity e) {
        Identifier identifier = new Identifier(this.identifier);

        if (!ServerPaintingManager.get().getCustomServerPaintings().get(identifier).author.equals(e.getGameProfile().getName())) {
            Main.LOGGER.warn(String.format("Player %s tried to delete an image they do not own.", e));
            return;
        }

        ServerPaintingManager.deregisterPainting(identifier);

        //update clients
        for (ServerPlayerEntity player : Objects.requireNonNull(e.getServer()).getPlayerManager().getPlayerList()) {
            NetworkHandler.sendToPlayer(new PaintingListMessage(identifier, null), player);
        }
    }
}
