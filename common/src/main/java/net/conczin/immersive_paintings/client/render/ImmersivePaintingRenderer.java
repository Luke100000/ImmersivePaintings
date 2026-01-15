package net.conczin.immersive_paintings.client.render;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.client.render.state.ImmersivePaintingRenderState;
import net.conczin.immersive_paintings.config.ClientConfig;
import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.conczin.immersive_paintings.ClientPaintingManager;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.registration.Configs;
import net.conczin.immersive_paintings.resources.ObjectLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import owens.oobjloader.Face;
import owens.oobjloader.FaceVertex;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

public class ImmersivePaintingRenderer<T extends ImmersivePaintingEntity> extends EntityRenderer<T, ImmersivePaintingRenderState> {
    public ImmersivePaintingRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ImmersivePaintingRenderState createRenderState() {
        return new ImmersivePaintingRenderState();
    }

    @Override
    public void extractRenderState(T entity, ImmersivePaintingRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.light = LevelRenderer.getLightColor(entity.level(), entity.blockPosition());

        state.xRot = entity.getXRot(partialTick);
        state.yRot = entity.getYRot(partialTick);
        //state.rotation = entity.getRotation();
        state.isGlowing = entity.isGlowing();
        state.isGraffiti = entity.isGraffiti();
        state.widthPixels = entity.getPaintingWidth() * 16;
        state.heightPixels = entity.getPaintingHeight() * 16;

        state.frame = entity.getFrame();
        state.material = entity.getMaterial();

        Minecraft client = Minecraft.getInstance();
        ClientConfig config = Configs.CLIENT;

        double distance = (client.player == null ? 0 : client.player.distanceTo(entity));
        double blocksVisible = Math.tan(client.options.fov().get() / 180.0 * Math.PI / 2.0) * 2.0 * distance;

        Optional<Painting> painting = ClientPaintingManager.getPainting(entity.getMotive());
        if (painting.isEmpty()) {
            state.texture = ClientPaintingManager.getImageIdentifier(Painting.DEFAULT_IDENTIFIER, Painting.Size.FULL);
            return;
        }

        int resolution = painting.get().resolution();
        double pixelDensity = blocksVisible * resolution / client.getWindow().getHeight();

        Painting.Size size = pixelDensity > config.thumbResolutionThreshold ? Painting.Size.THUMBNAIL
                : pixelDensity > config.eighthResolutionThreshold ? Painting.Size.EIGHTH
                : pixelDensity > config.quarterResolutionThreshold ? Painting.Size.QUARTER
                : pixelDensity > config.halfResolutionThreshold ? Painting.Size.HALF
                : Painting.Size.FULL;

        state.texture = ClientPaintingManager.getImageIdentifier(entity.getMotive(), size);
    }

    // TODO
    @Override
    protected int getBlockLightLevel(T entity, BlockPos pos) {
        int parentLight = super.getBlockLightLevel(entity, pos);
        return entity.isGlowing() ? Math.max(5, parentLight) : parentLight;
    }

    @Override
    public void render(ImmersivePaintingRenderState state, PoseStack poseStack, MultiBufferSource buffer, int light) {
        super.render(state, poseStack, buffer, light);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));
        poseStack.scale(0.0625f, 0.0625f, 0.0625f);
        renderPainting(poseStack, buffer, state);
        poseStack.popPose();
    }

    protected int getLight(int light, boolean glowing) {
        if (!glowing)
            return light;

        return LightTexture.pack((int)(LightTexture.block(light) * 0.25 + 15 * 0.75), LightTexture.sky(light));
    }

    protected int getFrameLight(int light, boolean glowing) {
        if (!glowing)
            return light;

        return LightTexture.pack((int)(LightTexture.block(light) * 0.875 + 2), LightTexture.sky(light));
    }

    private void renderPainting(PoseStack poses, MultiBufferSource buffer, ImmersivePaintingRenderState state) {
        PoseStack.Pose pose = poses.last();
        VertexConsumer vertexConsumer;

        boolean hasFrame = !state.frame.equals(Main.NONE_LOCATION);

        //canvas
        vertexConsumer = buffer.getBuffer(state.isGraffiti ? RenderType.entityTranslucent(state.texture) : RenderType.entitySolid(state.texture));
        renderFaces(state.isGraffiti ? "objects/graffiti.obj" : "objects/canvas.obj", pose, vertexConsumer, getLight(state.light, state.isGlowing), state.widthPixels, state.heightPixels, hasFrame ? 1.0f : 0.0f);

        //frame
        if (hasFrame) {
            vertexConsumer = buffer.getBuffer(RenderType.entityCutout(state.material));
            renderFrame(state.frame, pose, vertexConsumer, getFrameLight(state.light, state.isGlowing), state.widthPixels, state.heightPixels);
        }
    }

    private void renderFaces(String name, PoseStack.Pose pose, VertexConsumer vertexConsumer, int light, float width, float height, float margin) {
        List<Face> faces = ObjectLoader.objects.get(Main.locate(name));
        for (Face face : faces) {
            for (FaceVertex v : face.vertices) {
                vertex(pose,
                        vertexConsumer,
                        v.v.x * (width - margin * 2),
                        v.v.y * (height - margin * 2),
                        v.v.z * 16.0f,
                        v.t.u * (width - margin * 2) / width + margin / width,
                        (1.0f - v.t.v) * (height - margin * 2) / height + margin / height,
                        v.n.x,
                        v.n.y,
                        v.n.z,
                        light);
            }
        }
    }

    private List<Face> getFaces(ResourceLocation frame, String part) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(frame.getNamespace(), frame.getPath() + "/" + part + ".obj");
        if (ObjectLoader.objects.containsKey(id)) {
            return ObjectLoader.objects.get(id);
        }
        return List.of();
    }

    private void renderFrame(ResourceLocation frame, PoseStack.Pose pose, VertexConsumer vertexConsumer, int light, float width, float height) {
        List<Face> faces = getFaces(frame, "bottom");
        for (int x = 0; x < width / 16; x++) {
            float u = width == 16 ? 0.75f : (x == 0 ? 0.0f : x == width / 16 - 1 ? 0.5f : 0.25f);
            for (Face face : faces) {
                for (FaceVertex v : face.vertices) {
                    vertex(pose, vertexConsumer, v.v.x + x * 16 - (width - 16) / 2, v.v.y - (height - 16) / 2, v.v.z, v.t.u * 0.25f + u, (1.0f - v.t.v), v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
        faces = getFaces(frame, "top");
        for (int x = 0; x < width / 16; x++) {
            float u = width == 16 ? 0.75f : (x == 0 ? 0.0f : x == width / 16 - 1 ? 0.5f : 0.25f);
            for (Face face : faces) {
                for (FaceVertex v : face.vertices) {
                    vertex(pose, vertexConsumer, v.v.x + x * 16 - (width - 16) / 2, v.v.y + (height - 16) / 2, v.v.z, v.t.u * 0.25f + u, (1.0f - v.t.v), v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
        faces = getFaces(frame, "right");
        for (int y = 0; y < height / 16; y++) {
            float u = 0.25f;
            for (Face face : faces) {
                for (FaceVertex v : face.vertices) {
                    vertex(pose, vertexConsumer, v.v.x + (width - 16) / 2, v.v.y + y * 16 - (height - 16) / 2, v.v.z, v.t.u * 0.25f + u, (1.0f - v.t.v), v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
        faces = getFaces(frame, "left");
        for (int y = 0; y < height / 16; y++) {
            float u = 0.25f;
            for (Face face : faces) {
                for (FaceVertex v : face.vertices) {
                    vertex(pose, vertexConsumer, v.v.x - (width - 16) / 2, v.v.y + y * 16 - (height - 16) / 2, v.v.z, v.t.u * 0.25f + u, (1.0f - v.t.v), v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
    }

    private void vertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ, int light) {
        vertexConsumer.addVertex(pose, x, y, z - 0.5f).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, normalX, normalY, normalZ);
    }
}