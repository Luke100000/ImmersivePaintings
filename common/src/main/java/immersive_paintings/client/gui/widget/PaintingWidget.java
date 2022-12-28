package immersive_paintings.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import immersive_paintings.resources.Painting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Supplier;

public class PaintingWidget extends DefaultButtonWidget {
    public final Painting.Texture thumbnail;
    private final PressAction onPressRight;
    private int button;

    public PaintingWidget(Painting.Texture thumbnail, int x, int y, int width, int height, PressAction onPress, PressAction onPressRight, Supplier<List<OrderedText>> tooltipSupplier) {
        super(x, y, width, height, Text.literal("Painting"), onPress, tooltipSupplier);
        this.onPressRight = onPressRight;
        this.thumbnail = thumbnail;
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
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, thumbnail.textureIdentifier);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        matrices.push();
        int tw = thumbnail.image == null ? 32 : thumbnail.image.getWidth();
        int th = thumbnail.image == null ? 32 : thumbnail.image.getHeight();
        float scale = Math.min((float)width / tw, (float)height / th);
        if (isHovered()) {
            scale *= 1.1;
        }
        matrices.translate(getX() + (this.width - tw * scale) / 2, getY() + (this.height - th * scale) / 2, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        drawTexture(matrices, 0, 0, 0, 0, tw, th, tw, th);
        matrices.pop();
    }
}

