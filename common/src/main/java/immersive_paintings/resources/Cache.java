package immersive_paintings.resources;

import net.minecraft.client.texture.NativeImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

public class Cache {
    private static File getFile(String key) {
        return new File("./immersive_paintings_cache/" + key + ".png");
    }

    public static Optional<NativeImage> get(Painting.Texture texture) {
        File file = getFile(texture.hash);

        if (!file.exists()) {
            return Optional.empty();
        }

        try {
            FileInputStream stream = new FileInputStream(file.getPath());
            return Optional.of(NativeImage.read(stream));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static void set(Painting.Texture texture) {
        try {
            if (texture.image != null) {
                File file = getFile(texture.hash);
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
                texture.image.writeTo(file);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
