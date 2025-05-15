package net.conczin.immersive_paintings.network.payload.c2s;

import java.util.Optional;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.network.LazyNetworkManager;
import net.conczin.immersive_paintings.network.Network;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.s2c.ImageResponsePayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingListPayload;
import net.conczin.immersive_paintings.painting.ServerPaintingManager;
import net.conczin.immersive_paintings.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ImageRequestPayload(ResourceLocation identifier) implements ImmersivePayload {
    public static final Type<ImageRequestPayload> TYPE = new Type<>(Main.locate("image_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ImageRequestPayload> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, ImageRequestPayload::identifier,
        ImageRequestPayload::new
    );

    // TODO: Send thumbnail before actual image
    @Override
    public void handle(Player player) {
        Optional<byte[]> image = ServerPaintingManager.getImageData(identifier());
        if (image.isPresent()) {
            Utils.processByteArrayInChunks(image.get(), (bytes, split, count) -> LazyNetworkManager.sendToClient(new ImageResponsePayload(identifier(), bytes, split, count), (ServerPlayer) player));
        } else {
            // If the painting was deleted on the server (or doesn't exist) we want to remove it from the client's view
            Network.sendToAllClients(player.getServer(), new PaintingListPayload(identifier(), null));
        }
    }

    @Override
    public Type<ImageRequestPayload> type() {
        return TYPE;
    }
}
