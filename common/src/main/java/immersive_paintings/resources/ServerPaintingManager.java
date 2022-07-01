package immersive_paintings.resources;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ServerPaintingManager {
    public static MinecraftServer server;
    private static Map<Identifier, Painting> datapackPaintings = new HashMap<>();

    public static CustomServerPaintings get() {
        return server.getOverworld().getPersistentStateManager()
                .getOrCreate(CustomServerPaintings::fromNbt, CustomServerPaintings::new, "immersive_paintings");
    }

    public static Map<Identifier, Painting> getDatapackPaintings() {
        return datapackPaintings;
    }

    public static void setDatapackPaintings(Map<Identifier, Painting> datapackPaintings) {
        ServerPaintingManager.datapackPaintings = datapackPaintings;
    }

    public static Path getPaintingPath(Identifier identifier) {
        return Path.of("immersive_paintings", identifier.toString().replace(":", "_") + ".png");
    }

    public static void registerPainting(Identifier identifier, Painting painting) {
        try {
            if (painting.image != null) {
                Path path = getPaintingPath(identifier);
                //noinspection ResultOfMethodCallIgnored
                new File(path.getParent().toString()).mkdirs();
                painting.image.writeTo(path);
            }

            get().getCustomServerPaintings().put(identifier, painting);
            get().setDirty(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deregisterPainting(Identifier identifier) {
        get().getCustomServerPaintings().remove(identifier);
        //todo delete data
        get().setDirty(true);
    }

    public static Painting getPainting(Identifier i) {
        if (datapackPaintings.containsKey(i)) {
            return datapackPaintings.get(i);
        } else {return get().customServerPaintings.getOrDefault(i, null);}
    }

    public static NativeImage getImage(Identifier i) {
        Painting painting = getPainting(i);

        if (painting.image == null) {
            try {
                if (painting.resource != null) {
                    return painting.image = NativeImage.read(painting.resource.getInputStream());
                } else if (get().customServerPaintings.containsKey(i)) {
                    FileInputStream stream = new FileInputStream(getPaintingPath(i).toString());
                    painting.image = NativeImage.read(stream);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return painting.image;
    }

    public static class CustomServerPaintings extends PersistentState {
        final Map<Identifier, Painting> customServerPaintings = new HashMap<>();

        public static CustomServerPaintings fromNbt(NbtCompound nbt) {
            CustomServerPaintings c = new CustomServerPaintings();
            for (String key : nbt.getKeys()) {
                c.customServerPaintings.put(new Identifier(key), Painting.fromNbt(nbt.getCompound(key)));
            }
            return c;
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            NbtCompound c = new NbtCompound();
            for (Map.Entry<Identifier, Painting> entry : customServerPaintings.entrySet()) {
                c.put(entry.getKey().toString(), entry.getValue().toNbt());
            }
            return c;
        }

        public Map<Identifier, Painting> getCustomServerPaintings() {
            return customServerPaintings;
        }
    }
}
