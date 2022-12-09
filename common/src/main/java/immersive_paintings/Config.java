package immersive_paintings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public final class Config implements Serializable {
    @Serial
    private static final long serialVersionUID = 9132405079466337851L;

    private static final Config INSTANCE = loadOrCreate();

    public static Config getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unused")
    public String README = "https://github.com/Luke100000/ImmersivePaintings/wiki/Config";

    public static final int VERSION = 1;

    public int thumbnailSize = 128;
    public int lodResolutionMinimum = 32;

    public float halfResolutionThreshold = 2.0f;
    public float quarterResolutionThreshold = 4.0f;
    public float eighthResolutionThreshold = 8.0f;

    public int maxPacketsPerSecond = 20;
    public int packetSize = 16 * 1024;

    public boolean testIfSpaceEmpty = false;
    public boolean paintingsHaveCollision = false;

    public int maxUserImageWidth = 4096;
    public int maxUserImageHeight = 4096;
    public int maxUserImages = 1000;

    public int minPaintingResolution = 8;
    public int maxPaintingResolution = 256;

    public boolean showOtherPlayersPaintings = true;

    public int version = 0;

    public static File getConfigFile() {
        return new File("./config/" + Main.MOD_ID + ".json");
    }

    public void save() {
        //noinspection ResultOfMethodCallIgnored
        new File("./config").mkdirs();

        try (FileWriter writer = new FileWriter(getConfigFile())) {
            version = VERSION;
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config loadOrCreate() {
        try (FileReader reader = new FileReader(getConfigFile())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Config config = gson.fromJson(reader, Config.class);
            if (config.version != VERSION) {
                config = new Config();
            }
            config.save();
            return config;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Config config = new Config();
        config.save();
        return config;
    }
}
