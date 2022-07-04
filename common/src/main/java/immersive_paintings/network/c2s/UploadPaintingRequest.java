package immersive_paintings.network.c2s;

import immersive_paintings.network.SegmentedPaintingMessage;
import immersive_paintings.resources.ByteImage;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;

public class UploadPaintingRequest extends SegmentedPaintingMessage {
    public static final HashMap<String, ByteImage> uploadedImages = new HashMap<>();

    public UploadPaintingRequest(int width, int height, byte[] data, int segment, int totalSegments) {
        super(width, height, data, segment, totalSegments);
    }

    @Override
    protected String getIdentifier(PlayerEntity e) {
        return e.getUuidAsString();
    }

    @Override
    protected void process(PlayerEntity e, ByteImage image) {
        uploadedImages.put(getIdentifier(e), image);
    }
}
