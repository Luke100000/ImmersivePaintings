package immersive_paintings.client.gui.widget;

import immersive_paintings.util.FlowingText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class TooltipButtonWidget extends ButtonWidget {
    public TooltipButtonWidget(int x, int y, int width, int height, Text message, Text tooltip, PressAction onPress) {
        super(x, y, width, height, message, onPress, (ButtonWidget buttonWidget, MatrixStack matrixStack, int mx, int my) ->
        {
            assert MinecraftClient.getInstance().currentScreen != null;
            MinecraftClient.getInstance().currentScreen.renderTooltip(matrixStack, FlowingText.wrap(tooltip, 160), mx, my);
        });
    }
}
