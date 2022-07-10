package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class RegisterPaintingResponse implements Message {
    private static final long serialVersionUID = -5560323045809539454L;

    public final String error;
    public final String identifier;

    public RegisterPaintingResponse(String error, Identifier identifier) {
        this.error = error;
        this.identifier = identifier == null ? null : identifier.toString();
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handleRegisterPaintingResponse(this);
    }
}
