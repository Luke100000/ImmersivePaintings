package immersive_paintings.resources;

import immersive_paintings.Main;
import immersive_paintings.util.ImageManipulations;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class Painting {
    public final int width;
    public final int height;
    public final int resolution;

    public final String name;
    public final String author;
    public final String hash;
    public boolean datapack;

    @Nullable
    public NativeImage image;

    public boolean requested = false;
    public Identifier textureIdentifier = Main.locate("textures/block/frame/canvas.png");

    public Painting(@Nullable NativeImage image, int width, int height, int resolution) {
        this(image, width, height, resolution, "", "", false, UUID.randomUUID().toString());
    }

    public Painting(@Nullable NativeImage image, int width, int height, int resolution, String name, String author, boolean datapack, String hash) {
        this.image = image;
        this.width = width;
        this.height = height;
        this.resolution = resolution;
        this.name = name;
        this.author = author;
        this.datapack = datapack;
        this.hash = hash;
    }

    public Painting(NativeImage image, int resolution) {
        this(image, resolution, "", "", false, UUID.randomUUID().toString());
    }

    public Painting(NativeImage image, int resolution, String name, String author, boolean datapack, String hash) {
        this.image = image;
        this.width = image.getWidth() / resolution;
        this.height = image.getHeight() / resolution;
        this.resolution = resolution;
        this.name = name;
        this.author = author;
        this.datapack = datapack;
        this.hash = hash;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("width", width);
        nbt.putInt("height", height);
        nbt.putInt("resolution", resolution);
        nbt.putString("name", name);
        nbt.putString("author", author);
        nbt.putBoolean("datapack", datapack);
        nbt.putString("hash", hash);
        return nbt;
    }

    public NbtCompound toFullNbt() {
        assert image != null;
        NbtCompound nbt = toNbt();
        nbt.putIntArray("image", ImageManipulations.imageToInts(image));
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

        NativeImage image = null;
        if (nbt.contains("image")) {
            image = ImageManipulations.intsToImage(width * resolution, height * resolution, nbt.getIntArray("image"));
        }

        return new Painting(image, width, height, resolution, name, author, datapack, hash);
    }

    public int getPixelWidth() {
        return width * resolution;
    }

    public int getPixelHeight() {
        return height * resolution;
    }
}
