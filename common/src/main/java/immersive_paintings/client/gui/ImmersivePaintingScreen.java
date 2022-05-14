package immersive_paintings.client.gui;

import immersive_paintings.client.gui.widget.PaintingWidget;
import immersive_paintings.resources.Paintings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.List;
import java.util.UUID;

public class ImmersivePaintingScreen extends Screen {
    final UUID uuid;
    private List<Paintings.PaintingData> filteredPaintings;

    public ImmersivePaintingScreen(UUID uuid) {
        super(new LiteralText("Painting"));
        this.uuid = uuid;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        // paintings
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
                addDrawableChild(new PaintingWidget((int)(width / 2 + (x - 3.5) * 52) - 24, height / 2 - 60 + y * 52, 48, 48, sender -> {
                }));
            }
        }

        // page
        addDrawableChild(new ButtonWidget(width / 2 - 65 - 100, height / 2 - 90, 30, 20, new LiteralText("<<"), sender -> {
        }));
        addDrawableChild(new ButtonWidget(width / 2 - 65 - 70, height / 2 - 90, 70, 20, new LiteralText("page 1 / 2"), sender -> {
        }));
        addDrawableChild(new ButtonWidget(width / 2 - 65, height / 2 - 90, 30, 20, new LiteralText(">>"), sender -> {
        }));

        //search
        addDrawableChild(new ButtonWidget(width / 2 + 50 - 35, height / 2 - 90, 130, 20, new LiteralText("searchbar"), sender -> {
        }));

        // filters
        addDrawableChild(new ButtonWidget(width / 2 - 50 - 150, height / 2 - 90 - 22, 100, 20, new LiteralText("Yours"), sender -> {
        }));
        addDrawableChild(new ButtonWidget(width / 2 - 50 - 50, height / 2 - 90 - 22, 100, 20, new LiteralText("Datapacks"), sender -> {
        }));
        addDrawableChild(new ButtonWidget(width / 2 - 50 + 50, height / 2 - 90 - 22, 100, 20, new LiteralText("Players"), sender -> {
        }));
        addDrawableChild(new ButtonWidget(width / 2 - 50 + 150, height / 2 - 90 - 22, 100, 20, new LiteralText("New Painting"), sender -> {
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
    }
}
