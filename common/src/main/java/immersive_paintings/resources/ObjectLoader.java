package immersive_paintings.resources;

import immersive_paintings.Main;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import owens.oobjloader.Builder;
import owens.oobjloader.Face;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectLoader extends SinglePreparationResourceReloader<Map<Identifier, Resource>> {
    protected static final Identifier ID = Main.locate("objects");

    public final static Map<Identifier, List<Face>> objects = new HashMap<>();

    @Override
    protected Map<Identifier, Resource> prepare(ResourceManager manager, Profiler profiler) {
        return manager.findResources("objects", n -> n.getPath().endsWith(".obj"));
    }

    @Override
    protected void apply(Map<Identifier, Resource> o, ResourceManager manager, Profiler profiler) {
        objects.clear();
        o.forEach((id, res) -> {
            try {
                InputStream stream = res.getInputStream();
                ArrayList<Face> faces = new Builder(new BufferedReader(new InputStreamReader(stream))).faces;
                Identifier newId = new Identifier(id.getNamespace(), id.getPath());
                objects.put(newId, faces);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
