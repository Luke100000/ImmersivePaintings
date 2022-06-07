package immersive_paintings.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public abstract class ExtendedSliderWidget<T> extends SliderWidget {
    private T oldValue;
    final Consumer<T> onApplyValue;

    public ExtendedSliderWidget(int x, int y, int width, int height, Text text, double value, Consumer<T> onApplyValue) {
        super(x, y, width, height, text, value);
        this.onApplyValue= onApplyValue;
    }

    protected double getOpticalValue() {
        return value;
    }

    abstract T getValue();

    @Override
    protected void renderBackground(MatrixStack matrices, MinecraftClient client, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int i = (this.isHovered() ? 2 : 1) * 20;
        this.drawTexture(matrices, this.x + (int)(getOpticalValue() * (double)(this.width - 8)), this.y, 0, 46 + i, 4, 20);
        this.drawTexture(matrices, this.x + (int)(getOpticalValue() * (double)(this.width - 8)) + 4, this.y, 196, 46 + i, 4, 20);
    }

    @Override
    protected void applyValue() {
        T v = getValue();
        if (v != oldValue) {
            oldValue = v;
            onApplyValue.accept(v);
        }
    }
}
