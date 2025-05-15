package net.conczin.immersive_paintings.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.conczin.immersive_paintings.util.ByteImage;

public class SegmentManager {
    private static final Map<String, ByteArrayOutputStream> buffer = new HashMap<>();

    public static Optional<ByteImage> handleSegmentedPayload(String key, SegmentedPayload payload) {
        ByteArrayOutputStream byteBuffer = buffer.computeIfAbsent(key, k -> new ByteArrayOutputStream());
        try {
            byteBuffer.write(payload.data());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        if (payload.segment() + 1 == payload.totalSegments()) {
            try {
                return Optional.of(ByteImage.read(byteBuffer.toByteArray()));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                buffer.remove(key);
            }
        }

        return Optional.empty();
    }

    public interface SegmentedPayload {
        byte[] data();
    
        int segment();
    
        int totalSegments();
    }
}
