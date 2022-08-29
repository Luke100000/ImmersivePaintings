package immersive_paintings.resources;

import immersive_paintings.Config;
import immersive_paintings.Main;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class Painting {
    public int width;
    public int height;
    public final int resolution;

    public final String name;
    public final String author;
    public final boolean datapack;

    public final Texture texture;
    public final Texture half;
    public final Texture quarter;
    public final Texture eighth;
    public final Texture thumbnail;

    public final static Painting DEFAULT = new Painting(new ByteImage(16, 16), 16);

    public Painting(@Nullable ByteImage image, int width, int height, int resolution) {
        this(image, width, height, resolution, "", "", false, UUID.randomUUID().toString());
    }

    public Painting(@Nullable ByteImage image, int width, int height, int resolution, String name, String author, boolean datapack, String hash) {
        this.texture = new Texture(image, hash, Type.FULL);

        int res = Math.max(width, height) * resolution;

        Type halfType = res / 2 < Config.getInstance().lodResolutionMinimum ? Type.FULL : Type.HALF;
        this.half = new Texture(null, hash + "_half", halfType);

        Type quarterType = res / 4 < Config.getInstance().lodResolutionMinimum ? halfType : Type.QUARTER;
        this.quarter = new Texture(null, hash + "_quarter", quarterType);

        Type eighthType = res / 8 < Config.getInstance().lodResolutionMinimum ? quarterType : Type.EIGHTH;
        this.eighth = new Texture(null, hash + "_eighth", eighthType);

        Type thumbnailType = res < Config.getInstance().thumbnailSize ? Type.FULL : Type.THUMBNAIL;
        this.thumbnail = new Texture(null, hash + "_thumbnail", thumbnailType);

        this.width = width;
        this.height = height;
        this.resolution = resolution;
        this.name = name;
        this.author = author;
        this.datapack = datapack;
    }

    public Painting(ByteImage image, int resolution) {
        this(image, image.getWidth() / resolution, image.getHeight() / resolution, resolution, "", "", false, UUID.randomUUID().toString());
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("width", width);
        nbt.putInt("height", height);
        nbt.putInt("resolution", resolution);
        nbt.putString("name", name);
        nbt.putString("author", author);
        nbt.putBoolean("datapack", datapack);
        nbt.putString("hash", texture.hash);
        return nbt;
    }

    public static Painting fromNbt(NbtCompound nbt) {
        int width = nbt.getInt("width");
        int height = nbt.getInt("height");
        int resolution = nbt.getInt("resolution");
        String name = nbt.getString("name");
        String author = nbt.getString("author");
        boolean datapack = nbt.getBoolean("datapack");
        String hash = nbt.getString("hash");
        return new Painting(null, width, height, resolution, name, author, datapack, hash);
    }

    public Texture getTexture(Type type) {
        return switch (type) {
            case FULL -> texture;
            case HALF -> half;
            case QUARTER -> quarter;
            case EIGHTH -> eighth;
            case THUMBNAIL -> thumbnail;
        };
    }

    public enum Type {
        FULL,
        HALF,
        QUARTER,
        EIGHTH,
        THUMBNAIL
    }

    public static class Texture {
        public ByteImage image;
        public boolean requested = false;
        public Identifier textureIdentifier = Main.locate("textures/block/frame/canvas.png");
        public Resource resource;
        public final String hash;
        public final Type link;

        public Texture(ByteImage image, String hash, Type link) {
            this.image = image;
            this.hash = hash;
            this.link = link;
        }
    }
}
