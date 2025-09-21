package net.conczin.immersive_paintings;

import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingSyncPayload;
import net.conczin.immersive_paintings.util.Cache;
import net.conczin.immersive_paintings.util.ImageManipulations;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.saveddata.SavedData;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

import com.mojang.serialization.Codec;

public class ServerPaintingManager extends SavedData {
    private static final SavedData.Factory<ServerPaintingManager> TYPE = new SavedData.Factory<>(ServerPaintingManager::new, ServerPaintingManager::fromNbt, null);
    private static final Codec<Map<ResourceLocation, Painting>> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, Painting.CODEC);
    private static final Set<UUID> sent = new HashSet<>();

    private static Map<ResourceLocation, Entry<Painting, Resource>> datapackPaintings = new HashMap<>();
    private final Map<ResourceLocation, Painting> customServerPaintings = new HashMap<>();

    private static final ServerCache paintingCache = new ServerCache("");
    private static final ServerCache thumbnailCache = new ServerCache("_thumbnail");

    private static ServerPaintingManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE, Main.MOD_ID);
    }

    public static Map<ResourceLocation, Painting> getCustomPaintings(MinecraftServer server) {
        return get(server).customServerPaintings;
    }

    public static Map<ResourceLocation, Entry<Painting, Resource>> getDatapackPaintings() {
        return datapackPaintings;
    }

    public static void setDatapackPaintings(Map<ResourceLocation, Entry<Painting, Resource>> datapackPaintings) {
        // TODO: List old packs as deleted, refresh and use new ones
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

    public static void playerLoggedOut(ServerPlayer player) {
        sent.remove(player.getUUID());
    }

    public static void playerLoggedIn(ServerPlayer player) {
        if (!sent.contains(player.getUUID())) {
            HashMap<ResourceLocation, Optional<Painting>> paintings = new HashMap<>();
            getDatapackPaintings().forEach((id, entry) -> paintings.put(id, Optional.of(entry.getKey())));
            getCustomPaintings(player.getServer()).forEach((id, p) -> paintings.put(id, Optional.of(p)));

            List<PaintingSyncPayload> payloads = PaintingSyncPayload.splitPaintings(paintings, true);
            payloads.forEach(payload -> NetworkHandler.sendToClient(player, payload));
            sent.add(player.getUUID());
        }
    }

    public static ServerPaintingManager fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
        ServerPaintingManager m = new ServerPaintingManager();

        Map<ResourceLocation, Painting> paintings = CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("paintings"))
            .result()
            .orElse(new HashMap<>());

        paintings.forEach((id, painting) -> {
            if (!paintingCache.exists(id))
                return;

            // Add any thumbnails that don't exist for some reason
            if (!thumbnailCache.exists(id)) {
                try {
                    Optional<byte[]> source = paintingCache.get(id);
                    if (source.isPresent()) {
                        thumbnailCache.set(id, ImageManipulations.encode(ImageManipulations.resizeImage(ImageManipulations.decode(source.get()), Painting.Size.THUMBNAIL)));
                    }
                } catch (IOException e) {
                    Main.LOGGER.error("could not create thumbnail from stored image", e);
                }
            }

            m.customServerPaintings.put(id, painting);
        });

        return m;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
        CODEC.encodeStart(NbtOps.INSTANCE, customServerPaintings)
            .result()
            .ifPresent(tag -> nbt.put("paintings", tag));

        return nbt;
    }

    private static class ServerCache extends Cache<ResourceLocation, byte[]> {
        private String suffix = ".png";

        public ServerCache(String suffix) {
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
