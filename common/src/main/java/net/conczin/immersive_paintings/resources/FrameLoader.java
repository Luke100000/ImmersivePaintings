package net.conczin.immersive_paintings.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.conczin.immersive_paintings.Main;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class FrameLoader extends SimpleJsonResourceReloadListener<FrameLoader.Frame> {
    public static final Identifier ID = Main.locate("frames");

    public static final Map<Identifier, Frame> frames = new HashMap<>();

    private static final String DEFAULT_FRAME = Main.locate("frame/simple").toString();
    private static final String DEFAULT_MATERIAL = Main.locate("frame/simple/oak").toString();

    static final Codec<Frame> FRAME_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.optionalFieldOf("frame", Identifier.parse(DEFAULT_FRAME)).forGetter(Frame::frame),
        Codec.BOOL.optionalFieldOf("diagonals", false).forGetter(Frame::diagonals),
        Identifier.CODEC.optionalFieldOf("material", Identifier.parse(DEFAULT_MATERIAL)).forGetter(Frame::material)
    ).apply(instance, Frame::new));

    public FrameLoader() {
        super(FRAME_CODEC, FileToIdConverter.json(ID.getPath()));
    }

    @Override
    protected void apply(Map<Identifier, Frame> prepared, ResourceManager manager, ProfilerFiller profiler) {
        frames.clear();
        frames.putAll(prepared);
    }

    public record Frame(Identifier frame, boolean diagonals, Identifier material) {}
}
