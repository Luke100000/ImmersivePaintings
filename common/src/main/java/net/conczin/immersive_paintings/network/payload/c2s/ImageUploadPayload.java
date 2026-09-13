package net.conczin.immersive_paintings.network.payload.c2s;

import net.conczin.immersive_paintings.ImmersivePaintings;
import net.conczin.immersive_paintings.network.SegmentManager;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.registry.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.player.Player;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public record ImageUploadPayload(byte[] data, int segment, int totalSegments) implements ImmersivePayload {
    public static final Type<ImageUploadPayload> TYPE = new Type<>(ImmersivePaintings.locate("image_upload"));
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
        if (!player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(Config.COMMON.uploadPermissionLevel)))) return;

        String key = player.getStringUUID();
        byte[] data = data();
        int segment = segment();
        int totalSegments = totalSegments();
        int maxWidth = Config.COMMON.maxUserImageWidth;
        int maxHeight = Config.COMMON.maxUserImageHeight;
        if (Config.COMMON.automaticImageResizing) {
            int maxClientSize = 16 * Config.COMMON.maxPaintingResolution;
            maxWidth = Math.max(maxWidth, maxClientSize);
            maxHeight = Math.max(maxHeight, maxClientSize);
        }
        int maxBytes = (int)Math.min(Integer.MAX_VALUE, (long)maxWidth * maxHeight * 4 + 1024 * 1024);
        manager.handleSegmentedPayload(key, data, segment, totalSegments, maxBytes, maxWidth, maxHeight)
                .ifPresent(image -> runner.run(() -> uploaded.put(key, image)));
    }

    public static void playerLoggedOut(Player player) {
        String key = player.getStringUUID();
        manager.clear(key);
        uploaded.remove(key);
    }

    @Override
    public Type<ImageUploadPayload> type() {
        return TYPE;
    }
}
