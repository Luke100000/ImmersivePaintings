package immersive_paintings.network.c2s;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.s2c.PaintingResponse;
import immersive_paintings.resources.PaintingManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PaintingRequest implements Message {
    private final String identifier;

    public PaintingRequest(Identifier identifier) {
        this.identifier = identifier.toString();
    }

    @Override
    public void receive(PlayerEntity e) {
        Identifier i = new Identifier(identifier);
        if (PaintingManager.getServerPaintings().containsKey(i)) {
            NativeImage image = PaintingManager.getServerPaintings().get(i).image;

            int[] is = new int[image.getWidth() * image.getHeight()];
            for (int x = 0; x < image.getWidth(); ++x) {
                for (int y = 0; y < image.getHeight(); ++y) {
                    is[x + y * image.getWidth()] = image.getColor(x, y);
                }
            }

            NetworkHandler.sendToPlayer(new PaintingResponse(i, is, image.getWidth(), image.getHeight()), (ServerPlayerEntity)e);
        }
    }
}
