package immersive_paintings.network.s2c;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.resources.ClientPaintingManager;
import immersive_paintings.resources.Paintings;
import immersive_paintings.util.ImageManipulations;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

public class ImageResponse implements Message {
    private final String identifier;
    private final int[] data;
    private final int segment;
    private final int totalSegments;

    private static final Map<Identifier, List<int[]>> buffer = new HashMap<>();

    public ImageResponse(Identifier identifier, int[] data, int segment, int totalSegments) {
        this.identifier = identifier.toString();
        this.data = data;
        this.segment = segment;
        this.totalSegments = totalSegments;
    }

    @Override
    public void receive(PlayerEntity e) {
        Identifier i = new Identifier(this.identifier);

        List<int[]> integers = buffer.computeIfAbsent(i, (k) -> new LinkedList<>());
        integers.add(data);

        if (segment + 1 == totalSegments) {
            if (ClientPaintingManager.getPaintings().containsKey(i)) {
                Paintings.PaintingData data = ClientPaintingManager.getPaintings().get(i);
                int[] ints = integers.stream().flatMapToInt(Arrays::stream).toArray();
                data.image = ImageManipulations.intsToImage(data.getPixelWidth(), data.getPixelHeight(), ints);
                data.textureIdentifier = MinecraftClient.getInstance().getTextureManager()
                        .registerDynamicTexture("immersive_painting/" + this.identifier.replace(":", "_"), new NativeImageBackedTexture(data.image));
                data.image.upload(0, 0, 0, false);
                buffer.remove(i);
            }
        }
    }
}
