package immersive_paintings.network.c2s;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.network.LazyNetworkManager;
import immersive_paintings.network.s2c.ImageResponse;
import immersive_paintings.resources.ByteImage;
import immersive_paintings.resources.Painting;
import immersive_paintings.resources.ServerPaintingManager;
import immersive_paintings.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ImageRequest extends Message {
    private final String identifier;
    private final Painting.Type type;

    public ImageRequest(Identifier identifier, Painting.Type type) {
        this.identifier = identifier.toString();
        this.type = type;
    }

    public ImageRequest(PacketByteBuf b) {
        this.identifier = b.readString();
        this.type = b.readEnumConstant(Painting.Type.class);
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeString(identifier);
        b.writeEnumConstant(type);
    }

    @Override
    public void receive(PlayerEntity e) {
        Identifier identifier = new Identifier(this.identifier);
        ByteImage image = ServerPaintingManager.getImage(identifier, type);
        if (image != null) {
            Utils.processByteArrayInChunks(image.getBytes(), (ints, split, splits) -> LazyNetworkManager.sendToClient(new ImageResponse(identifier, type, image.getWidth(), image.getHeight(), ints, split, splits), (ServerPlayerEntity)e));
        }
    }
}
