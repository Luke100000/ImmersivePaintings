package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class OpenGuiRequest extends Message {
    public final Type gui;

    public final int entity;

    public OpenGuiRequest(OpenGuiRequest.Type gui, int entity) {
        this.gui = gui;
        this.entity = entity;
    }

    public OpenGuiRequest(PacketByteBuf b) {
        this.gui = b.readEnumConstant(OpenGuiRequest.Type.class);
        this.entity = b.readInt();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeEnumConstant(gui);
        b.writeInt(entity);
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handleOpenGuiRequest(this);
    }

    public enum Type {
        EDITOR,
    }
}
