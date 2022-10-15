package immersive_paintings.network.c2s;

import immersive_paintings.network.SegmentedPaintingMessage;
import immersive_paintings.resources.ByteImage;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;

public class UploadPaintingRequest extends SegmentedPaintingMessage {
    private static final long serialVersionUID = -8172991552002686333L;

    public static final HashMap<String, ByteImage> uploadedImages = new HashMap<>();

    public UploadPaintingRequest(byte[] data, int segment, int totalSegments) {
        super(data, segment, totalSegments);
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
