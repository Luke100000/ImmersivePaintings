package net.conczin.immersive_paintings.neoforge.platform;

import net.conczin.immersive_paintings.platform.IPlatformHelper;
import net.neoforged.fml.ModList;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
