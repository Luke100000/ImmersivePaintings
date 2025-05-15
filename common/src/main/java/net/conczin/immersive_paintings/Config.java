package net.conczin.immersive_paintings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Config INSTANCE;

    public static Config getInstance() {
        if (INSTANCE == null)
            INSTANCE = read();
        return INSTANCE;
    }

    // TODO Options:
    // - Restrict editing to painting owner
    // - NSFW Blur

    // General
    public boolean testIfSpaceEmpty = false;
    public boolean paintingsHaveCollision = false;

    public boolean showOtherPlayersPaintings = true;
    public int uploadPermissionLevel = 0;

    // Image
    public int thumbnailSize = 128;
    public int lodResolutionMinimum = 32;

    public float halfResolutionThreshold = 2.0f;
    public float quarterResolutionThreshold = 4.0f;
    public float eighthResolutionThreshold = 8.0f;

    public int maxUserImageWidth = 4096;
    public int maxUserImageHeight = 4096;
    public int maxUserImages = 1000;

    public int minPaintingResolution = 8;
    public int maxPaintingResolution = 256;

    // Advanced
    public int maxPacketsPerSecond = 20;
    public int packetSize = 16 * 1024;
    public int packetSplitInterval = 200;

    public int version = 0;

    int getVersion() {
        return 1;
    }

    public static File getConfigFile() {
        return new File("./config/" + Main.MOD_ID + ".json");
    }

    public void save() {
        try (FileWriter writer = new FileWriter(getConfigFile())) {
            version = getVersion();
            writer.write(GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config read() {
        if (getConfigFile().exists()) {
            try (FileReader reader = new FileReader(getConfigFile())) {
                Config config = GSON.fromJson(reader, Config.class);
                if (config.version != config.getVersion()) {
                    config = new Config();
                }
                config.save();
                return config;
            } catch (Exception e) {
                Main.LOGGER.error("Failed to load Immersive Paintings config! Default config is used for now. Delete the file to reset.");
                Main.LOGGER.error(e);
                return new Config();
            }
        } else {
            Config config = new Config();
            config.save();
            return config;
        }
    }
}
