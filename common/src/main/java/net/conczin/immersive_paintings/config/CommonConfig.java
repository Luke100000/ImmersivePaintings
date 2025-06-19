package net.conczin.immersive_paintings.config;

import me.fzzyhmstrs.fzzy_config.annotations.Translation;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigGroup;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.registration.Configs;

@Translation(prefix = Configs.CONFIG_ID + "common")
public class CommonConfig extends Config {
    public CommonConfig() {
        super(Main.locate("common_config"));
    }

    // TODO Options: Restrict editing to painting owner

    public ConfigGroup generalGroup = new ConfigGroup("general");
    public boolean testIfSpaceEmpty = false;
    public boolean paintingsHaveCollision = false;
    public boolean showOtherPlayersPaintings = true;
    @ConfigGroup.Pop
    public int uploadPermissionLevel = 0;

    public ConfigGroup advancedGroup = new ConfigGroup("advanced");
    public int maxUserImageWidth = 4096;
    public int maxUserImageHeight = 4096;
    public int maxUserImages = 1000;

    public int minPaintingResolution = 8;
    public int maxPaintingResolution = 256;

    // Advanced
    public int maxPacketsPerSecond = 20;
    public int packetSize = 64 * 1024;
    @ConfigGroup.Pop
    public int packetSplitInterval = 250;
}
