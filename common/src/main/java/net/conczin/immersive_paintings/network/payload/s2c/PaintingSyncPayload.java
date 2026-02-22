package net.conczin.immersive_paintings.network.payload.s2c;

import net.conczin.immersive_paintings.ClientPaintingManager;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.client.gui.ImmersivePaintingScreen;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.registration.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.stream.Collectors;

public record PaintingSyncPayload(Map<Identifier, Optional<Painting>> paintings, boolean clear) implements ImmersivePayload {
    public static final Type<PaintingSyncPayload> TYPE = new Type<>(Main.locate("painting_list"));
    public static final StreamCodec<FriendlyByteBuf, PaintingSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.optional(Painting.STREAM_CODEC)), PaintingSyncPayload::paintings,
            ByteBufCodecs.BOOL, PaintingSyncPayload::clear,
            PaintingSyncPayload::new
    );

    public PaintingSyncPayload(Identifier identifier, Painting painting) {
        this(Map.of(identifier, Optional.ofNullable(painting)), false);
    }

    // Break paintings up into smaller batches if necessary to avoid packet limits
    // The interval is chosen to be an arbitrary number that feels like a good amount to send at a time
    public static List<PaintingSyncPayload> splitPaintings(Map<Identifier, Optional<Painting>> paintings, boolean clearFirst) {
        List<PaintingSyncPayload> paintingsList = new ArrayList<>();
        final int interval = Configs.COMMON.packetSplitInterval;

        Map<Identifier, Optional<Painting>> optionalPaintings = paintings
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (paintings.size() < interval) {
            Main.LOGGER.debug("Found {} paintings, < {}", paintings.size(), interval);
            paintingsList.add(new PaintingSyncPayload(optionalPaintings, clearFirst));
            return paintingsList;
        }

        Main.LOGGER.debug("Found {} paintings, >= {}, splitting into {} groups", paintings.size(), interval, paintings.size() / interval + Math.min(paintings.size() % interval, 1));

        int totalSize = paintings.size();
        int processed = 0;

        while (processed < totalSize) {
            int currentSize = Math.min(totalSize - processed, interval);
            Map<Identifier, Optional<Painting>> p = optionalPaintings
                    .entrySet().stream()
                    .skip(processed)
                    .limit(currentSize)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            paintingsList.add(new PaintingSyncPayload(p, processed == 0 && clearFirst));

            processed += currentSize;
        }

        return paintingsList;
    }

    @Override
    public void handle(Player player, Runner runner) {
        Map<Identifier, Optional<Painting>> paintings = paintings();
        boolean cleared = clear();

        runner.run(() -> {
            if (cleared) {
                ClientPaintingManager.getPaintings().clear();
            }

            paintings.forEach((id, painting) -> {
                if (painting.isEmpty()) {
                    ClientPaintingManager.deregisterPainting(id);
                } else {
                    ClientPaintingManager.registerPainting(id, painting.get());
                }
            });

            if (Minecraft.getInstance().screen instanceof ImmersivePaintingScreen screen) {
                screen.refreshPage();
            }
        });
    }


    @Override
    public Type<PaintingSyncPayload> type() {
        return TYPE;
    }
}
