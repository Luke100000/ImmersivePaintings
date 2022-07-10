package immersive_paintings.network.s2c;

import immersive_paintings.network.SegmentedPaintingMessage;
import immersive_paintings.resources.ByteImage;
import immersive_paintings.resources.ClientPaintingManager;
import immersive_paintings.resources.Painting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.io.Serial;

public class ImageResponse extends SegmentedPaintingMessage {
    @Serial
    private static final long serialVersionUID = -2404615222596628414L;

    private final String identifier;
    private final Painting.Type type;

    public ImageResponse(Identifier identifier, Painting.Type type, int width, int height, byte[] data, int segment, int totalSegments) {
        super(width, height, data, segment, totalSegments);
        this.identifier = identifier.toString();
        this.type = type;
    }

    @Override
    protected String getIdentifier(PlayerEntity e) {
        return identifier + type.name();
    }

    @Override
    protected void process(PlayerEntity e, ByteImage image) {
        ClientPaintingManager.loadImage(new Identifier(identifier), type, image);
    }
}
