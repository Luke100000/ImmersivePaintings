package immersive_paintings.dev;

import immersive_paintings.client.gui.ImmersivePaintingScreen.PixelatorSettings;
import net.minecraft.client.texture.NativeImage;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static immersive_paintings.client.gui.ImmersivePaintingScreen.pixelateImage;

public class DatapackPaintingsGenerator {
    static String base = "../paintings/";
    static String output = "../common/src/main/resources/data/immersive_paintings/paintings/";

    public static void run() throws IOException {
        process("Airships - AlexRuizArt.jpg", 3, 2, 0.25, 12, 32, false);
        process("Autumn - NostalgiaTree.png", 3, 2, 0.0, 0, 32, true);
        process("Beyond Hill and Dale - Alena Aenami.jpg", 3, 2, 0.25, 12, 32, false);
        process("Calm Days by the Lake - Scilex.png", 3, 2, 0, 0, 32, true);
        process("Cavern - sennin13.png", 3, 2, 0, 0, 32, true);
        process("concept - BINU BALAN.jpg", 3, 2, 0.25, 12, 32, false);
        process("Cthulhu - Andree Wallin.jpg", 3, 2, 0.25, 12, 32, false);
        process("Dark Phoenix - Starkiteckt.png", 3, 2, 0.25, 12, 32, false);
        process("Frost Valley - Jorge Jacinto.jpg", 3, 2, 0.25, 12, 32, false);
        process("Hazy Castle - David Frasheski.jpg", 3, 2, 0.25, 12, 32, false);
        process("Ikran Nebula - Starkiteckt.png", 3, 2, 0.25, 12, 32, false);
        process("Lake - Sarel Theron.jpg", 3, 2, 0.25, 12, 32, false);
        process("Life In The Sky - Vladimir Kostuchek.jpg", 3, 2, 0.25, 12, 32, false);
        process("Nevermore - Josef Bartoň.jpg", 3, 2, 0.25, 12, 32, false);
        process("Small Memory - Mikael Gustafsson.jpg", 3, 2, 0.25, 12, 32, false);
        process("The Ancient Ones - Jessica Woulfe.jpg", 3, 2, 0.25, 12, 32, false);
        process("The Esteemed Palace - Chris Ostrowski.png", 3, 2, 0.25, 12, 32, false);
        process("The Lost Legend - Xu Tianhua.jpg", 3, 2, 0.25, 12, 32, false);
        process("The Torch - Starkiteckt.png", 3, 2, 0.25, 12, 32, false);
        process("Tranquil Sunday Walk - Scilex.png", 3, 2, 0.25, 12, 32, false);
        process("Underwater - tox5000.png", 3, 2, 0.25, 12, 32, false);
        process("unnamed - Wedding-Pristine", 3, 2, 0.25, 12, 32, false);
        process("Windmill Town - Darek Zabrocki.jpg", 3, 2, 0.25, 12, 32, false);

        System.exit(0);
    }

    private static void process(String path, int width, int height, double dither, int colors, int resolution, boolean pixelArt) throws IOException {
        NativeImage image = loadImage(path);
        String[] split = path.split(" - ");

        FileWriter writer = new FileWriter(output + path.replace(".png", ".json").replace(".jpg", ".json"));
        writer.write(String.format("{\n\t\"name\": \"%s\",\n\t\"author\": \"%s\",\n\t\"resolution\": %d,\n\t\"width\": %d,\n\t\"height\": %d\n}", split[0], split[1], resolution, width, height));
        writer.close();

        PixelatorSettings settings = new PixelatorSettings(dither, colors, resolution, width, height, 0.5, 0.5, 1.0, pixelArt);

        pixelateImage(image, settings).writeTo(output + path.replace(".jpg", ".png"));
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
