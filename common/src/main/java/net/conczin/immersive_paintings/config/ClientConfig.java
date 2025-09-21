package net.conczin.immersive_paintings.config;

import me.fzzyhmstrs.fzzy_config.annotations.Translation;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigGroup;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.registration.Configs;

@Translation(prefix = Configs.CONFIG_ID + "client")
public class ClientConfig extends Config {
    public ClientConfig() {
        super(Main.locate("client_config"));
    }

    public ConfigGroup generalGroup = new ConfigGroup("general");
    public boolean showOtherPlayersPaintings = true;
    @ConfigGroup.Pop
    public boolean showNSFWPaintings = true;

    public ConfigGroup advancedGroup = new ConfigGroup("advanced");
    public int thumbnailSize = 128;
    public int lodResolutionMinimum = 32;

    public float halfResolutionThreshold = 4.0f;
    public float quarterResolutionThreshold = 8.0f;
    public float eighthResolutionThreshold = 16.0f;
    @ConfigGroup.Pop
    public float thumbResolutionThreshold = 32.0f;
}
