package immersive_paintings.client.gui;

import immersive_paintings.client.gui.widget.PaintingWidget;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.c2s.PaintingModifyRequest;
import immersive_paintings.resources.PaintingManager;
import immersive_paintings.resources.Paintings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ImmersivePaintingScreen extends Screen {
    final int entityId;
    private final List<Identifier> filteredPaintings = new ArrayList<>();

    int page;
    private ButtonWidget pageWidget;
    private TextFieldWidget textFieldWidget;

    public ImmersivePaintingScreen(int entityId) {
        super(new LiteralText("Painting"));
        this.entityId = entityId;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        search("");
    }

    private void rebuild() {
        clearChildren();

        // paintings
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
                int i = y * 8 + x + page * 24;
                if (i < filteredPaintings.size()) {
                    Identifier identifier = filteredPaintings.get(i);
                    Paintings.PaintingData painting = PaintingManager.getPainting(identifier);
                    addDrawableChild(new PaintingWidget(painting, (int)(width / 2 + (x - 3.5) * 52) - 24, height / 2 - 60 + y * 52, 48, 48,
                            sender -> NetworkHandler.sendToServer(new PaintingModifyRequest(entityId, identifier)),
                            (ButtonWidget b, MatrixStack matrices, int mx, int my) -> renderTooltip(matrices, List.of(
                                    new LiteralText(identifier.getPath()),
                                    new LiteralText("author").formatted(Formatting.ITALIC),
                                    new LiteralText(painting.width + "x" + painting.height + " at " + painting.resolution + "px").formatted(Formatting.ITALIC)
                            ), mx, my)));
                } else {
                    break;
                }
            }
        }

        // page
        addDrawableChild(new ButtonWidget(width / 2 - 65 - 100, height / 2 - 90, 30, 20, new LiteralText("<<"), sender -> setPage(page - 1)));
        pageWidget = addDrawableChild(new ButtonWidget(width / 2 - 65 - 70, height / 2 - 90, 70, 20, new LiteralText(""), sender -> {
        }));
        addDrawableChild(new ButtonWidget(width / 2 - 65, height / 2 - 90, 30, 20, new LiteralText(">>"), sender -> setPage(page + 1)));

        //search
        TextFieldWidget oldTextFieldWidget = textFieldWidget;
        textFieldWidget = addDrawableChild(new TextFieldWidget(this.textRenderer, width / 2 + 50 - 35, height / 2 - 90, 130, 20,
                new LiteralText("search")));
        textFieldWidget.setMaxLength(64);
        textFieldWidget.setChangedListener(this::search);
        textFieldWidget.changeFocus(oldTextFieldWidget != null && oldTextFieldWidget.isFocused());

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

    private void search(String s) {
        filteredPaintings.clear();
        filteredPaintings.addAll(PaintingManager.getClientPaintings().keySet().stream().filter(k -> k.toString().contains(s)).toList());
        setPage(page);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void setPage(int p) {
        page = Math.min(getMaxPages() - 1, Math.max(0, p));
        rebuild();
        pageWidget.setMessage(new LiteralText((page + 1) + " / " + getMaxPages()));
    }

    private int getMaxPages() {
        return (int)Math.ceil(filteredPaintings.size() / 24.0);
    }
}
