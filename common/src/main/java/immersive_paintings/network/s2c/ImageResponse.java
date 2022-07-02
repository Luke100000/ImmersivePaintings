package immersive_paintings.network.s2c;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.resources.ClientPaintingManager;
import immersive_paintings.resources.Painting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

public class ImageResponse implements Message {
    private final String identifier;
    private final Painting.Type type;
    private final int width;
    private final int height;
    private final int[] data;
    private final int segment;
    private final int totalSegments;

    private static final Map<Identifier, List<int[]>> buffer = new HashMap<>();

    public ImageResponse(Identifier identifier, Painting.Type type, int width, int height, int[] data, int segment, int totalSegments) {
        this.identifier = identifier.toString();
        this.type = type;
        this.width = width;
        this.height = height;
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
                int[] ints = integers.stream().flatMapToInt(Arrays::stream).toArray();
                ClientPaintingManager.loadImage(i, type, ints, width, height);
                buffer.remove(i);
            }
        }
    }
}
