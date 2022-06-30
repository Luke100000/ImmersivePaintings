package immersive_paintings.resources;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import immersive_paintings.Main;
import immersive_paintings.util.ImageManipulations;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class Paintings extends SinglePreparationResourceReloader<Map<Identifier, Paintings.PaintingData>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FILE_SUFFIX_LENGTH = ".json".length();
    private final Gson gson = new GsonBuilder().create();
    String dataType = "paintings";

    @Override
    protected Map<Identifier, PaintingData> prepare(ResourceManager manager, Profiler profiler) {
        Map<Identifier, Paintings.PaintingData> map = Maps.newHashMap();
        int dataTypeLength = dataType.length() + 1;

        for (Identifier identifier : manager.findResources(dataType, (path) -> path.endsWith(".png"))) {
            String string = identifier.getPath();
            Identifier imageIdentifier = new Identifier(identifier.getNamespace(), string.substring(dataTypeLength, string.length() - FILE_SUFFIX_LENGTH));

            try {
                NativeImage nativeImage = NativeImage.read(manager.getResource(identifier).getInputStream());
                Identifier jsonIdentifier = new Identifier(identifier.getNamespace(), string.replace(".png", ".json"));

                PaintingData data = null;
                if (manager.containsResource(jsonIdentifier)) {
                    InputStream inputStream = manager.getResource(jsonIdentifier).getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    JsonObject jsonElement = Objects.requireNonNull(JsonHelper.deserialize(gson, reader, JsonElement.class)).getAsJsonObject();

                    int resolution = JsonHelper.getInt(jsonElement, "author", 32);
                    String name = JsonHelper.getString(jsonElement, "name", "unknown");
                    String author = JsonHelper.getString(jsonElement, "author", "unknown");

                    data = new PaintingData(nativeImage, resolution, name, author, true);
                }

                if (data == null) {
                    data = new PaintingData(nativeImage, 32, "unknown", "unknown", true);
                }

                map.put(identifier, data);
            } catch (IllegalArgumentException | IOException | JsonParseException exception) {
                LOGGER.error("Couldn't load painting {} from {} ({})", imageIdentifier, identifier, exception);
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<Identifier, PaintingData> prepared, ResourceManager manager, Profiler profiler) {
        ServerPaintingManager.setDatapackPaintings(prepared);
    }

    public static final class PaintingData {
        public final int width;
        public final int height;
        public final int resolution;

        public final String name;
        public final String author;
        public boolean datapack;

        @Nullable
        public NativeImage image;

        public boolean requested = false;
        public Identifier textureIdentifier = Main.locate("textures/block/frame/canvas.png");

        public PaintingData(@Nullable NativeImage image, int width, int height, int resolution) {
            this(image, width, height, resolution, "", "", false);
        }

        public PaintingData(@Nullable NativeImage image, int width, int height, int resolution, String name, String author, boolean datapack) {
            this.image = image;
            this.width = width;
            this.height = height;
            this.resolution = resolution;
            this.name = name;
            this.author = author;
            this.datapack = datapack;
        }

        public PaintingData(NativeImage image, int resolution) {
            this(image, resolution, "", "", false);
        }

        public PaintingData(NativeImage image, int resolution, String name, String author, boolean datapack) {
            this.image = image;
            this.width = image.getWidth() / resolution;
            this.height = image.getHeight() / resolution;
            this.resolution = resolution;
            this.name = name;
            this.author = author;
            this.datapack = datapack;
        }

        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putInt("width", width);
            nbt.putInt("height", height);
            nbt.putInt("resolution", resolution);
            nbt.putString("name", name);
            nbt.putString("author", author);
            nbt.putBoolean("datapack", datapack);
            return nbt;
        }

        public NbtCompound toFullNbt() {
            assert image != null;
            NbtCompound nbt = toNbt();
            nbt.putIntArray("image", ImageManipulations.imageToInts(image));
            return nbt;
        }

        public static PaintingData fromNbt(NbtCompound nbt) {
            int width = nbt.getInt("width");
            int height = nbt.getInt("height");
            int resolution = nbt.getInt("resolution");
            String name = nbt.getString("name");
            String author = nbt.getString("author");
            boolean datapack = nbt.getBoolean("datapack");

            NativeImage image = null;
            if (nbt.contains("image")) {
                image = ImageManipulations.intsToImage(width * resolution, height * resolution, nbt.getIntArray("image"));
            }

            return new PaintingData(image, width, height, resolution, name, author, datapack);
        }

        public int getPixelWidth() {
            return width * resolution;
        }

        public int getPixelHeight() {
            return height * resolution;
        }
    }
}
