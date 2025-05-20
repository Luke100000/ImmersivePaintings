package net.conczin.immersive_paintings.network;

import net.conczin.immersive_paintings.util.ImageManipulations;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SegmentManager {
    private static final Map<String, ByteArrayOutputStream> buffer = new HashMap<>();

    public static Optional<BufferedImage> handleSegmentedPayload(String key, SegmentedPayload payload) {
        ByteArrayOutputStream byteBuffer = buffer.computeIfAbsent(key, k -> new ByteArrayOutputStream());
        try {
            byteBuffer.write(payload.data());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        if (payload.segment() + 1 == payload.totalSegments()) {
            buffer.remove(key);
            return Optional.ofNullable(ImageManipulations.decode(byteBuffer.toByteArray()));
        }

        return Optional.empty();
    }

    public interface SegmentedPayload {
        byte[] data();
    
        int segment();
    
        int totalSegments();
    }
}
