package net.conczin.immersive_paintings.network.payload.s2c;

import java.util.Optional;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.network.SegmentManager;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.painting.ClientPaintingManager;
import net.conczin.immersive_paintings.util.ByteImage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record ImageResponsePayload(ResourceLocation identifier, byte[] data, int segment, int totalSegments) implements ImmersivePayload, SegmentManager.SegmentedPayload {
    public static final Type<ImageResponsePayload> TYPE = new Type<>(Main.locate("image_response"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ImageResponsePayload> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, ImageResponsePayload::identifier,
        ByteBufCodecs.BYTE_ARRAY, ImageResponsePayload::data,
        ByteBufCodecs.INT, ImageResponsePayload::segment,
        ByteBufCodecs.INT, ImageResponsePayload::totalSegments,
        ImageResponsePayload::new
    );

    @Override
    public void handle(Player player) {
        // Add the player's UUID to avoid situations where multiple players are trying to load the same Identifier
        Optional<ByteImage> image = SegmentManager.handleSegmentedPayload(player.getStringUUID() + "_" + identifier.toString(), this);
        if (image.isEmpty())
            return;

        ClientPaintingManager.registerImage(identifier(), image.get());
    }

    @Override
    public Type<ImageResponsePayload> type() {
        return TYPE;
    }
}
