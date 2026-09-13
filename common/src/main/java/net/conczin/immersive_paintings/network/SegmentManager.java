package net.conczin.immersive_paintings.network;

import net.conczin.immersive_paintings.ImmersivePaintings;
import net.conczin.immersive_paintings.util.ImageManipulations;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SegmentManager {
    private final Map<String, ByteArrayOutputStream> buffer = new HashMap<>();

    public synchronized Optional<BufferedImage> handleSegmentedPayload(String key, byte[] data, int segment, int totalSegments) {
        return handleSegmentedPayload(key, data, segment, totalSegments, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public synchronized Optional<BufferedImage> handleSegmentedPayload(String key, byte[] data, int segment, int totalSegments, int maxBytes, int maxWidth, int maxHeight) {
        ByteArrayOutputStream byteBuffer = buffer.computeIfAbsent(key, k -> new ByteArrayOutputStream());
        if (segment < 0 || totalSegments < 1 || segment >= totalSegments || byteBuffer.size() > maxBytes - data.length) {
            buffer.remove(key);
            return Optional.empty();
        }

        try {
            byteBuffer.write(data);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        if (segment + 1 == totalSegments) {
            buffer.remove(key);

            try {
                return Optional.ofNullable(ImageManipulations.decode(byteBuffer.toByteArray(), maxWidth, maxHeight));
            } catch (IOException e) {
                ImmersivePaintings.LOGGER.error("Could not combine segmented payloads for {}", key, e);
            }
        }

        return Optional.empty();
    }

    public synchronized void clear(String key) {
        buffer.remove(key);
    }
}
