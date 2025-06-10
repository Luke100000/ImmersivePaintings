package net.conczin.immersive_paintings.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;

import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.registration.Configs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

public class PaintingWidget extends Button {
    private BufferedImage image;
    private ResourceLocation identifier = Painting.DEFAULT_IDENTIFIER;

    private int paintingWidth = 256;
    private int paintingHeight = 256;

    private final Button.OnPress onPressRight;
    private int button;

    public PaintingWidget(int x, int y, int width, int height, Button.OnPress onPress, Button.OnPress onPressRight) {
        super(x, y, width, height, Component.literal("Painting"), onPress, Button.DEFAULT_NARRATION);
        this.onPressRight = onPressRight;
    }

    @Nullable
    public BufferedImage getImage() {
        return image;
    }

    // Specifically for screenshots where the image needs to be stored so it can be created when clicked
    public void update(ResourceLocation identifier, BufferedImage image) {
        this.image = image;
        update(identifier, 4, 2);
    }

    public void update(ResourceLocation identifier, int paintingWidth, int paintingHeight) {
        int thumbnailSize = Configs.CLIENT.thumbnailSize;

        this.identifier = identifier;
        this.paintingWidth = paintingWidth * thumbnailSize;
        this.paintingHeight = paintingHeight * thumbnailSize;
    }

    @Override
    public void onPress() {
        if (button == 0) {
            onPress.onPress(this);
        } else {
            onPressRight.onPress(this);
        }
    }

    @Override
    protected boolean isValidClickButton(int button) {
        this.button = button;
        return button == 0 || button == 1;
    }

    // TODO: Fix
    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        float scale = Math.min((float)width / paintingWidth, (float)height / paintingHeight);
        if (isHovered()) {
            scale *= 1.1f;
        }
        pose.translate(getX() + (width - paintingWidth * scale) / 2, getY() + (height - paintingHeight * scale) / 2, 0.0f);
        pose.scale(scale, scale, 1.0f);
        graphics.blit(identifier, 0, 0, 0, 0, paintingWidth, paintingHeight, paintingWidth, paintingHeight);
        pose.popPose();
    }
}