package net.conczin.immersive_paintings.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class TexturedButtonWidget extends Button {
    private final int tw, th, w, h;
    private final Identifier texture;

    public TexturedButtonWidget(int x, int y, int width, int height, Identifier texture, int tw, int th, Component message, Button.OnPress onPress) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        this.texture = texture;
        this.w = width;
        this.h = height;
        this.tw = tw;
        this.th = th;
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int color = isHovered() ? ARGB.color(Mth.ceil(alpha * 255), 255, 192, 192) : ARGB.white(Mth.ceil(alpha * 255));
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), 0.0f, active ? 0.0f : 16.0f, w, h, tw, th, color);

        int j = active ? 0xFFFFFF : 0xA0A0A0;
        graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2, j | Mth.ceil(alpha * 255.0f) << 24);
    }
}