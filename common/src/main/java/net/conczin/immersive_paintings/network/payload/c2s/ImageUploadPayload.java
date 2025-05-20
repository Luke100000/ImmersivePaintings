package net.conczin.immersive_paintings.network.payload.c2s;

import java.awt.image.BufferedImage;
import java.util.Optional;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.network.SegmentManager;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.painting.ServerPaintingManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record ImageUploadPayload(String name, byte[] data, int segment, int totalSegments) implements ImmersivePayload, SegmentManager.SegmentedPayload {
    public static final Type<ImageUploadPayload> TYPE = new Type<>(Main.locate("image_upload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ImageUploadPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ImageUploadPayload::name,
        ByteBufCodecs.BYTE_ARRAY, ImageUploadPayload::data,
        ByteBufCodecs.INT, ImageUploadPayload::segment,
        ByteBufCodecs.INT, ImageUploadPayload::totalSegments,
        ImageUploadPayload::new
    );

    @Override
    public void handle(Player player) {
        Optional<BufferedImage> image = SegmentManager.handleSegmentedPayload(player.getStringUUID(), this);
        if (image.isEmpty())
            return;

        ServerPaintingManager.uploadedImages.put(player.getUUID(), image.get());
    }

    @Override
    public Type<ImageUploadPayload> type() {
        return TYPE;
    }
}
