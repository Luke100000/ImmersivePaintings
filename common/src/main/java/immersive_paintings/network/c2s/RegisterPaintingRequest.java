package immersive_paintings.network.c2s;

import immersive_paintings.Config;
import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.network.s2c.PaintingListMessage;
import immersive_paintings.network.s2c.RegisterPaintingResponse;
import immersive_paintings.resources.ByteImage;
import immersive_paintings.resources.Painting;
import immersive_paintings.resources.ServerPaintingManager;
import immersive_paintings.util.Utils;
import immersive_paintings.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class RegisterPaintingRequest extends Message {
    private final String name;
    private final NbtCompound painting;

    public RegisterPaintingRequest(String name, Painting painting) {
        this.name = name;
        this.painting = painting.toNbt();
    }

    public RegisterPaintingRequest(PacketByteBuf b) {
        this.name = b.readString();
        this.painting = b.readNbt();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeString(name);
        b.writeNbt(painting);
    }

    @Override
    public void receive(PlayerEntity e) {
        ByteImage image = UploadPaintingRequest.uploadedImages.get(e.getUuidAsString());

        if (image.getWidth() > Config.getInstance().maxUserImageWidth || image.getHeight() > Config.getInstance().maxUserImageHeight) {
            error("too_large", e, null);
            return;
        }

        long count = ServerPaintingManager.get().getCustomServerPaintings().values().stream().filter(p -> p.author.equals(e.getGameProfile().getName())).count();
        if (count > Config.getInstance().maxUserImages) {
            error("limit_reached", e, null);
            return;
        }

        String id = Utils.escapeString(e.getGameProfile().getName()) + "/" + Utils.escapeString(name);
        Identifier identifier = Main.locate(id);

        NbtCompound nbt = this.painting;

        nbt.putString("author", e.getGameProfile().getName());
        nbt.putString("name", name);

        Painting painting = Painting.fromNbt(nbt);

        painting.texture.image = image;

        ServerPaintingManager.registerPainting(
                identifier,
                painting
        );

        //update clients
        for (ServerPlayerEntity player : Objects.requireNonNull(e.getServer()).getPlayerManager().getPlayerList()) {
            NetworkHandler.sendToPlayer(new PaintingListMessage(identifier, painting), player);
        }

        error("", e, identifier);
    }

    private void error(String error, PlayerEntity e, Identifier i) {
        NetworkHandler.sendToPlayer(new RegisterPaintingResponse(error, i), (ServerPlayerEntity)e);
    }
}
