package net.conczin.immersive_paintings.network.payload.s2c;

import net.conczin.immersive_paintings.ClientPaintingManager;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.network.SegmentManager;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public record ImageResponsePayload(Identifier identifier, boolean thumbnail, byte[] data, int segment, int totalSegments) implements ImmersivePayload {
    public static final Type<ImageResponsePayload> TYPE = new Type<>(Main.locate("image_response"));
    public static final StreamCodec<FriendlyByteBuf, ImageResponsePayload> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, ImageResponsePayload::identifier,
            ByteBufCodecs.BOOL, ImageResponsePayload::thumbnail,
            ByteBufCodecs.BYTE_ARRAY, ImageResponsePayload::data,
            ByteBufCodecs.INT, ImageResponsePayload::segment,
            ByteBufCodecs.INT, ImageResponsePayload::totalSegments,
            ImageResponsePayload::new
    );

    private static final SegmentManager manager = new SegmentManager();

    @Override
    public void handle(Player player, Runner runner) {
        String key = identifier().toString();
        if (thumbnail)
            key += "_thumbnail"; // Allows Thumbnail and FULL to download together

        Identifier id = identifier();
        boolean thumbnail = thumbnail();

        byte[] data = data();
        int segment = segment();
        int totalSegments = totalSegments();

        manager.handleSegmentedPayload(key, data, segment, totalSegments).ifPresent(image -> runner.run(() -> {
            if (thumbnail) {
                ClientPaintingManager.registerThumbnail(id, image, false);
            } else {
                ClientPaintingManager.registerImage(id, image, false);
            }
        }));
    }

    @Override
    public Type<ImageResponsePayload> type() {
        return TYPE;
    }
}
