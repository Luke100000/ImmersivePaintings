package net.conczin.immersive_paintings.client.render.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

public class ImmersivePaintingRenderState extends EntityRenderState {
    public int light;
    public float xRot;
    public float yRot;

    public boolean isGlowing;
    public boolean isGraffiti;

    public int widthPixels;
    public int heightPixels;

    public Identifier frame;
    public Identifier material;

    public Identifier texture;
}
