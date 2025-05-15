package net.conczin.immersive_paintings.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlowingText {
    public static List<Component> wrap(Component text, int maxWidth) {
        return Minecraft.getInstance().font.getSplitter().splitLines(text, maxWidth, Style.EMPTY).stream().map(line -> {
            MutableComponent compiled = Component.literal("");
            line.visit((s, t) -> {
                compiled.append(Component.literal(t).setStyle(s));
                return Optional.empty();
            }, text.getStyle());
            return compiled;
        }).collect(Collectors.toList());
    }

    public static Component consolidate(List<Component> textList) {
        if(textList == null)
            return null;

        Component base = Component.empty();
        MutableComponent lastTextNode = base.copy();

        if(textList.isEmpty())
            return base;

        for(int i = 0; i < textList.size() - 1; i++) {
            Component text = textList.get(i);
            lastTextNode = lastTextNode.append(text).append("\n");
        }

        Component finalElement = textList.getLast();
        return lastTextNode.append(finalElement);
    }
}
