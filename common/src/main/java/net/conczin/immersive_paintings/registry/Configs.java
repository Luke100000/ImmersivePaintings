package net.conczin.immersive_paintings.registry;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.config.ClientConfig;
import net.conczin.immersive_paintings.config.CommonConfig;

import java.util.function.Supplier;

public class Configs {
    public static final String CONFIG_ID = "config." + Main.MOD_ID + ".";

    public static final CommonConfig COMMON = register(CommonConfig::new, RegisterType.BOTH);

    public static final ClientConfig CLIENT = register(ClientConfig::new, RegisterType.CLIENT);

    // noop class to load
    public static void init() {}

    private static <T extends Config> T register(Supplier<T> supplier, RegisterType type) {
        return ConfigApiJava.registerAndLoadConfig(supplier, type);
    }
}
