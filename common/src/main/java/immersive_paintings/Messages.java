package immersive_paintings;


import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.s2c.ImmersivePaintingSpawnMessage;

public class Messages {
    public static void bootstrap() {
        NetworkHandler.registerMessage(ImmersivePaintingSpawnMessage.class);
    }
}
