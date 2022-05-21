package immersive_paintings.client.gui.widget;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.function.Consumer;

public class DoubleSliderWidget extends SliderWidget {
    private final double min;
    private final double max;
    private final Consumer<Double> onApplyValue;
    final String text;

    public DoubleSliderWidget(int x, int y, int width, int height, String text, double value, Consumer<Double> onApplyValue) {
        this(x, y, width, height, text, value, 0, 1, onApplyValue);
    }

    public DoubleSliderWidget(int x, int y, int width, int height, String text, double value, double min, double max, Consumer<Double> onApplyValue) {
        super(x, y, width, height, new LiteralText(""), (value - min) / (max - min));
        this.min = min;
        this.max = max;
        this.onApplyValue = onApplyValue;
        this.text = text;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(new TranslatableText(text, String.format("%.2f", getDouble())));
    }

    double getDouble() {
        return value * (max - min) + min;
    }

    @Override
    protected void applyValue() {
        onApplyValue.accept(getDouble());
    }
}
