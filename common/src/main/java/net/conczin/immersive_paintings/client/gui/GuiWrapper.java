package net.conczin.immersive_paintings.client.gui;

import net.minecraft.client.Minecraft;

import java.util.UUID;

public class GuiWrapper {
    public static void open(UUID entityId) {
        Minecraft.getInstance().setScreen(new ImmersivePaintingScreen(entityId));
    }
}
