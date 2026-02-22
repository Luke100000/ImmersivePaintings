package net.conczin.immersive_paintings;

import com.mojang.serialization.Codec;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingSyncPayload;
import net.conczin.immersive_paintings.util.Cache;
import net.conczin.immersive_paintings.util.ImageManipulations;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

public class ServerPaintingManager extends SavedData {
    private static final ServerCache paintingCache = new ServerCache("");
    private static final ServerCache thumbnailCache = new ServerCache("_thumbnail");

    static final Codec<ServerPaintingManager> CODEC = Codec.unboundedMap(Identifier.CODEC, Painting.CODEC)
        .xmap(
            paintings -> {
                ServerPaintingManager m = new ServerPaintingManager();
                paintings.forEach((id, painting) -> {
                    if (!paintingCache.exists(id))
                        return;

                    // Add any thumbnails that don't exist for some reason
                    if (!thumbnailCache.exists(id)) {
                        try {
                            Optional<byte[]> source = paintingCache.get(id);
                            if (source.isPresent()) {
                                thumbnailCache.set(id, ImageManipulations.encode(
                                    ImageManipulations.resizeImage(ImageManipulations.decode(source.get()), Painting.Size.THUMBNAIL)));
                            }
                        } catch (IOException e) {
                            Main.LOGGER.error("could not create thumbnail from stored image", e);
                        }
                    }

                    m.customServerPaintings.put(id, painting);
                });
                return m;
            },
            m -> m.customServerPaintings
        );

    private static final SavedDataType<ServerPaintingManager> TYPE = new SavedDataType<>(
        Main.MOD_ID,
        ServerPaintingManager::new,
        CODEC,
        DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private static final Set<UUID> sent = new HashSet<>();

    private static Map<Identifier, Entry<Painting, Resource>> datapackPaintings = new HashMap<>();
    private final Map<Identifier, Painting> customServerPaintings = new HashMap<>();

    private static ServerPaintingManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static Map<Identifier, Painting> getCustomPaintings(MinecraftServer server) {
        return get(server).customServerPaintings;
    }

    public static Map<Identifier, Entry<Painting, Resource>> getDatapackPaintings() {
        return datapackPaintings;
    }

    public static void setDatapackPaintings(Map<Identifier, Entry<Painting, Resource>> datapackPaintings) {
        // TODO: List old packs as deleted, refresh and use new ones
        ServerPaintingManager.datapackPaintings = datapackPaintings;
    }

    public static Optional<Painting> getPainting(MinecraftServer server, Identifier identifier) {
        if (datapackPaintings.containsKey(identifier)) {
            return Optional.of(datapackPaintings.get(identifier).getKey());
        } else {
            return Optional.ofNullable(getCustomPaintings(server).get(identifier));
        }
    }

    public static Optional<byte[]> getImageData(Identifier identifier, boolean thumbnail) {
        if (datapackPaintings.containsKey(identifier)) {
            // TODO: Not doing thumbnails for datapacks simplifies some things, but if a user adds a
            // custom datapack with larger images (default pack is only ~4MB), then it will be slower
            Entry<Painting, Resource> entry = datapackPaintings.get(identifier);
            return paintingCache.getResource(entry.getKey().location(), entry.getValue());
        } else {
            Optional<byte[]> image;
            if (thumbnail) {
                image = thumbnailCache.get(identifier);
            } else {
                image = paintingCache.get(identifier);
            }

            if (image.isEmpty())
                Main.LOGGER.error("no image found with identifier {} [thumb={}] in cache", identifier, thumbnail);
            return image;
        }
    }

    public static void registerPainting(MinecraftServer server, Identifier identifier, Painting painting, BufferedImage image) {
        if (image != null) {
            try {
                paintingCache.set(identifier, ImageManipulations.encode(image));
                thumbnailCache.set(identifier, ImageManipulations.encode(ImageManipulations.resizeImage(image, Painting.Size.THUMBNAIL)));
                getCustomPaintings(server).put(identifier, painting);
                get(server).setDirty(true);
            } catch (IOException e) {
                Main.LOGGER.error("could not register image {}", identifier, e);
            }
        }
    }

    public static void deregisterPainting(MinecraftServer server, Identifier identifier) {
        getCustomPaintings(server).remove(identifier);
        get(server).setDirty(true);
        paintingCache.delete(identifier);
        thumbnailCache.delete(identifier);
    }

    public static void playerLoggedOut(ServerPlayer player) {
        sent.remove(player.getUUID());
    }

    public static void playerLoggedIn(ServerPlayer player) {
        if (!sent.contains(player.getUUID())) {
            HashMap<Identifier, Optional<Painting>> paintings = new HashMap<>();
            getDatapackPaintings().forEach((id, entry) -> paintings.put(id, Optional.of(entry.getKey())));
            getCustomPaintings(player.level().getServer()).forEach((id, p) -> paintings.put(id, Optional.of(p)));

            List<PaintingSyncPayload> payloads = PaintingSyncPayload.splitPaintings(paintings, true);
            payloads.forEach(payload -> NetworkHandler.sendToClient(player, payload));
            sent.add(player.getUUID());
        }
    }

    private static class ServerCache extends Cache<Identifier, byte[]> {
        private String suffix = ".png";

        public ServerCache(String suffix) {
            this.suffix = suffix + this.suffix;
        }

        @Override
        public String getCachePath(Identifier key) {
            return key.getPath() + suffix;
        }

        public Optional<byte[]> getResource(Identifier key, Resource resource) {
            Optional<byte[]> data = get(key);
            if (data.isPresent())
                return data;

            try (InputStream stream = resource.open()) {
                byte[] bytes = stream.readAllBytes();
                // Set so that future queries will go from cache instead
                set(key, bytes);
                return Optional.of(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public byte[] decode(byte[] bytes) {
            return bytes;
        }

        @Override
        public byte[] encode(byte[] image) {
            return image;
        }
    }
}
