package immersive_paintings.util;

import immersive_paintings.Config;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Arrays;
import java.util.Locale;

public class Utils {
    public static double cosNoise(double time) {
        return cosNoise(time, 5);
    }

    public static double cosNoise(double time, int layers) {
        double value = 0.0f;
        for (int i = 0; i < layers; i++) {
            value += Math.cos(time);
            time *= 1.7;
        }
        return value;
    }

    public static String lastSplit(String string, String sep) {
        return string.substring(string.lastIndexOf(sep) + 1);
    }

    public static String firstSplit(String string, String sep) {
        int i = string.lastIndexOf(sep);
        if (i < 0) {
            return string;
        } else {
            return string.substring(0, i);
        }
    }

    public static String identifierToTranslation(Identifier identifier) {
        return firstSplit(lastSplit(identifier.getPath(), "/"), ".");
    }

    public static void processByteArrayInChunks(byte[] is, TriConsumer<byte[], Integer, Integer> consumer) {
        int splits = (int)Math.ceil((double)is.length / Config.getInstance().packetSize);
        int split = 0;
        for (int i = 0; i < is.length; i += Config.getInstance().packetSize) {
            byte[] ints = Arrays.copyOfRange(is, i, Math.min(is.length, i + Config.getInstance().packetSize));
            consumer.accept(ints, split, splits);
            split++;
        }
    }

    public static String escapeString(String string) {
        return string.toLowerCase(Locale.ROOT).replaceAll("[^a-z\\d_.-]", "");
    }
}
