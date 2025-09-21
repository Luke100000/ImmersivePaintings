package net.conczin.immersive_paintings.util;

import net.conczin.immersive_paintings.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class Cache<K, V> {
    public static final Path CACHE_PATH = Path.of(Main.MOD_ID + "_cache");

    private final Map<K, V> cache;

    // TODO: Add a way to configure this number in settings
    public Cache() {
        this(200);
    }

    // The maximum number of entries to allow in the cache before old entries start getting evicted
    public Cache(int maxEntries) {
        this.cache = Collections.synchronizedMap(new LinkedHashMap<>(maxEntries + 1, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
                return size() > maxEntries;
            }
        });
    }

    public abstract String getCachePath(K key);

    public abstract V decode(byte[] bytes) throws IOException;

    public abstract byte[] encode(V key);

    // TODO: Some sort of logging needs to be done if the file doesn't exist
    private File getFile(K key) {
        Path path = CACHE_PATH.resolve(getCachePath(key));
        return Files.exists(path) ? path.toFile() : null;
    }

    public boolean exists(K key) {
        return getFile(key) != null;
    }

    public Optional<V> get(K key) {
        if (cache.containsKey(key))
            return Optional.of(cache.get(key));

        File file = getFile(key);
        if (file == null)
            return Optional.empty();

        try (FileInputStream stream = new FileInputStream(file)) {
            V data = decode(stream.readAllBytes());
            if (data == null)
                return Optional.empty();

            cache.put(key, data);
            return Optional.of(data);
        } catch (IOException e) {
            // https://logging.apache.org/log4j/2.x/manual/api.html#best-practice-exception
            Main.LOGGER.error("failed getting cached file {}", file, e);
        }

        return Optional.empty();
    }

    public void set(K key, V value) {
        Path path = CACHE_PATH.resolve(getCachePath(key));
        if (!Files.exists(path.getParent())) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                Main.LOGGER.error("failed creating directories for {}", path.getParent(), e);
                return;
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(path.toFile())) {
            byte[] v = encode(value);
            if (v != null) {
                outputStream.write(v);
                cache.put(key, value);
            }
        } catch (IOException e) {
            Main.LOGGER.error("failed writing cached file {}", path, e);
        }
    }

    public boolean delete(K key) {
        cache.remove(key);
        File file = getFile(key);
        if (file == null)
            return false;

        boolean deleted = file.delete();

        // Remove folder if no more files exist in it
        Path parent = file.toPath().getParent();
        try (Stream<Path> entries = Files.list(parent)) {
            if (entries.findAny().isEmpty())
                Files.delete(parent);
        } catch (IOException e) {
            Main.LOGGER.error("failed deleting cache directory {}", parent, e);
        }

        return deleted;
    }
}
