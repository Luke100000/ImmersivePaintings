package net.conczin.immersive_paintings.resources;

import net.conczin.immersive_paintings.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
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

public class ObjectLoader extends SimplePreparableReloadListener<Map<ResourceLocation, Resource>> {
    protected static final ResourceLocation ID = Main.locate("objects");

    public final static Map<ResourceLocation, List<Face>> objects = new HashMap<>();

    @Override
    protected Map<ResourceLocation, Resource> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return manager.listResources("objects", n -> n.getPath().endsWith(".obj"));
    }

    @Override
    protected void apply(Map<ResourceLocation, Resource> o, ResourceManager manager, ProfilerFiller profiler) {
        objects.clear();
        o.forEach((id, res) -> {
            try {
                InputStream stream = res.open();
                ArrayList<Face> faces = new Builder(new BufferedReader(new InputStreamReader(stream))).faces;
                ResourceLocation newId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath());
                objects.put(newId, faces);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
