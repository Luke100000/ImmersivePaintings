package net.conczin.immersive_paintings.resources;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.minecraft.resources.Identifier;
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

public class PaintingsLoader extends SimplePreparableReloadListener<Map<Identifier, Entry<Painting, Resource>>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Gson GSON = new GsonBuilder().create();

    private static final String dataType = "paintings";
    private static final int dataTypeLength = dataType.length() + 1;
    private static final int fileSuffixLength = ".json".length();
    
    protected static final Identifier ID = Main.locate(dataType);

    @Override
    protected Map<Identifier, Entry<Painting, Resource>> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, Entry<Painting, Resource>> map = new HashMap<>();
        
        Map<Identifier, Resource> resources = manager.listResources(dataType, (path) -> path.getPath().endsWith(".png"));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            String string = entry.getKey().getPath();
            Identifier imageIdentifier = Identifier.fromNamespaceAndPath(entry.getKey().getNamespace(), string.substring(dataTypeLength, string.length() - fileSuffixLength));

            try {
                Identifier jsonIdentifier = Identifier.fromNamespaceAndPath(entry.getKey().getNamespace(), string.replace(".png", ".json"));
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
                String hash = entry.getKey().toString().replaceAll("[^a-zA-Z\\d]", "");

                EnumSet<Painting.Flag> flags = EnumSet.noneOf(Painting.Flag.class);
                if (graffiti)
                    flags.add(Painting.Flag.GRAFFITI);

                Painting painting = new Painting(width, height, resolution, name, author, UUID.randomUUID(), Painting.Type.DATAPACK, flags, hash);

                map.put(painting.location(), Map.entry(painting, entry.getValue()));
            } catch (IllegalArgumentException | IOException | JsonParseException exception) {
                LOGGER.error("Couldn't load painting {} from {} ({})", imageIdentifier, entry.getKey(), exception);
            }
        }
        return map;
    }

    @Override
    protected void apply(Map<Identifier, Entry<Painting, Resource>> prepared, ResourceManager manager, ProfilerFiller profiler) {
        ServerPaintingManager.setDatapackPaintings(prepared);
    }
}
