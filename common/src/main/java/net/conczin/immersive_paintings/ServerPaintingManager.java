package net.conczin.immersive_paintings;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingSyncPayload;
import net.conczin.immersive_paintings.registry.Configs;
import net.conczin.immersive_paintings.util.Cache;
import net.conczin.immersive_paintings.util.ImageManipulations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.saveddata.SavedData;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.saveddata.SavedDataType;

public class ServerPaintingManager extends SavedData {
    public static final SavedDataType<ServerPaintingManager> TYPE = new SavedDataType<>(
            Main.MOD_ID,
            ServerPaintingManager::new,
            ctx -> RecordCodecBuilder.create(instance -> instance.group(
                    RecordCodecBuilder.point(ctx.levelOrThrow()),
                    Codec.unboundedMap(ResourceLocation.CODEC, Painting.CODEC).fieldOf("paintings").forGetter(s -> s.customPaintings)
            ).apply(instance, ServerPaintingManager::new)),
            null
    );

    private final Map<ResourceLocation, Painting> customPaintings;
    private static Map<ResourceLocation, Entry<Painting, Resource>> datapackPaintings = new HashMap<>();

    private static final ServerCache paintingCache = new ServerCache("");
    private static final ServerCache thumbnailCache = new ServerCache("_thumbnail");

    public ServerPaintingManager(SavedData.Context ctx) {
        this(ctx.levelOrThrow(), new HashMap<>());
    }

    public ServerPaintingManager(ServerLevel level, Map<ResourceLocation, Painting> customPaintings) {
        this.customPaintings = new HashMap<>(customPaintings); // Codec gives us an ImmutableMap, we need it mutable
    }

    private static ServerPaintingManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static Map<ResourceLocation, Painting> getCustomPaintings(MinecraftServer server) {
        return get(server).customPaintings;
    }

    public static Map<ResourceLocation, Entry<Painting, Resource>> getDatapackPaintings() {
        return datapackPaintings;
    }

    public static void setDatapackPaintings(Map<ResourceLocation, Entry<Painting, Resource>> datapackPaintings) {
        ServerPaintingManager.datapackPaintings = datapackPaintings;
    }

    public static Optional<Painting> getPainting(MinecraftServer server, ResourceLocation identifier) {
        if (datapackPaintings.containsKey(identifier)) {
            return Optional.of(datapackPaintings.get(identifier).getKey());
        } else {
            return Optional.ofNullable(getCustomPaintings(server).get(identifier));
        }
    }

    public static Optional<byte[]> getImageData(ResourceLocation identifier, boolean thumbnail) {
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

    public static void registerPainting(MinecraftServer server, ResourceLocation identifier, Painting painting, BufferedImage image) {
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

    public static void deregisterPainting(MinecraftServer server, ResourceLocation identifier) {
        getCustomPaintings(server).remove(identifier);
        get(server).setDirty(true);
        paintingCache.delete(identifier);
        thumbnailCache.delete(identifier);
    }

    public static void playerLoggedIn(ServerPlayer player) {
        HashMap<ResourceLocation, Optional<Painting>> paintings = new HashMap<>();
        getDatapackPaintings().forEach((id, entry) -> paintings.put(id, Optional.of(entry.getKey())));
        getCustomPaintings(player.getServer()).forEach((id, p) -> paintings.put(id, Optional.of(p)));

        // Break paintings up into smaller batches if necessary to avoid packet limits
        // The interval is chosen to be an arbitrary number that feels like a good amount to send at a time
        List<PaintingSyncPayload> payloads = new ArrayList<>();
        final int interval = Configs.COMMON.packetSplitInterval;

        Main.LOGGER.debug("Found {} paintings, splitting into {} groups", paintings.size(), paintings.size() / interval + Math.min(paintings.size() % interval, 1));

        int size = paintings.size();
        int processed = 0;

        while (processed < size) {
            int currentSize = Math.min(size - processed, interval);
            Map<ResourceLocation, Optional<Painting>> p = paintings
                    .entrySet()
                    .stream()
                    .skip(processed)
                    .limit(currentSize)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            payloads.add(new PaintingSyncPayload(p));

            processed += currentSize;
        }

        payloads.forEach(payload -> NetworkHandler.sendToClient(player, payload));
    }

    private static class ServerCache extends Cache<ResourceLocation, byte[]> {
        private String suffix = ".png";

        public ServerCache(String suffix) {
            super();
            this.suffix = suffix + this.suffix;
        }

        @Override
        public String getCachePath(ResourceLocation key) {
            return key.getPath() + suffix;
        }

        public Optional<byte[]> getResource(ResourceLocation key, Resource resource) {
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
