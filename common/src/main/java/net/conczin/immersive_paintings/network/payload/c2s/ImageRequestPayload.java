package net.conczin.immersive_paintings.network.payload.c2s;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.conczin.immersive_paintings.network.LazyNetworkManager;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.s2c.ImageResponsePayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingSyncPayload;
import net.conczin.immersive_paintings.util.ImageManipulations;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public record ImageRequestPayload(Identifier identifier, boolean thumbnail) implements ImmersivePayload {
    public static final Type<ImageRequestPayload> TYPE = new Type<>(Main.locate("image_request"));
    public static final StreamCodec<FriendlyByteBuf, ImageRequestPayload> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, ImageRequestPayload::identifier,
            ByteBufCodecs.BOOL, ImageRequestPayload::thumbnail,
            ImageRequestPayload::new
    );

    @Override
    public void handle(Player player, Runner runner) {
        Identifier id = identifier();
        boolean thumbnail = thumbnail();

        runner.run(() -> {
            Optional<byte[]> image = ServerPaintingManager.getImageData(id, thumbnail);
            if (image.isPresent()) {
                ImageManipulations.processByteArrayInChunks(image.get(), (bytes, split, count) -> LazyNetworkManager.sendToClient(new ImageResponsePayload(id, thumbnail, bytes, split, count), (ServerPlayer) player));
            } else if (!thumbnail && player.level().getServer() != null) {
                // If the painting was deleted on the server (or doesn't exist) we want to remove it from the client's view
                NetworkHandler.sendToAllClients(player.level().getServer(), new PaintingSyncPayload(id, null));
            }
        });
    }

    @Override
    public Type<ImageRequestPayload> type() {
        return TYPE;
    }
}
