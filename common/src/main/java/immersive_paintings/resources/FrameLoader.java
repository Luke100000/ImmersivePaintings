package immersive_paintings.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import immersive_paintings.Main;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;

public class FrameLoader extends JsonDataLoader {
    public static final Identifier ID = Main.locate("frames");

    public static final Map<Identifier, Frame> frames = new HashMap<>();

    public FrameLoader() {
        super(new Gson(), ID.getPath());
    }

    private static final String DEFAULT_FRAME = Main.locate("frame/simple").toString();
    private static final String DEFAULT_MATERIAL = Main.locate("frame/simple/oak").toString();

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        frames.clear();
        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            try {
                JsonObject object = entry.getValue().getAsJsonObject();

                Frame frame = new Frame(
                        new Identifier(JsonHelper.getString(object, "frame", DEFAULT_FRAME)),
                        JsonHelper.getBoolean(object, "diagonals", false),
                        new Identifier(JsonHelper.getString(object, "material", DEFAULT_MATERIAL)));

                frames.put(entry.getKey(), frame);
            } catch (Exception e) {
                Main.LOGGER.error(e);
            }
        }
    }
}
