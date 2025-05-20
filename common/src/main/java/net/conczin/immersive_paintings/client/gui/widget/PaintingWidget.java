package net.conczin.immersive_paintings.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;

import net.conczin.immersive_paintings.painting.ClientPaintingManager;
import net.conczin.immersive_paintings.painting.Painting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

public class PaintingWidget extends Button {
    private ResourceLocation identifier;
    private BufferedImage image;
    private int paintingWidth = 32;
    private int paintingHeight = 32;

    private final Button.OnPress onPressRight;
    private int button;

    public PaintingWidget(ResourceLocation identifier, int x, int y, int width, int height, Button.OnPress onPress, Button.OnPress onPressRight) {
        super(x, y, width, height, Component.literal("Painting"), onPress, Button.DEFAULT_NARRATION);
        this.onPressRight = onPressRight;
        update(identifier, null);
    }

    @Nullable
    public BufferedImage getImage() {
        return image;
    }

    // Specifically for screenshots where the image needs to be stored so it can be created when clicked
    public void update(ResourceLocation identifier, BufferedImage image) {
        if (identifier == null)
            return;

        this.identifier = identifier;

        if (image == null) {
            Painting painting = ClientPaintingManager.getPainting(identifier);

            paintingWidth = painting.width();
            paintingHeight = painting.height();
        } else {
            this.image = image;

            paintingWidth = image.getWidth();
            paintingHeight = image.getHeight();
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
        //int tw = image == null ? 32 : image.getWidth();
        //int th = image == null ? 32 : image.getHeight();
        int tw = paintingWidth;
        int th = paintingHeight;
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