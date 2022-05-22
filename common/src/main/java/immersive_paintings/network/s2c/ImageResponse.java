package immersive_paintings.network.s2c;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.resources.ClientPaintingManager;
import immersive_paintings.resources.Paintings;
import immersive_paintings.util.ImageManipulations;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ImageResponse implements Message {
    private final String identifier;
    private final int[] data;

    public ImageResponse(Identifier identifier, int[] data) {
        this.identifier = identifier.toString();
        this.data = data;
    }

    @Override
    public void receive(PlayerEntity e) {
        Identifier i = new Identifier(this.identifier);
        if (ClientPaintingManager.getPaintings().containsKey(i)) {
            Paintings.PaintingData data = ClientPaintingManager.getPaintings().get(i);
            data.image = ImageManipulations.intsToImage(data.getPixelWidth(), data.getPixelHeight(), this.data);
            data.textureIdentifier = MinecraftClient.getInstance().getTextureManager()
                    .registerDynamicTexture("immersive_painting/" + this.identifier.replace(":", "_"), new NativeImageBackedTexture(data.image));
            data.image.upload(0, 0, 0, false);
        }
    }
}
