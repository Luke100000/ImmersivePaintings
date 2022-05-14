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
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class Paintings extends SinglePreparationResourceReloader<Map<Identifier, Paintings.PaintingData>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FILE_SUFFIX_LENGTH = ".json".length();
    private final Gson gson = new GsonBuilder().create();
    String dataType = "paintings";

    public static Map<Identifier, PaintingData> paintings;

    @Override
    protected Map<Identifier, PaintingData> prepare(ResourceManager manager, Profiler profiler) {
        Map<Identifier, Paintings.PaintingData> map = Maps.newHashMap();
        int dataTypeLength = dataType.length() + 1;

        for (Identifier identifier : manager.findResources(dataType, (path) -> path.endsWith(".png"))) {
            String string = identifier.getPath();
            Identifier name = new Identifier(identifier.getNamespace(), string.substring(dataTypeLength, string.length() - FILE_SUFFIX_LENGTH));

            try {
                NativeImage nativeImage = NativeImage.read(manager.getResource(identifier).getInputStream());
                Identifier jsonIdentifier = new Identifier(identifier.getNamespace(), string.replace(".png", ".json"));

                PaintingData data = null;
                if (manager.containsResource(jsonIdentifier)) {
                    InputStream inputStream = manager.getResource(jsonIdentifier).getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    JsonObject jsonElement = Objects.requireNonNull(JsonHelper.deserialize(gson, reader, JsonElement.class)).getAsJsonObject();

                    int resolution = jsonElement.get("resolution").getAsInt();
                    data = new PaintingData(nativeImage,
                            nativeImage.getWidth() / resolution,
                            nativeImage.getHeight() / resolution,
                            resolution);
                }

                if (data == null) {
                    data = new PaintingData(nativeImage,
                            nativeImage.getWidth() / 32,
                            nativeImage.getHeight() / 32,
                            32);
                }

                map.put(identifier, data);
            } catch (IllegalArgumentException | IOException | JsonParseException exception) {
                LOGGER.error("Couldn't load painting {} from {} ({})", name, identifier, exception);
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<Identifier, PaintingData> prepared, ResourceManager manager, Profiler profiler) {
        paintings = prepared;
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static final class PaintingData implements Serializable {
        transient public final NativeImage image;
        public final int width;
        public final int height;
        public final int resolution;

        public PaintingData(NativeImage image, int width, int height, int resolution) {
            this.image = image;
            this.width = width;
            this.height = height;
            this.resolution = resolution;
        }
    }
}
