package immersive_paintings.network.c2s;

import immersive_paintings.Config;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.network.LazyNetworkManager;
import immersive_paintings.network.s2c.ImageResponse;
import immersive_paintings.resources.ByteImage;
import immersive_paintings.resources.Painting;
import immersive_paintings.resources.ServerPaintingManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.io.Serial;
import java.util.Arrays;

public class ImageRequest implements Message {
    @Serial
    private static final long serialVersionUID = 3086732481904956437L;

    private final String identifier;
    private final Painting.Type type;

    public ImageRequest(Identifier identifier, Painting.Type type) {
        this.identifier = identifier.toString();
        this.type = type;
    }

    @Override
    public void receive(PlayerEntity e) {
        Identifier identifier = new Identifier(this.identifier);
        ByteImage image = ServerPaintingManager.getImage(identifier, type);

        if (image != null) {
            byte[] is = image.getBytes();
            int splits = (int)Math.ceil((double)is.length / Config.getInstance().packetSize);
            int split = 0;
            for (int i = 0; i < is.length; i += Config.getInstance().packetSize) {
                byte[] ints = Arrays.copyOfRange(is, i, Math.min(is.length, i + Config.getInstance().packetSize));
                LazyNetworkManager.sendClient(new ImageResponse(identifier, type, image.getWidth(), image.getHeight(), ints, split, splits), (ServerPlayerEntity)e);
                split++;
            }
        }
    }
}
