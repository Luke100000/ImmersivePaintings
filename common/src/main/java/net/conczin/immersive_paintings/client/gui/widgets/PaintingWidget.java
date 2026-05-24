package net.conczin.immersive_paintings.client.gui.widgets;

import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.registry.Config;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
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
    public void update(Identifier identifier, BufferedImage image) {
        this.image = image;
        update(identifier, 4, 2);
    }

    public void update(Identifier identifier, int paintingWidth, int paintingHeight) {
        this.identifier = identifier;
        this.paintingWidth = paintingWidth * Config.CLIENT.thumbnailSize;
        this.paintingHeight = paintingHeight * Config.CLIENT.thumbnailSize;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (input instanceof MouseButtonEvent event) {
            if (event.button() == 0) {
                onPress.onPress(this);
            } else {
                onPressRight.onPress(this);
            }
        }
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return buttonInfo.button() == 0 || buttonInfo.button() == 1;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int i, int i1, float v) {
        Matrix3x2fStack matrix = graphics.pose();
        matrix.pushMatrix();
        float scale = Math.min((float)width / paintingWidth, (float)height / paintingHeight);
        if (isHovered()) {
            scale *= 1.1f;
        }
        matrix.translate(getX() + (width - paintingWidth * scale) / 2, getY() + (height - paintingHeight * scale) / 2);
        matrix.scale(scale, scale);
        graphics.blit(RenderPipelines.GUI_TEXTURED, identifier, 0, 0, 0, 0, paintingWidth, paintingHeight, paintingWidth, paintingHeight);
        matrix.popMatrix();
    }
}