package immersive_paintings.network.c2s;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.network.LazyNetworkManager;
import immersive_paintings.network.s2c.ImageResponse;
import immersive_paintings.resources.Painting;
import immersive_paintings.resources.ServerPaintingManager;
import immersive_paintings.util.ImageManipulations;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Arrays;

public class ImageRequest implements Message {
    public static final int BYTES_PER_MESSAGE = 16 * 1024;

    private final String identifier;
    private final Painting.Type type;

    public ImageRequest(Identifier identifier, Painting.Type type) {
        this.identifier = identifier.toString();
        this.type = type;
    }

    @Override
    public void receive(PlayerEntity e) {
        Identifier identifier = new Identifier(this.identifier);
        NativeImage image = ServerPaintingManager.getImage(identifier, type);

        if (image != null) {
            int[] is = ImageManipulations.imageToInts(image);
            int splits = (int)Math.ceil((double)is.length / BYTES_PER_MESSAGE);
            int split = 0;
            for (int i = 0; i < is.length; i += BYTES_PER_MESSAGE) {
                int[] ints = Arrays.copyOfRange(is, i, Math.min(is.length, i + BYTES_PER_MESSAGE));
                LazyNetworkManager.sendClient(new ImageResponse(identifier, type, image.getWidth(), image.getHeight(), ints, split, splits), (ServerPlayerEntity)e);
                split++;
            }
        }
    }
}
