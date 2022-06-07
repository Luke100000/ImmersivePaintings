package immersive_paintings.client.gui.widget;

import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.function.Consumer;

public class DoubleSliderWidget extends ExtendedSliderWidget<Double> {
    final double min;
    final double max;
    final String text;

    public DoubleSliderWidget(int x, int y, int width, int height, String text, double value, double min, double max, Consumer<Double> onApplyValue) {
        super(x, y, width, height, new LiteralText(""), (value - min) / (max - min), onApplyValue);
        this.min = min;
        this.max = max;
        this.text = text;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(new TranslatableText(text, String.format("%.2f", getValue())));
    }

    @Override
    Double getValue() {
        return value * (max - min) + min;
    }
}
