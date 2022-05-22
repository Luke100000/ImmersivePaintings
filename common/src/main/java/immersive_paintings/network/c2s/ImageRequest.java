package immersive_paintings.network.c2s;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.s2c.ImageResponse;
import immersive_paintings.resources.ServerPaintingManager;
import immersive_paintings.util.ImageManipulations;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ImageRequest implements Message {
    private final String identifier;

    public ImageRequest(Identifier identifier) {
        this.identifier = identifier.toString();
    }

    @Override
    public void receive(PlayerEntity e) {
        Identifier i = new Identifier(identifier);
        NativeImage image = ServerPaintingManager.getImage(i);

        if (image != null) {
            int[] is = ImageManipulations.imageToInts(image);
            NetworkHandler.sendToPlayer(new ImageResponse(i, is), (ServerPlayerEntity)e);
        }
    }
}
