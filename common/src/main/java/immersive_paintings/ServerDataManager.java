package immersive_paintings;

import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.s2c.PaintingListMessage;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class ServerDataManager {
    public static final Set<Integer> sent = new HashSet<>();

    public static void playerLoggedOff(ServerPlayerEntity player) {
        sent.remove(player.getEntityId());
    }

    public static void playerRequestedImages(ServerPlayerEntity player) {
        String playerName = player.getGameProfile().getName();
        if (!sent.contains(player.getEntityId())) {
            NetworkHandler.sendToPlayer(new PaintingListMessage(playerName), player);
            sent.add(player.getEntityId());
        }
    }
}
