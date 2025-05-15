package net.conczin.immersive_paintings.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.conczin.immersive_paintings.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class FrameLoader extends SimpleJsonResourceReloadListener {
    public static final ResourceLocation ID = Main.locate("frames");

    public static final Map<ResourceLocation, Frame> frames = new HashMap<>();

    public FrameLoader() {
        super(new Gson(), ID.getPath());
    }

    private static final String DEFAULT_FRAME = Main.locate("frame/simple").toString();
    private static final String DEFAULT_MATERIAL = Main.locate("frame/simple/oak").toString();

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
        frames.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : prepared.entrySet()) {
            try {
                JsonObject object = entry.getValue().getAsJsonObject();

                Frame frame = new Frame(
                    ResourceLocation.parse(GsonHelper.getAsString(object, "frame", DEFAULT_FRAME)),
                    GsonHelper.getAsBoolean(object, "diagonals", false),
                    ResourceLocation.parse(GsonHelper.getAsString(object, "material", DEFAULT_MATERIAL)));

                frames.put(entry.getKey(), frame);
            } catch (Exception e) {
                Main.LOGGER.error(e);
            }
        }
    }

    public record Frame(ResourceLocation frame, boolean diagonals, ResourceLocation material) {}
}
