package net.conczin.immersive_paintings.painting;

import net.conczin.immersive_paintings.network.Network;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingListPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class PlayerManager {
    private static final Set<UUID> sent = new HashSet<>();

    public static void playerLoggedOff(ServerPlayer player) {
        sent.remove(player.getUUID());
    }

    public static void playerRequestedImages(ServerPlayer player) {
        if (!sent.contains(player.getUUID())) {
            HashMap<ResourceLocation, Optional<Painting>> paintings = new HashMap<>();
            ServerPaintingManager.getDatapackPaintings().forEach((id, entry) -> paintings.put(id, Optional.of(entry.getKey())));
            ServerPaintingManager.getCustomPaintings(player.getServer()).forEach((id, p) -> paintings.put(id, Optional.of(p)));

            List<PaintingListPayload> payloads = PaintingListPayload.splitPaintings(paintings, true);
            payloads.forEach(payload -> Network.sendToClient(player, payload));
            sent.add(player.getUUID());
        }
    }
}
