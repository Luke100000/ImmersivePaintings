package net.conczin.immersive_paintings.client.gui.widget;

import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.registration.Configs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.awt.image.BufferedImage;

public class PaintingWidget extends Button {
    private BufferedImage image;
    private Identifier identifier = Painting.DEFAULT_IDENTIFIER;

    private int paintingWidth = 256;
    private int paintingHeight = 256;

    private final Button.OnPress onPressRight;

    public PaintingWidget(int x, int y, int width, int height, Button.OnPress onPress, Button.OnPress onPressRight) {
        super(x, y, width, height, Component.literal("Painting"), onPress, Button.DEFAULT_NARRATION);
        this.onPressRight = onPressRight;
    }

    @Nullable
    public BufferedImage getImage() {
        return image;
    }

    // Specifically for screenshots where the image needs to be stored so it can be created when clicked
    public void update(Identifier identifier, BufferedImage image) {
        this.image = image;
        update(identifier, 4, 2);
    }

    public void update(Identifier identifier, int paintingWidth, int paintingHeight) {
        int thumbnailSize = Configs.CLIENT.thumbnailSize;

        this.identifier = identifier;
        this.paintingWidth = paintingWidth * thumbnailSize;
        this.paintingHeight = paintingHeight * thumbnailSize;
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo mouseButtonInfo) {
        return mouseButtonInfo.button() == 0 || mouseButtonInfo.button() == 1;
    }

    @Override
    public void onClick(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (mouseButtonEvent.buttonInfo().button() == 0) {
            onPress.onPress(this);
        } else {
            onPressRight.onPress(this);
        }
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        float scale = Math.min((float)width / paintingWidth, (float)height / paintingHeight);
        if (isHovered()) {
            scale *= 1.1f;
        }
        pose.translate(getX() + (width - paintingWidth * scale) / 2, getY() + (height - paintingHeight * scale) / 2);
        pose.scale(scale, scale);
        graphics.blit(identifier, 0, 0, paintingWidth, paintingHeight, 0.0f, 1.0f, 0.0f, 1.0f);
        pose.popMatrix();
    }
}