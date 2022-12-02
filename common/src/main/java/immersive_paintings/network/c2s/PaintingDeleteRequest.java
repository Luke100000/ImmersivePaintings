package immersive_paintings.network.c2s;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.s2c.PaintingListMessage;
import immersive_paintings.resources.ServerPaintingManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.io.Serial;
import java.util.Objects;

public class PaintingDeleteRequest implements Message {
    @Serial
    private static final long serialVersionUID = -4122382267250199065L;

    private final String identifier;

    public PaintingDeleteRequest(Identifier identifier) {
        this.identifier = identifier.toString();
    }

    @Override
    public void receive(PlayerEntity e) {
        Identifier identifier = new Identifier(this.identifier);

        if (ServerPaintingManager.get().getCustomServerPaintings().get(identifier).author.equals(e.getGameProfile().getName()) || e.hasPermissionLevel(4)) {
            Main.LOGGER.info(String.format("Player %s deleted painting %s.", e, identifier));
        } else {
            Main.LOGGER.warn(String.format("Player %s tried to delete an image they does not own.", e));
            return;
        }

        ServerPaintingManager.deregisterPainting(identifier);

        //update clients
        for (ServerPlayerEntity player : Objects.requireNonNull(e.getServer()).getPlayerManager().getPlayerList()) {
            NetworkHandler.sendToPlayer(new PaintingListMessage(identifier, null), player);
        }
    }
}
