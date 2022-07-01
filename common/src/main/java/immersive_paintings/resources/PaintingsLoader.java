package immersive_paintings.resources;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class PaintingsLoader extends SinglePreparationResourceReloader<Map<Identifier, Painting>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FILE_SUFFIX_LENGTH = ".json".length();
    private final Gson gson = new GsonBuilder().create();
    String dataType = "paintings";

    @Override
    protected Map<Identifier, Painting> prepare(ResourceManager manager, Profiler profiler) {
        Map<Identifier, Painting> map = Maps.newHashMap();
        int dataTypeLength = dataType.length() + 1;

        for (Identifier identifier : manager.findResources(dataType, (path) -> path.endsWith(".png"))) {
            String string = identifier.getPath();
            Identifier imageIdentifier = new Identifier(identifier.getNamespace(), string.substring(dataTypeLength, string.length() - FILE_SUFFIX_LENGTH));

            try {
                NativeImage nativeImage = NativeImage.read(manager.getResource(identifier).getInputStream());
                Identifier jsonIdentifier = new Identifier(identifier.getNamespace(), string.replace(".png", ".json"));

                String hash = DigestUtils.sha1Hex(identifier.toString());

                Painting data = null;
                if (manager.containsResource(jsonIdentifier)) {
                    InputStream inputStream = manager.getResource(jsonIdentifier).getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    JsonObject jsonElement = Objects.requireNonNull(JsonHelper.deserialize(gson, reader, JsonElement.class)).getAsJsonObject();

                    int resolution = JsonHelper.getInt(jsonElement, "resolution", 32);
                    String name = JsonHelper.getString(jsonElement, "name", "unknown");
                    String author = JsonHelper.getString(jsonElement, "author", "unknown");

                    data = new Painting(nativeImage, resolution, name, author, true, hash);
                }

                if (data == null) {
                    data = new Painting(nativeImage, 32, "unknown", "unknown", true, hash);
                }

                map.put(identifier, data);
            } catch (IllegalArgumentException | IOException | JsonParseException exception) {
                LOGGER.error("Couldn't load painting {} from {} ({})", imageIdentifier, identifier, exception);
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<Identifier, Painting> prepared, ResourceManager manager, Profiler profiler) {
        ServerPaintingManager.setDatapackPaintings(prepared);
    }
}
