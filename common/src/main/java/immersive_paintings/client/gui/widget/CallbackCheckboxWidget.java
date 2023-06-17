package immersive_paintings.client.gui.widget;

import immersive_paintings.util.FlowingText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class CallbackCheckboxWidget extends CheckboxWidget {
    private final Consumer<Boolean> onChecked;
    private final Text tooltip;

    public CallbackCheckboxWidget(int x, int y, int width, int height, Text message, Text tooltip, boolean checked, boolean showMessage, Consumer<Boolean> onChecked) {
        super(x, y, width, height, message, checked, showMessage);

        this.onChecked = onChecked;
        this.tooltip = tooltip;
    }

    @Override
    public void onPress() {
        super.onPress();

        onChecked.accept(isChecked());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (isHovered() && tooltip != null) {
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, FlowingText.wrap(tooltip, 160), mouseX, mouseY);
        }
    }
}
