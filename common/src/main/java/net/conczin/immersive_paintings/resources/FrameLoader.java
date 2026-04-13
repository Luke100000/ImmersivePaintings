package net.conczin.immersive_paintings.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.conczin.immersive_paintings.ImmersivePaintings;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class FrameLoader extends SimpleJsonResourceReloadListener<JsonElement> {
    public static final Identifier ID = ImmersivePaintings.locate("frame_loader");

    public static final Map<Identifier, Frame> frames = new HashMap<>();

    private static final String DEFAULT_FRAME = ImmersivePaintings.locate("frame/simple").toString();
    private static final String DEFAULT_MATERIAL = ImmersivePaintings.locate("frame/simple/oak").toString();

    public FrameLoader() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("frames"));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
        frames.clear();
        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            try {
                JsonObject object = entry.getValue().getAsJsonObject();

                Frame frame = new Frame(
                    Identifier.parse(GsonHelper.getAsString(object, "frame", DEFAULT_FRAME)),
                    GsonHelper.getAsBoolean(object, "diagonals", false),
                    Identifier.parse(GsonHelper.getAsString(object, "material", DEFAULT_MATERIAL))
                );

                frames.put(entry.getKey(), frame);
            } catch (Exception e) {
                ImmersivePaintings.LOGGER.error(e);
            }
        }
    }

    public record Frame(Identifier frame, boolean diagonals, Identifier material) {}
}
