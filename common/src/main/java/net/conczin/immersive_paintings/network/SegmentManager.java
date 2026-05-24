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

    public Optional<BufferedImage> handleSegmentedPayload(String key, byte[] data, int segment, int totalSegments) {
        ByteArrayOutputStream byteBuffer = buffer.computeIfAbsent(key, k -> new ByteArrayOutputStream());
        try {
            byteBuffer.write(data);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        if (segment + 1 == totalSegments) {
            buffer.remove(key);

            try {
                return Optional.of(ImageManipulations.decode(byteBuffer.toByteArray()));
            } catch (IOException e) {
                ImmersivePaintings.LOGGER.error("could not combne segmented payloads for {}", key, e);
            }
        }

        return Optional.empty();
    }
}
