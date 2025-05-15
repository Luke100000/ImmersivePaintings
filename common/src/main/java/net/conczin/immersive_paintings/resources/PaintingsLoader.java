package net.conczin.immersive_paintings.resources;

import com.google.gson.*;
import com.mojang.logging.LogUtils;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.painting.Painting;
import net.conczin.immersive_paintings.painting.ServerPaintingManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

public class PaintingsLoader extends SimplePreparableReloadListener<Map<ResourceLocation, Entry<Painting, Resource>>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Gson GSON = new GsonBuilder().create();

    private static final String dataType = "paintings";
    private static final int dataTypeLength = dataType.length() + 1;
    private static final int fileSuffixLength = ".json".length();
    
    protected static final ResourceLocation ID = Main.locate(dataType);

    @Override
    protected Map<ResourceLocation, Entry<Painting, Resource>> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, Entry<Painting, Resource>> map = new HashMap<>();
        
        Map<ResourceLocation, Resource> resources = manager.listResources(dataType, (path) -> path.getPath().endsWith(".png"));
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            String string = entry.getKey().getPath();
            ResourceLocation imageIdentifier = ResourceLocation.fromNamespaceAndPath(entry.getKey().getNamespace(), string.substring(dataTypeLength, string.length() - fileSuffixLength));

            try {
                ResourceLocation jsonIdentifier = ResourceLocation.fromNamespaceAndPath(entry.getKey().getNamespace(), string.replace(".png", ".json"));
                Optional<Resource> resource = manager.getResource(jsonIdentifier);

                if (resource.isEmpty()) {
                    LOGGER.error("Couldn't load painting {} from {}: no resource present", imageIdentifier, entry.getKey());
                    continue;
                }

                InputStreamReader reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8);
                JsonObject jsonElement = Objects.requireNonNull(GsonHelper.fromJson(GSON, reader, JsonElement.class)).getAsJsonObject();

                int width = GsonHelper.getAsInt(jsonElement, "width", 1);
                int height = GsonHelper.getAsInt(jsonElement, "height", 1);
                int resolution = GsonHelper.getAsInt(jsonElement, "resolution", 32);
                String name = GsonHelper.getAsString(jsonElement, "name", "unknown");
                String author = GsonHelper.getAsString(jsonElement, "author", "unknown");
                boolean graffiti = GsonHelper.getAsBoolean(jsonElement, "graffiti", false);
                //String hash = entry.getKey().toString().replaceAll("[^a-zA-Z\\d]", "");
                String hash = (name + author + resolution + "px").replaceAll("[^a-zA-Z\\d]", "").toLowerCase();

                Painting painting = new Painting(width, height, resolution, name, author, UUID.randomUUID(), Painting.Type.DATAPACK, false, graffiti, hash);

                map.put(painting.location(), Map.entry(painting, entry.getValue()));
            } catch (IllegalArgumentException | IOException | JsonParseException exception) {
                LOGGER.error("Couldn't load painting {} from {} ({})", imageIdentifier, entry.getKey(), exception);
            }
        }
        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, Entry<Painting, Resource>> prepared, ResourceManager manager, ProfilerFiller profiler) {
        ServerPaintingManager.setDatapackPaintings(prepared);
    }
}
