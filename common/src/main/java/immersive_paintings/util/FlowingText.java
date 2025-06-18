package immersive_paintings.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlowingText {
    public static List<Text> wrap(Text text, int maxWidth) {
        return MinecraftClient.getInstance().textRenderer.getTextHandler().wrapLines(text, maxWidth, Style.EMPTY).stream().map(line -> {
            MutableText compiled = Text.literal("");
            line.visit((s, t) -> {
                compiled.append(Text.literal(t).setStyle(s));
                return Optional.empty();
            }, text.getStyle());
            return compiled;
        }).collect(Collectors.toList());
    }

    public static Text consolidate(List<Text> textList) {
        if(textList == null)
            return null;

        Text base = Text.empty();
        MutableText lastTextNode = base.copy();

        if(textList.isEmpty())
            return base;

        for(int i = 0; i < textList.size() - 1; i++) {
            Text text = textList.get(i);
            lastTextNode = lastTextNode.append(text).append("\n");
        }

        Text finalElement = textList.get(textList.size() - 1);
        return lastTextNode.append(finalElement);
    }

}
