package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.io.Serial;

public class OpenGuiRequest implements Message {
    @Serial
    private static final long serialVersionUID = -2371116419166251497L;

    public final Type gui;

    public final int villager;

    public OpenGuiRequest(OpenGuiRequest.Type gui, Entity villager) {
        this(gui, villager.getId());
    }

    public OpenGuiRequest(OpenGuiRequest.Type gui, int villager) {
        this.gui = gui;
        this.villager = villager;
    }

    public OpenGuiRequest(OpenGuiRequest.Type gui) {
        this(gui, 0);
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handleOpenGuiRequest(this);
    }

    public enum Type {
        EDITOR,
    }
}
