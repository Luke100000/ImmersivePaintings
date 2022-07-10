package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import net.minecraft.entity.player.PlayerEntity;

public class OpenGuiRequest implements Message {
    private static final long serialVersionUID = -2371116419166251497L;

    public final Type gui;

    public final int entity;

    public OpenGuiRequest(OpenGuiRequest.Type gui, int entity) {
        this.gui = gui;
        this.entity = entity;
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handleOpenGuiRequest(this);
    }

    public enum Type {
        EDITOR,
    }
}
