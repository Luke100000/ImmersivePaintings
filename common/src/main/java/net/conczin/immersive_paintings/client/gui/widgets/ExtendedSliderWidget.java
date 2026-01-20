package net.conczin.immersive_paintings.client.gui.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public abstract class ExtendedSliderWidget<T> extends AbstractSliderButton {
    private T oldValue;
    final Consumer<T> onApplyValue;

    protected final T min;
    protected final T max;

    protected final String text;

    public ExtendedSliderWidget(int x, int y, int width, int height, String text, double value, T min, T max, Consumer<T> onApplyValue) {
        super(x, y, width, height, Component.literal(""), value);
        this.min = min;
        this.max = max;
        this.text = text;
        this.onApplyValue = onApplyValue;
    }

    abstract T getValue();

    @Override
    protected void applyValue() {
        T v = getValue();
        if (v != oldValue) {
            oldValue = v;
            onApplyValue.accept(v);
        }
    }
}