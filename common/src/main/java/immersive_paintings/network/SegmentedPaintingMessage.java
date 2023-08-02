package immersive_paintings.network;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.resources.ByteImage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class SegmentedPaintingMessage extends Message {
    private final int width;
    private final int height;
    private final byte[] data;
    private final int segment;
    private final int totalSegments;

    private static final Map<String, List<byte[]>> buffer = new HashMap<>();

    public SegmentedPaintingMessage(int width, int height, byte[] data, int segment, int totalSegments) {
        this.width = width;
        this.height = height;
        this.data = data;
        this.segment = segment;
        this.totalSegments = totalSegments;
    }

    public SegmentedPaintingMessage(PacketByteBuf b) {
        this.width = b.readInt();
        this.height = b.readInt();
        this.data = b.readByteArray();
        this.segment = b.readInt();
        this.totalSegments = b.readInt();
    }
    protected abstract String getIdentifier(PlayerEntity e);

    protected abstract void process(PlayerEntity e, ByteImage image);

    @Override
    public void encode(PacketByteBuf b) {
        b.writeInt(width);
        b.writeInt(height);
        b.writeByteArray(data);
        b.writeInt(segment);
        b.writeInt(totalSegments);
    }

    @Override
    public void receive(PlayerEntity e) {
        String i = getIdentifier(e);

        List<byte[]> byteBuffer = buffer.computeIfAbsent(i, k -> new LinkedList<>());
        byteBuffer.add(data);

        if (segment + 1 == totalSegments) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            for (byte[] b : byteBuffer) {
                try {
                    outputStream.write(b);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            byte[] bytes = outputStream.toByteArray();
            ByteImage image = new ByteImage(bytes, width, height);
            process(e, image);
            buffer.remove(i);
        }
    }
}
