package immersive_paintings.resources;

import immersive_paintings.Config;
import immersive_paintings.util.ImageManipulations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static immersive_paintings.resources.Painting.DEFAULT;

public class ServerPaintingManager {

    public static MinecraftServer server;
    private static Map<Identifier, Painting> datapackPaintings = new HashMap<>();

    public static CustomServerPaintingsState get() {
        return server.getOverworld().getPersistentStateManager()
                .getOrCreate(CustomServerPaintingsState.getPersistentStateType(), "immersive_paintings");
    }

    public static Map<Identifier, Painting> getDatapackPaintings() {
        return datapackPaintings;
    }

    public static void setDatapackPaintings(Map<Identifier, Painting> datapackPaintings) {
        ServerPaintingManager.datapackPaintings = datapackPaintings;
    }

    public static Path getPaintingPath(Identifier identifier) {
        return Path.of("immersive_paintings", identifier.toString().replace(":", "_") + ".png");
    }

    public static void registerPainting(Identifier identifier, Painting painting) {
        Painting.Texture texture = painting.getTexture(Painting.Type.FULL);
        if (texture.image != null) {
            Path path = getPaintingPath(identifier);
            //noinspection ResultOfMethodCallIgnored
            new File(path.getParent().toString()).mkdirs();
            texture.image.write(path.toFile());
        }

        get().getCustomServerPaintings().put(identifier, painting);
        get().setDirty(true);
    }

    public static void deregisterPainting(Identifier identifier) {
        get().getCustomServerPaintings().remove(identifier);
        get().setDirty(true);
        //noinspection ResultOfMethodCallIgnored
        getPaintingPath(identifier).toFile().delete();
    }

    public static Painting getPainting(Identifier i) {
        if (datapackPaintings.containsKey(i)) {
            return datapackPaintings.get(i);
        } else {return get().customServerPaintings.getOrDefault(i, DEFAULT);}
    }

    public static Optional<ByteImage> getImage(Identifier i, Painting.Type type) {
        Optional<byte[]> data = getImageData(i, type);
        if (data.isPresent()) {
            try {
                return Optional.of(ByteImage.read(data.get()));
            } catch (IOException ignored) {}
        }
        return Optional.empty();
    }

    public static Optional<byte[]> getImageData(Identifier i, Painting.Type type) {
        Painting painting = getPainting(i);
        Painting.Texture texture = painting.getTexture(type);

        byte[] data = null;

        if (type == Painting.Type.FULL) {
            if (texture.image == null) {
                try {
                    if (texture.resource != null) {
                        data = texture.getResource();
                    } else if (get().customServerPaintings.containsKey(i)) {
                        try (FileInputStream stream = new FileInputStream(getPaintingPath(i).toString())) {
                            data = stream.readAllBytes();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                data = texture.image.encode();
            }
        } else {
            Optional<byte[]> imageData = Cache.getData(texture);
            if (imageData.isPresent()) {
                data = imageData.get();
            } else {
                Optional<ByteImage> optionalByteImage = getImage(i, Painting.Type.FULL);

                if (optionalByteImage.isPresent()) {
                    ByteImage image = optionalByteImage.get();
                    int w, h;
                    if (type == Painting.Type.THUMBNAIL) {
                        float zoom = Math.min(
                                (float)Config.getInstance().thumbnailSize / image.getWidth(),
                                (float)Config.getInstance().thumbnailSize / image.getHeight()
                        );

                        // The thumbnail would not be smaller than the actual painting
                        if (zoom >= 1.0f) {
                            return getImageData(i, Painting.Type.FULL);
                        }

                        w = (int)(image.getWidth() * zoom);
                        h = (int)(image.getHeight() * zoom);
                    } else if (type == Painting.Type.HALF) {
                        w = image.getWidth() / 2;
                        h = image.getHeight() / 2;
                    } else if (type == Painting.Type.QUARTER) {
                        w = image.getWidth() / 4;
                        h = image.getHeight() / 4;
                    } else {
                        w = image.getWidth() / 8;
                        h = image.getHeight() / 8;
                    }

                    // resize it
                    ByteImage target = new ByteImage(w, h);
                    ImageManipulations.resize(target, image, (double)image.getWidth() / w, 0, 0);
                    texture.image = target;

                    // encode it for networking
                    data = texture.image.encode();

                    // cache it
                    Cache.set(texture, data);
                }
            }
        }

        return Optional.ofNullable(data);
    }
}
