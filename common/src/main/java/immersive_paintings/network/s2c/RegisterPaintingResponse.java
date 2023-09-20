package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class RegisterPaintingResponse extends Message {
    public final String error;
    public final String identifier;

    public RegisterPaintingResponse(String error, Identifier identifier) {
        this.error = error;
        this.identifier = identifier == null ? "" : identifier.toString();
    }

    public RegisterPaintingResponse(PacketByteBuf b) {
        this.error = b.readString();
        this.identifier = b.readString();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeString(error);
        b.writeString(identifier);
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handleRegisterPaintingResponse(this);
    }
}
