package net.conczin.immersive_paintings.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;

import net.conczin.immersive_paintings.util.ByteImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PaintingWidget extends Button {
    public ByteImage image;
    public ResourceLocation identifier;

    private final Button.OnPress onPressRight;
    private int button;

    public PaintingWidget(ByteImage image, ResourceLocation identifier, int x, int y, int width, int height, Button.OnPress onPress, Button.OnPress onPressRight) {
        super(x, y, width, height, Component.literal("Painting"), onPress, Button.DEFAULT_NARRATION);
        this.onPressRight = onPressRight;
        this.image = image;
        this.identifier = identifier;
    }

    public void update(ResourceLocation identifier, ByteImage image) {
        if (identifier != null) {
            this.identifier = identifier;
        }

        if (image != null) {
            this.image = image;
        }
    }

    @Override
    public void onPress() {
        if (button == 0) {
            onPress.onPress(this);
        } else {
            onPressRight.onPress(this);
        }
        button = 0;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        this.button = button;
        return button == 0 || button == 1;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        int tw = image == null ? 32 : image.getWidth();
        int th = image == null ? 32 : image.getHeight();
        float scale = Math.min((float)width / tw, (float)height / th);
        if (isHovered()) {
            scale *= 1.1f;
        }
        pose.translate(getX() + (width - tw * scale) / 2, getY() + (height - th * scale) / 2, 0.0f);
        pose.scale(scale, scale, 1.0f);
        graphics.blit(identifier, 0, 0, 0, 0, tw, th, tw, th);
        pose.popPose();
    }
}