package immersive_paintings.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import immersive_paintings.resources.Paintings;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class PaintingWidget extends ButtonWidget {
    private final Paintings.PaintingData painting;

    public PaintingWidget(Paintings.PaintingData painting, int x, int y, int width, int height, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, width, height, new LiteralText("Painting"), onPress, tooltipSupplier);
        this.painting = painting;
    }

    @Override
    public void onPress() {
        onPress.onPress(this);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, painting.textureIdentifier);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        
        matrices.push();
        int tw = painting.getPixelWidth();
        int th = painting.getPixelHeight();
        float scale = Math.min((float)width / tw, (float)height / th);
        if (isHovered()) {
            scale *= 1.1;
        }
        matrices.translate(x + (this.width - tw * scale) / 2, y + (this.height - th * scale) / 2, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        drawTexture(matrices, 0, 0, 0, 0, tw, th, tw, th);
        matrices.pop();

        if (isHovered()) {
            renderTooltip(matrices, mouseX, mouseY);
        }
    }
}

