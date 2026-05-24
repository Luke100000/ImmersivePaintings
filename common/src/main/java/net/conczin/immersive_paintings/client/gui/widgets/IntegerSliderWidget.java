package net.conczin.immersive_paintings.client.gui.widgets;

import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class IntegerSliderWidget extends ExtendedSliderWidget<Integer> {
    public IntegerSliderWidget(int x, int y, int width, int height, String text, double value, int min, int max, Consumer<Integer> onApplyValue) {
        super(x, y, width, height, text, (value - min) / (max - min), min, max, onApplyValue);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.translatable(text, getValue()));
    }

    @Override
    Integer getValue() {
        return (int)(value * (max - min) + min);
    }
}
