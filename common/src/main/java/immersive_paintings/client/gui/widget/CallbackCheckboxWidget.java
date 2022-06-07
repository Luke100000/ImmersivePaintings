package immersive_paintings.client.gui.widget;

import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class CallbackCheckboxWidget extends CheckboxWidget {
    private final Consumer<Boolean> onChecked;

    public CallbackCheckboxWidget(int x, int y, int width, int height, Text message, boolean checked, boolean showMessage, Consumer<Boolean> onChecked) {
        super(x, y, width, height, message, checked, showMessage);

        this.onChecked = onChecked;
    }

    @Override
    public void onPress() {
        super.onPress();

        onChecked.accept(isChecked());
    }
}
