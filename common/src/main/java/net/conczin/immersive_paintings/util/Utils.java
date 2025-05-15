package net.conczin.immersive_paintings.util;

import net.conczin.immersive_paintings.Config;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Arrays;
import java.util.Locale;

public class Utils {
    public static String identifierToTranslation(ResourceLocation location) {
        String s = location.getPath();
        String lastSplit = s.substring(s.lastIndexOf("/") + 1);

        int i = lastSplit.lastIndexOf(".");
        return i <  0 ? lastSplit : lastSplit.substring(0, i);
    }

    public static void processByteArrayInChunks(byte[] is, TriConsumer<byte[], Integer, Integer> consumer) {
        int splits = (int)Math.ceil((double)is.length / Config.getInstance().packetSize);
        int split = 0;
        for (int i = 0; i < is.length; i += Config.getInstance().packetSize) {
            byte[] b = Arrays.copyOfRange(is, i, Math.min(is.length, i + Config.getInstance().packetSize));
            consumer.accept(b, split, splits);
            split++;
        }
    }

    public static String escapeString(String string) {
        return string.toLowerCase(Locale.ROOT).replaceAll("[^a-z\\d_.-]", "");
    }

    public interface RegisterHelper<T> {
        void register(ResourceLocation name, T value);
    }
}
