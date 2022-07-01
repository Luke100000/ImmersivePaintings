package immersive_paintings.dev;

import immersive_paintings.client.gui.ImmersivePaintingScreen.PixelatorSettings;
import immersive_paintings.util.ImageManipulations;
import net.minecraft.client.texture.NativeImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

import static immersive_paintings.client.gui.ImmersivePaintingScreen.pixelateImage;

public class DatapackPaintingsGenerator {
    static String base = "../paintings/";
    static String output = "../common/src/main/resources/data/immersive_paintings/paintings/";

    public static void run() throws IOException {
        //noinspection ResultOfMethodCallIgnored
        new File(output).mkdirs();

        for (int res : new int[] {16, 32, 64, 128}) {
            process("Airships - AlexRuizArt.jpg", 5, 3, 0.25, 0, res, false);
            process("Autumn - NostalgiaTree.png", 3, 2, 0.0, 0, res, false);
            process("Beyond Hill and Dale - Alena Aenami.jpg", 2, 1, 0.25, 12, res, false);
            process("Calm Days by the Lake - Scilex.png", 2, 2, 0.25, 12, res, false);
            process("Cavern - sennin13.png", 2, 2, 0.25, 12, res, false);
            process("concept - BINU BALAN.jpg", 4, 2, 0.25, 12, res, false);
            process("Cthulhu - Andree Wallin.jpg", 3, 2, 0.25, 12, res, false);
            process("Dark Phoenix - Starkiteckt.png", 4, 2, 0, 32, res, false);
            process("Frost Valley - Jorge Jacinto.jpg", 3, 2, 0.25, 12, res, false);
            process("Hazy Castle - David Frasheski.jpg", 5, 2, 0, 12, res, false);
            process("Ikran Nebula - Starkiteckt.png", 2, 1, 0, 32, res, false);
            process("Lake - Sarel Theron.jpg", 3, 2, 0.25, 12, res, false);
            process("Life In The Sky - Vladimir Kostuchek.jpg", 3, 2, 0.25, 12, res, false);
            process("Nevermore - Josef Barton.jpg", 3, 2, 0.25, 12, res, false);
            process("Small Memory - Mikael Gustafsson.jpg", 3, 2, 0.0, 12, res, false);
            process("The Ancient Ones - Jessica Woulfe.jpg", 2, 1, 0.5, 12, res, false);
            process("The Esteemed Palace - Chris Ostrowski.png", 2, 2, 0.25, 12, res, false);
            process("The Lost Legend - Xu Tianhua.jpg", 3, 2, 0.25, 12, res, false);
            process("The Torch - Starkiteckt.png", 3, 3, 0, 32, res, false);
            process("Tranquil Sunday Walk - Scilex.png", 4, 4, 0.0, 0, res, true);
            process("Underwater - tox5000.png", 4, 4, 0, 0, res, true);
            process("unnamed - Wedding-Pristine", 4, 2, 0, 32, res, false);
            process("Windmill Town - Darek Zabrocki.jpg", 5, 3, 0.25, 12, res, false);
        }

        System.exit(0);
    }

    private static void process(String name, int width, int height, double dither, int colors, int resolution, boolean pixelArt) throws IOException {
        NativeImage image = loadImage(name);

        int zoom = ImageManipulations.scanForPixelArtMultiple(image);

        if (image.getWidth() / zoom < width * resolution || image.getHeight() / zoom < height * resolution) {
            return;
        }

        String[] split = name
                .replace(".png", "")
                .replace(".jpg", "")
                .split(" - ");

        name = output + name.toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace(".png", "")
                .replace(".jpg", "") + "-" + resolution + "px";

        FileWriter writer = new FileWriter(name + ".json");
        writer.write(String.format("{\n\t\"name\": \"%s\",\n\t\"author\": \"%s\",\n\t\"resolution\": %d,\n\t\"width\": %d,\n\t\"height\": %d\n}", split[0], split[1], resolution, width, height));
        writer.close();

        PixelatorSettings settings = new PixelatorSettings(dither / 2, colors, resolution, width, height, 0.5, 0.5, 1.0, pixelArt);

        pixelateImage(image, settings).writeTo(name + ".png");
    }

    private static NativeImage loadImage(String path) throws IOException {
        return loadImage(Path.of(base, path));
    }

    private static NativeImage loadImage(Path path) throws IOException {
        FileInputStream stream = new FileInputStream(path.toFile());
        NativeImage nativeImage = NativeImage.read(stream);
        stream.close();
        return nativeImage;
    }
}
