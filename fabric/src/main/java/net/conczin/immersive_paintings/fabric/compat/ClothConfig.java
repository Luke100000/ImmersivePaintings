package net.conczin.immersive_paintings.fabric.compat;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.conczin.immersive_paintings.Config;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ClothConfig {
    public static Screen createConfigScreen(Screen parent) {
        Config config = Config.getInstance();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("cloth_config.immersive_paintings.settings"))
                .setSavingRunnable(config::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("cloth_config.immersive_paintings.category.general"));
        general.addEntry(makeEntry(entryBuilder, "testIfSpaceEmpty", config.testIfSpaceEmpty, newValue -> config.testIfSpaceEmpty = newValue));
        general.addEntry(makeEntry(entryBuilder, "nsfw", config.showNSFWPaintings, newValue -> config.showNSFWPaintings = newValue));
        general.addEntry(makeEntry(entryBuilder, "paintingsHaveCollision", config.paintingsHaveCollision, newValue -> config.paintingsHaveCollision = newValue));
        general.addEntry(makeEntry(entryBuilder, "showOtherPlayersPaintings", config.showOtherPlayersPaintings, newValue -> config.showOtherPlayersPaintings = newValue));
        general.addEntry(makeEntry(entryBuilder, "uploadPermissionLevel", config.uploadPermissionLevel, newValue -> config.uploadPermissionLevel = newValue));


        ConfigCategory image = builder.getOrCreateCategory(Component.translatable("cloth_config.immersive_paintings.category.image"));
        image.addEntry(makeEntry(entryBuilder, "thumbnailSize", config.thumbnailSize, newValue -> config.thumbnailSize = newValue));
        image.addEntry(makeEntry(entryBuilder, "lodResolutionMinimum", config.lodResolutionMinimum, newValue -> config.lodResolutionMinimum = newValue));

        image.addEntry(makeEntry(entryBuilder, "halfResolutionThreshold", config.halfResolutionThreshold, newValue -> config.halfResolutionThreshold = newValue));
        image.addEntry(makeEntry(entryBuilder, "quarterResolutionThreshold", config.quarterResolutionThreshold, newValue -> config.quarterResolutionThreshold = newValue));
        image.addEntry(makeEntry(entryBuilder, "eighthResolutionThreshold", config.eighthResolutionThreshold, newValue -> config.eighthResolutionThreshold = newValue));

        image.addEntry(makeEntry(entryBuilder, "maxUserImageWidth", config.maxUserImageWidth, newValue -> config.maxUserImageWidth = newValue));
        image.addEntry(makeEntry(entryBuilder, "maxUserImageHeight", config.maxUserImageHeight, newValue -> config.maxUserImageHeight = newValue));
        image.addEntry(makeEntry(entryBuilder, "maxUserImages", config.maxUserImages, newValue -> config.maxUserImages = newValue));

        image.addEntry(makeEntry(entryBuilder, "minPaintingResolution", config.minPaintingResolution, newValue -> config.minPaintingResolution = newValue));
        image.addEntry(makeEntry(entryBuilder, "maxPaintingResolution", config.maxPaintingResolution, newValue -> config.maxPaintingResolution = newValue));

        ConfigCategory advanced = builder.getOrCreateCategory(Component.translatable("cloth_config.immersive_paintings.category.advanced"));
        advanced.setDescription(new Component[]{Component.translatable("cloth_config.immersive_paintings.category.advanced.description")});
        advanced.addEntry(makeEntry(entryBuilder, "maxPacketsPerSecond", config.maxPacketsPerSecond, newValue -> config.maxPacketsPerSecond = newValue));
        advanced.addEntry(makeEntry(entryBuilder, "packetSize", config.packetSize, newValue -> config.packetSize = newValue));
        advanced.addEntry(makeEntry(entryBuilder, "packetSplitInterval", config.packetSplitInterval, newValue -> config.packetSplitInterval = newValue));

        return builder.build();
    }

    private static AbstractConfigListEntry<Boolean> makeEntry(ConfigEntryBuilder builder, String name, Boolean value, Consumer<Boolean> consumer) {
        return builder.startBooleanToggle(Component.translatable("cloth_config.immersive_paintings.option." + name), value)
                .setTooltip(Component.translatable("cloth_config.immersive_paintings.option." + name + ".tooltip"))
                .setSaveConsumer(consumer)
                .build();
    }

    private static AbstractConfigListEntry<Integer> makeEntry(ConfigEntryBuilder builder, String name, Integer value, Consumer<Integer> consumer) {
        return builder.startIntField(Component.translatable("cloth_config.immersive_paintings.option." + name), value)
                .setTooltip(Component.translatable("cloth_config.immersive_paintings.option." + name + ".tooltip"))
                .setSaveConsumer(consumer)
                .build();
    }

    private static AbstractConfigListEntry<Float> makeEntry(ConfigEntryBuilder builder, String name, Float value, Consumer<Float> consumer) {
        return builder.startFloatField(Component.translatable("cloth_config.immersive_paintings.option." + name), value)
                .setTooltip(Component.translatable("cloth_config.immersive_paintings.option." + name + ".tooltip"))
                .setSaveConsumer(consumer)
                .build();
    }
}
