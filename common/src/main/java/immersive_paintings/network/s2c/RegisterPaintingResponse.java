package immersive_paintings.network.s2c;

import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import net.minecraft.entity.player.PlayerEntity;

public class RegisterPaintingResponse implements Message {
    public final String error;

    public RegisterPaintingResponse(String error) {
        this.error = error;
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handleRegisterPaintingResponse(this);
    }
}
