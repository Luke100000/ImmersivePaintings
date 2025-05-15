package net.conczin.immersive_paintings.network.payload.s2c;

import java.util.*;
import java.util.stream.Collectors;

import net.conczin.immersive_paintings.Config;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.client.gui.ImmersivePaintingScreen;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.painting.ClientPaintingManager;
import net.conczin.immersive_paintings.painting.Painting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PaintingListPayload(Map<ResourceLocation, Optional<Painting>> paintings, boolean clear) implements ImmersivePayload {
    public static final Type<PaintingListPayload> TYPE = new Type<>(Main.locate("painting_list"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PaintingListPayload> STREAM_CODEC = StreamCodec.ofMember(PaintingListPayload::encode, PaintingListPayload::decode);

    public PaintingListPayload(ResourceLocation identifier, Painting painting) {
        this(Map.of(identifier, Optional.ofNullable(painting)), false);
    }

    // Break paintings up into smaller batches if necessary to avoid packet limits
    // The interval is chosen to be an arbitrary number that feels like a good amount to send at a time
    public static List<PaintingListPayload> splitPaintings(Map<ResourceLocation, Optional<Painting>> paintings, boolean clearFirst) {
        List<PaintingListPayload> paintingsList = new ArrayList<>();
        final int interval = Config.getInstance().packetSplitInterval;

        Map<ResourceLocation, Optional<Painting>> optionalPaintings = paintings
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (paintings.size() < interval) {
            Main.LOGGER.debug("Found {} paintings, < {}", paintings.size(), interval);
            paintingsList.add(new PaintingListPayload(optionalPaintings, true));
            return paintingsList;
        }

        Main.LOGGER.debug("Found {} paintings, >= {}, splitting into {} groups", paintings.size(), interval, paintings.size() / interval + Math.min(paintings.size() % interval, 1));

        int totalSize = paintings.size();
        int processed = 0;

        while (processed < totalSize) {
            int currentSize = Math.min(totalSize - processed, interval);
            Map<ResourceLocation, Optional<Painting>> p = optionalPaintings
                    .entrySet().stream()
                    .skip(processed)
                    .limit(currentSize)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            paintingsList.add(new PaintingListPayload(p, processed == 0 && clearFirst));

            processed += currentSize;
        }

        return paintingsList;
    }

    public RegistryFriendlyByteBuf encode(RegistryFriendlyByteBuf buf) {
        buf.writeMap(paintings, ResourceLocation.STREAM_CODEC, (b, painting) -> {
            b.writeOptional(painting, Painting.STREAM_CODEC);
        });
        buf.writeBoolean(clear);
        return buf;
    }

    public static PaintingListPayload decode(RegistryFriendlyByteBuf buf) {
        Map<ResourceLocation, Optional<Painting>> paintings = buf.readMap(ResourceLocation.STREAM_CODEC, (b) -> b.readOptional(Painting.STREAM_CODEC));
        boolean clear = buf.readBoolean();
        return new PaintingListPayload(paintings, clear);
    }

    @Override
    public void handle(Player player) {
        if (clear()) {
            ClientPaintingManager.getPaintings().clear();
        }

        paintings().forEach((id, painting) -> {
            if (painting.isEmpty()) {
                ClientPaintingManager.deregisterPainting(id);
            } else {
                ClientPaintingManager.registerPainting(id, painting.get());
            }
        });

        if (Minecraft.getInstance().screen instanceof ImmersivePaintingScreen screen) {
            screen.refreshPage();
        }
    }


    @Override
    public Type<PaintingListPayload> type() {
        return TYPE;
    }
}
