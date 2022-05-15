package immersive_paintings.network.s2c;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.resources.PaintingManager;
import immersive_paintings.resources.Paintings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class PaintingResponse implements Message {
    private final String identifier;
    private final int[] data;
    private final int width;
    private final int height;

    public PaintingResponse(Identifier identifier, int[] data, int width, int height) {
        this.identifier = identifier.toString();
        this.data = data;
        this.width = width;
        this.height = height;
    }

    @Override
    public void receive(PlayerEntity e) {
        Identifier i = new Identifier(this.identifier);
        if (PaintingManager.getClientPaintings().containsKey(i)) {
            NativeImage image = new NativeImage(width, height, false);
            Paintings.PaintingData data = PaintingManager.getClientPaintings().get(i);
            data.image = image;

            data.textureIdentifier = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("immersive_painting/" + this.identifier.replace(":", "/"), new NativeImageBackedTexture(image));

            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    image.setColor(x, y, this.data[x + y * width]);
                }
            }

            data.image.upload(0, 0, 0, false);
        }
    }
}
