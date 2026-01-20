package net.conczin.immersive_paintings;

import java.util.EnumSet;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public record Painting(int version, int width, int height, int resolution, String name, String author, UUID authorUUID, Type type, EnumSet<Flag> flags, String hash) {
    public static final Codec<Painting> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.fieldOf("version").forGetter(Painting::version),
        Codec.INT.fieldOf("width").forGetter(Painting::width),
        Codec.INT.fieldOf("height").forGetter(Painting::height),
        Codec.INT.fieldOf("resolution").forGetter(Painting::resolution),
        Codec.STRING.fieldOf("name").forGetter(Painting::name),
        Codec.STRING.fieldOf("author").forGetter(Painting::author),
        UUIDUtil.CODEC.fieldOf("authorUUID").forGetter(Painting::authorUUID),
        Type.CODEC.fieldOf("type").forGetter(Painting::type),
        Flag.CODEC.fieldOf("flags").forGetter(Painting::flags),
        Codec.STRING.fieldOf("hash").forGetter(Painting::hash)
    ).apply(i, Painting::new));

    public static final StreamCodec<ByteBuf, Painting> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static final ResourceLocation DEFAULT_IDENTIFIER = Main.locate("textures/block/frame/canvas.png");

    public Painting(int width, int height, int resolution, String name, String author, UUID authorUUID, Type type, EnumSet<Flag> flags, String hash) {
        this(1, width, height, resolution, name, author, authorUUID, type, flags, hash);
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

    public boolean is(Type t) {
        return type().equals(t);
    }

    public boolean has(Flag flag) {
        return flags().contains(flag);
    }

    public Painting withHash(String newHash) {
        return new Painting(width(), height(), resolution(), name(), author(), authorUUID(), type(), flags(), newHash);
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
        THUMBNAIL,
        NSFW;
    }

    public enum Flag {
        HIDDEN(1),
        NSFW(2),
        GRAFFITI(4);

        private final long bits;

        Flag(long bits) {
            this.bits = bits;
        }

        public static final PrimitiveCodec<EnumSet<Flag>> CODEC = new PrimitiveCodec<>() {
            @Override
            public <T> DataResult<EnumSet<Flag>> read(final DynamicOps<T> ops, final T input) {
                DataResult<Long> result = ops.getNumberValue(input).map(Number::longValue);
                if (result.isError())
                    return DataResult.error(() -> "No long provided for reading FlagSet");

                long l = result.getOrThrow();
                EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);

                for (Flag f : Flag.values()) {
                    if ((l & f.bits) != 0) {
                        flags.add(f);
                    }
                }

                return DataResult.success(flags);
            }

            @Override
            public <T> T write(final DynamicOps<T> ops, final EnumSet<Flag> value) {
                long l = value.stream().map(f -> f.bits).reduce(0L, (a, b) -> a | b);
                return ops.createLong(l);
            }

            @Override
            public String toString() {
                return "Flags";
            }
        };

        public static final StreamCodec<ByteBuf, EnumSet<Flag>> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
    }
}
