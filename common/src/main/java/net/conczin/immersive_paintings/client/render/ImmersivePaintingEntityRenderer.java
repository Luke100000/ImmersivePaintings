package net.conczin.immersive_paintings.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.conczin.immersive_paintings.ClientPaintingManager;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.config.ClientConfig;
import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.conczin.immersive_paintings.registration.Configs;
import net.conczin.immersive_paintings.resources.ObjectLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import owens.oobjloader.Face;
import owens.oobjloader.FaceVertex;

import java.util.List;
import java.util.Optional;

public class ImmersivePaintingEntityRenderer
        extends EntityRenderer<ImmersivePaintingEntity, ImmersivePaintingEntityRenderer.RenderState> {

    public static class RenderState extends EntityRenderState {
        public Identifier textureId = Painting.DEFAULT_IDENTIFIER;
        public Identifier materialId = Main.locate("none");
        public Identifier frameId = Main.locate("none");
        public int paintingWidth = 1;
        public int paintingHeight = 1;
        public boolean glowing;
        public boolean graffiti;
        public float yaw;
        public float xRot;
    }

    public ImmersivePaintingEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(ImmersivePaintingEntity entity, RenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.paintingWidth = entity.getPaintingWidth();
        state.paintingHeight = entity.getPaintingHeight();
        state.glowing = entity.isGlowing();
        state.graffiti = entity.isGraffiti();
        state.frameId = entity.getFrame();
        state.materialId = entity.getMaterial();
        state.textureId = computeTextureId(entity);
        state.yaw = entity.getYRot();
        state.xRot = entity.getXRot();
    }

    private Identifier computeTextureId(ImmersivePaintingEntity entity) {
        Minecraft client = Minecraft.getInstance();
        ClientConfig config = Configs.CLIENT;

        double distance = (client.player == null ? 0 : client.player.distanceTo(entity));
        double blocksVisible = Math.tan(client.options.fov().get() / 180.0 * Math.PI / 2.0) * 2.0 * distance;

        Optional<Painting> painting = ClientPaintingManager.getPainting(entity.getMotive());
        if (painting.isEmpty())
            return ClientPaintingManager.getImageIdentifier(Painting.DEFAULT_IDENTIFIER, Painting.Size.FULL);

        int resolution = painting.get().resolution();
        double pixelDensity = blocksVisible * resolution / client.getWindow().getHeight();

        Painting.Size size = pixelDensity > config.thumbResolutionThreshold ? Painting.Size.THUMBNAIL
                : pixelDensity > config.eighthResolutionThreshold ? Painting.Size.EIGHTH
                : pixelDensity > config.quarterResolutionThreshold ? Painting.Size.QUARTER
                : pixelDensity > config.halfResolutionThreshold ? Painting.Size.HALF
                : Painting.Size.FULL;

        return ClientPaintingManager.getImageIdentifier(entity.getMotive(), size);
    }

    @Override
    public void submit(RenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        poses.mulPose(Axis.XP.rotationDegrees(-state.xRot));
        poses.scale(0.0625f, 0.0625f, 0.0625f);

        int light = state.lightCoords;
        boolean hasFrame = !state.frameId.getPath().equals("none");
        int widthPixels = state.paintingWidth * 16;
        int heightPixels = state.paintingHeight * 16;
        boolean glowing = state.glowing;
        boolean graffiti = state.graffiti;

        // Canvas
        RenderType canvasType = graffiti
                ? RenderTypes.entityTranslucent(state.textureId)
                : RenderTypes.entitySolid(state.textureId);
        String canvasObj = graffiti ? "objects/graffiti.obj" : "objects/canvas.obj";
        float margin = hasFrame ? 1.0f : 0.0f;
        collector.submitCustomGeometry(poses, canvasType, (pose, vc) ->
                renderFaces(canvasObj, pose, vc, getLight(light, glowing), widthPixels, heightPixels, margin));

        // Frame
        if (hasFrame) {
            collector.submitCustomGeometry(poses, RenderTypes.entityCutout(state.materialId), (pose, vc) ->
                    renderFrame(state.frameId, pose, vc, getFrameLight(light, glowing), widthPixels, heightPixels));
        }

        poses.popPose();
        super.submit(state, poses, collector, camera);
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

    private List<Face> getFaces(Identifier frame, String part) {
        Identifier id = Identifier.fromNamespaceAndPath(frame.getNamespace(), frame.getPath() + "/" + part + ".obj");
        if (ObjectLoader.objects.containsKey(id)) {
            return ObjectLoader.objects.get(id);
        }
        return List.of();
    }

    private void renderFrame(Identifier frame, PoseStack.Pose pose, VertexConsumer vertexConsumer, int light, float width, float height) {
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
        vertexConsumer.addVertex(pose, x, y, z - 0.5f).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, normalX, normalY, normalZ);
    }
}
