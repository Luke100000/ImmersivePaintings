package net.conczin.immersive_paintings;

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;
import net.conczin.immersive_paintings.dev.DatapackPaintingsGenerator;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import java.io.IOException;

public final class Main {
    public static final String MOD_ID = "immersive_paintings";
    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation locate(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void init() {
        if (!ImageIO.getImageReadersByFormatName("webp").hasNext())
            IIORegistry.getDefaultInstance().registerServiceProvider(new WebPImageReaderSpi());
    }

    static {
        //noinspection ConstantConditions
        if (false) {
            try {
                DatapackPaintingsGenerator.run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
