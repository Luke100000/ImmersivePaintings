package immersive_paintings;

import immersive_paintings.dev.DatapackPaintingsGenerator;
import immersive_paintings.network.NetworkManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public final class Main {
    public static final String SHORT_MOD_ID = "ic_ip";
    public static final String MOD_ID = "immersive_paintings";
    public static final Logger LOGGER = LogManager.getLogger();
    public static NetworkManager networkManager;

    public static Identifier locate(String path) {
        return new Identifier(MOD_ID, path);
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
