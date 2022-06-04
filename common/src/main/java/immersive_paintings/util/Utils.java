package immersive_paintings.util;

import net.minecraft.util.Identifier;

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
}
