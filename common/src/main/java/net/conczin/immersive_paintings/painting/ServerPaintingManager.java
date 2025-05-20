package net.conczin.immersive_paintings.painting;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.util.ImageManipulations;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.saveddata.SavedData;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

public class ServerPaintingManager extends SavedData {
    public static final SavedData.Factory<ServerPaintingManager> TYPE = new SavedData.Factory<>(ServerPaintingManager::new, ServerPaintingManager::fromNbt, null);
    public static final Codec<Map<ResourceLocation, Painting>> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, Painting.CODEC);

    public static final HashMap<UUID, BufferedImage> uploadedImages = new HashMap<>();

    private static Map<ResourceLocation, Entry<Painting, Resource>> datapackPaintings = new HashMap<>();
    private final Map<ResourceLocation, Painting> customServerPaintings = new HashMap<>();

    private static final ServerCache serverCache = new ServerCache();

    private static ServerPaintingManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE, Main.MOD_ID);
    }

    private Map<ResourceLocation, Painting> getCustomServerPaintings() {
        return customServerPaintings;
    }

    public static Map<ResourceLocation, Painting> getCustomPaintings(MinecraftServer server) {
        return get(server).getCustomServerPaintings();
    }

    public static Map<ResourceLocation, Entry<Painting, Resource>> getDatapackPaintings() {
        return datapackPaintings;
    }

    public static void setDatapackPaintings(Map<ResourceLocation, Entry<Painting, Resource>> datapackPaintings) {
        ServerPaintingManager.datapackPaintings = datapackPaintings;
    }

    public static Painting getPainting(MinecraftServer server, ResourceLocation identifier) {
        if (datapackPaintings.containsKey(identifier)) {
            return datapackPaintings.get(identifier).getKey();
        } else {
            return getCustomPaintings(server).getOrDefault(identifier, Painting.DEFAULT);
        }
    }

    public static Optional<byte[]> getImageData(ResourceLocation identifier) {
        if (datapackPaintings.containsKey(identifier)) {
            Entry<Painting, Resource> entry = datapackPaintings.get(identifier);
            return serverCache.getResource(entry.getKey().location(), entry.getValue());
        } else {
            Optional<byte[]> image = serverCache.get(identifier);
            if (image.isEmpty())
                Main.LOGGER.error("no image found with identifier {} in cache", identifier);
            return image;
        }
    }

    public static void registerPainting(MinecraftServer server, ResourceLocation identifier, Painting painting, BufferedImage image) {
        if (image != null) {
            serverCache.set(identifier, ImageManipulations.encode(image));
            getCustomPaintings(server).put(identifier, painting);
            get(server).setDirty(true);
        }
    }

    public static void deregisterPainting(MinecraftServer server, ResourceLocation identifier) {
        getCustomPaintings(server).remove(identifier);
        get(server).setDirty(true);
        serverCache.delete(identifier);
    }

    public static ServerPaintingManager fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
        ServerPaintingManager m = new ServerPaintingManager();

        Map<ResourceLocation, Painting> paintings = CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("paintings"))
            .result()
            .orElse(new HashMap<>());

        // Remove any paintings that don't exist anymore
        Map<ResourceLocation, Painting> filteredPaintings = paintings
                .entrySet().stream()
                .filter(s -> serverCache.exists(s.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        m.customServerPaintings.putAll(filteredPaintings);
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
        @Override
        public String getCachePath(ResourceLocation key) {
            return key.getPath() + ".png";
        }

        public Optional<byte[]> getResource(ResourceLocation key, Resource resource) {
            Optional<byte[]> data = get(key);
            if (data.isPresent())
                return data;

            try (InputStream stream = resource.open()) {
                byte[] bytes = stream.readAllBytes();
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
