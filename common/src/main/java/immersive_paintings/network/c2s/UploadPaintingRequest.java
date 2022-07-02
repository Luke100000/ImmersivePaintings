package immersive_paintings.network.c2s;

import immersive_paintings.network.SegmentedPaintingMessage;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;

public class UploadPaintingRequest extends SegmentedPaintingMessage {
    public static final HashMap<String, NativeImage> uploadedImages = new HashMap<>();

    public UploadPaintingRequest(int width, int height, int[] data, int segment, int totalSegments) {
        super(width, height, data, segment, totalSegments);
    }

    @Override
    protected String getIdentifier(PlayerEntity e) {
        return e.getUuidAsString();
    }

    @Override
    protected void process(PlayerEntity e, NativeImage image) {
        uploadedImages.put(getIdentifier(e), image);
    }
}
