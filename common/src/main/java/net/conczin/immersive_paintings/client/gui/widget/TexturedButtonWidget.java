package net.conczin.immersive_paintings.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TexturedButtonWidget extends Button {
    private final int tw, th, w, h;
    private final ResourceLocation texture;

    public TexturedButtonWidget(int x, int y, int width, int height, ResourceLocation texture, int tw, int th, Component message, Button.OnPress onPress) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        this.texture = texture;
        this.w = width;
        this.h = height;
        this.tw = tw;
        this.th = th;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (isHovered()) {
            RenderSystem.setShaderColor(1.0f, 0.75f, 0.75f, alpha);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        }

        graphics.blit(texture, getX(), getY(), 0, (active ? 0 : 16), w, h, tw, th);

        int j = active ? 0xFFFFFF : 0xA0A0A0;
        graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2, j | Mth.ceil(alpha * 255.0f) << 24);
    }
}