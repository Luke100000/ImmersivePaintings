package net.conczin.immersive_paintings.painting;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.conczin.immersive_paintings.Main;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public record Painting(int width, int height, int resolution, String name, String author, UUID authorUUID, Type type, boolean hidden, boolean nsfw, boolean graffiti, String hash) {
    public static final Codec<Painting> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.fieldOf("width").forGetter(Painting::width),
        Codec.INT.fieldOf("height").forGetter(Painting::height),
        Codec.INT.fieldOf("resolution").forGetter(Painting::resolution),
        Codec.STRING.fieldOf("name").forGetter(Painting::name),
        Codec.STRING.fieldOf("author").forGetter(Painting::author),
        UUIDUtil.CODEC.fieldOf("authorUUID").forGetter(Painting::authorUUID),
        Type.CODEC.fieldOf("type").forGetter(Painting::type),
        Codec.BOOL.fieldOf("hidden").forGetter(Painting::hidden),
        Codec.BOOL.fieldOf("nsfw").forGetter(Painting::nsfw),
        Codec.BOOL.fieldOf("graffiti").forGetter(Painting::graffiti),
        Codec.STRING.fieldOf("hash").forGetter(Painting::hash)
    ).apply(i, Painting::new));

    public static final StreamCodec<ByteBuf, Painting> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static final ResourceLocation DEFAULT_IDENTIFIER = Main.locate("textures/block/frame/canvas.png");

    public static final Painting DEFAULT = new Painting(1, 1, 32, "", "", UUID.randomUUID(), Type.PAINTING, false, false, false,"");

    public boolean isDatapack() {
        return type.equals(Type.DATAPACK);
    }

    public ResourceLocation location() {
        switch (type) {
            case Type.DATAPACK -> {
                return Main.locate("datapack/" + hash);
            }
            case Type.XERCA -> {
                return Main.locate("xerca/" + hash);
            }
        }
        return Main.locate(authorUUID.toString() + "/" + hash);
    }

    public enum Type implements StringRepresentable {
        DATAPACK,
        XERCA,
        PAINTING;

        public static final Codec<Type> CODEC = StringRepresentable.fromValues(Type::values);

        @Override
        public @NotNull String getSerializedName() {
            return this.name();
        }
    }

    public enum Size {
        FULL,
        HALF,
        QUARTER,
        EIGHTH,
        THUMBNAIL,
        NSFW;
    }
}
