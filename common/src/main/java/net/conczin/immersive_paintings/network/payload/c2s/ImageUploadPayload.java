package net.conczin.immersive_paintings.network.payload.c2s;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.network.SegmentManager;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public record ImageUploadPayload(byte[] data, int segment, int totalSegments) implements ImmersivePayload {
    public static final Type<ImageUploadPayload> TYPE = new Type<>(Main.locate("image_upload"));
    public static final StreamCodec<FriendlyByteBuf, ImageUploadPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE_ARRAY, ImageUploadPayload::data,
        ByteBufCodecs.INT, ImageUploadPayload::segment,
        ByteBufCodecs.INT, ImageUploadPayload::totalSegments,
        ImageUploadPayload::new
    );

    private static final SegmentManager manager = new SegmentManager();
    public static final Map<String, BufferedImage> uploaded = new HashMap<>();

    @Override
    public void handle(Player player, Runner runner) {
        String key = player.getStringUUID();
        byte[] data = data();
        int segment = segment();
        int totalSegments = totalSegments();
        manager.handleSegmentedPayload(key, data, segment, totalSegments).ifPresent(image -> runner.run(() -> uploaded.put(key, image)));
    }

    @Override
    public Type<ImageUploadPayload> type() {
        return TYPE;
    }
}
