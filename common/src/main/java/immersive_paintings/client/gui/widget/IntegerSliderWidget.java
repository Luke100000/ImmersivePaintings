package immersive_paintings.client.gui.widget;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.function.Consumer;

public class IntegerSliderWidget extends SliderWidget {
    private final int min;
    private final int max;
    private final Consumer<Integer> onApplyValue;
    private final String text;
    private double oldValue = -1;

    public IntegerSliderWidget(int x, int y, int width, int height, String text, double value, int min, int max, Consumer<Integer> onApplyValue) {
        super(x, y, width, height, new LiteralText(""), (value - min) / (max - min));
        this.min = min;
        this.max = max;
        this.onApplyValue = onApplyValue;
        this.text = text;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(new TranslatableText(text, getInteger()));
    }

    private int getInteger() {
        return (int)(value * (max - min) + min);
    }

    @Override
    protected void applyValue() {
        value = ((double)getInteger() - min) / (max - min);
        if (value != oldValue) {
            oldValue = value;
            onApplyValue.accept(getInteger());
        }
    }
}
