package immersive_paintings.network.s2c;

import immersive_paintings.Config;
import immersive_paintings.Main;
import immersive_paintings.cobalt.network.Message;
import immersive_paintings.resources.Painting;
import immersive_paintings.resources.ServerPaintingManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PaintingListMessage extends Message {
    private final Map<String, NbtCompound> paintings = new HashMap<>();
    private final boolean clear;
    private final boolean showOtherPlayersPaintings;

    public PaintingListMessage() {
        //datapack paintings
        for (Map.Entry<Identifier, Painting> entry : ServerPaintingManager.getDatapackPaintings().entrySet()) {
            this.paintings.put(entry.getKey().toString(), entry.getValue().toNbt());
        }

        //custom paintings
        for (Map.Entry<Identifier, Painting> entry : ServerPaintingManager.get().getCustomServerPaintings().entrySet()) {
            this.paintings.put(entry.getKey().toString(), entry.getValue().toNbt());
        }

        showOtherPlayersPaintings = Config.getInstance().showOtherPlayersPaintings;
        clear = true;
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeInt(paintings.size());
        for (Map.Entry<String, NbtCompound> entry : paintings.entrySet()) {
            b.writeString(entry.getKey());
            b.writeNbt(entry.getValue());
        }

        b.writeBoolean(clear);
        b.writeBoolean(showOtherPlayersPaintings);
    }

    public PaintingListMessage(PacketByteBuf b) {
        int size = b.readInt();
        for (int i = 0; i < size; i++) {
            String key = b.readString();
            NbtCompound value = b.readNbt();
            paintings.put(key, value);
        }

        clear = b.readBoolean();
        showOtherPlayersPaintings = b.readBoolean();
    }

    public PaintingListMessage(Identifier identifier, Painting painting) {
        this.paintings.put(identifier.toString(), painting == null ? null : painting.toNbt());
        showOtherPlayersPaintings = Config.getInstance().showOtherPlayersPaintings;
        clear = false;
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handlePaintingListResponse(this);
    }

    public Map<Identifier, Painting> getPaintings() {
        Map<Identifier, Painting> paintings = new HashMap<>();
        for (Map.Entry<String, NbtCompound> entry : this.paintings.entrySet()) {
            Identifier identifier = new Identifier(entry.getKey());
            if (entry.getValue() == null) {
                paintings.put(identifier, null);
            } else {
                paintings.put(identifier, Painting.fromNbt(entry.getValue()));
            }
        }
        return paintings;
    }

    public boolean shouldClear() {
        return clear;
    }

    public boolean shouldShowOtherPlayersPaintings() {
        return showOtherPlayersPaintings;
    }
}
