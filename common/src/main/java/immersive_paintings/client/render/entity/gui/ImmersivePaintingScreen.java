package immersive_paintings.client.render.entity.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class ImmersivePaintingScreen extends Screen {
    final UUID uuid;

    public ImmersivePaintingScreen(UUID uuid) {
        super(new LiteralText("Painting"));
        this.uuid = uuid;
    }
}
