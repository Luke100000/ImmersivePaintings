package immersive_paintings.network;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.util.ImageManipulations;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;

public abstract class SegmentedPaintingMessage implements Message {
    private final int width;
    private final int height;
    private final int[] data;
    private final int segment;
    private final int totalSegments;

    private static final Map<String, List<int[]>> buffer = new HashMap<>();

    public SegmentedPaintingMessage(int width, int height, int[] data, int segment, int totalSegments) {
        this.width = width;
        this.height = height;
        this.data = data;
        this.segment = segment;
        this.totalSegments = totalSegments;
    }

    abstract protected String getIdentifier(PlayerEntity e);
    abstract protected void process(PlayerEntity e, NativeImage image);

    @Override
    public void receive(PlayerEntity e) {
        String i = getIdentifier(e);

        List<int[]> integers = buffer.computeIfAbsent(i, (k) -> new LinkedList<>());
        integers.add(data);

        if (segment + 1 == totalSegments) {
            int[] ints = integers.stream().flatMapToInt(Arrays::stream).toArray();
            NativeImage image = ImageManipulations.intsToImage(width, height, ints);
            process(e, image);
            buffer.remove(i);
        }
    }
}
