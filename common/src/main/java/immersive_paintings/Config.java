package immersive_paintings;

public final class Config extends JsonConfig {
    private static final Config INSTANCE = loadOrCreate();

    public static Config getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unused")
    public String README = "https://github.com/Luke100000/ImmersivePaintings/wiki/Config";

    public int thumbnailSize = 128;
    public int lodResolutionMinimum = 32;

    public float halfResolutionThreshold = 2.0f;
    public float quarterResolutionThreshold = 4.0f;
    public float eighthResolutionThreshold = 8.0f;

    public int maxPacketsPerSecond = 20;
    public int packetSize = 16 * 1024;

    public boolean testIfSpaceEmpty = false;

    public int maxUserImageWidth = 4096;
    public int maxUserImageHeight = 4096;
    public int maxUserImages = 1000;

    public int minPaintingResolution = 8;
    public int maxPaintingResolution = 256;

    public boolean showOtherPlayersPaintings = true;
    public int uploadPermissionLevel = 0;
}
