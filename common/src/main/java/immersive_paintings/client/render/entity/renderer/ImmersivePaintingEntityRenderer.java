package immersive_paintings.client.render.entity.renderer;

import immersive_paintings.Config;
import immersive_paintings.Main;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.resources.ClientPaintingManager;
import immersive_paintings.resources.ObjectLoader;
import immersive_paintings.resources.Painting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import owens.oobjloader.Face;
import owens.oobjloader.FaceVertex;

import java.util.List;

public class ImmersivePaintingEntityRenderer<T extends ImmersivePaintingEntity> extends EntityRenderer<T> {
    public ImmersivePaintingEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        matrixStack.push();
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-entity.getPitch(tickDelta)));
        matrixStack.scale(0.0625f, 0.0625f, 0.0625f);
        renderPainting(matrixStack, vertexConsumerProvider, entity);
        matrixStack.pop();
        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
    }

    @Override
    public Identifier getTexture(T paintingEntity) {
        MinecraftClient client = MinecraftClient.getInstance();
        Config config = Config.getInstance();

        ClientPlayerEntity player = client.player;
        double distance = (player == null ? 0 : player.getPos().distanceTo(paintingEntity.getPos()));
        double blocksVisible = Math.tan(client.options.getFov().getValue() / 180.0 * Math.PI / 2.0) * 2.0 * distance;
        int resolution = ClientPaintingManager.getPainting(paintingEntity.getMotive()).resolution;
        double pixelDensity = blocksVisible * resolution / client.getWindow().getHeight();

        Painting.Type type = pixelDensity > config.eighthResolutionThreshold ? Painting.Type.EIGHTH
                : pixelDensity > config.quarterResolutionThreshold ? Painting.Type.QUARTER
                : pixelDensity > config.halfResolutionThreshold ? Painting.Type.HALF
                : Painting.Type.FULL;

        return ClientPaintingManager.getPaintingTexture(paintingEntity.getMotive(), type).textureIdentifier;
    }

    protected int getLight(int light) {
        return light;
    }

    protected int getFrameLight(int light) {
        return light;
    }

    private void renderPainting(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, T entity) {
        int light = WorldRenderer.getLightmapCoordinates(entity.world, entity.getBlockPos());

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f posMat = entry.getPositionMatrix();
        Matrix3f normMat = entry.getNormalMatrix();

        VertexConsumer vertexConsumer;

        boolean hasFrame = !entity.getFrame().getPath().equals("none");

        int width = entity.getWidthPixels();
        int height = entity.getHeightPixels();

        //canvas
        vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(getTexture(entity)));
        renderFaces("objects/canvas.obj", posMat, normMat, vertexConsumer, getLight(light), width, height, hasFrame ? 1.0f : 0.0f);

        int frameLight = getFrameLight(light);
        if (hasFrame) {
            vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutout(entity.getMaterial()));
            renderFrame(entity.getFrame(), posMat, normMat, vertexConsumer, frameLight, width, height);
        }
    }

    private void renderFaces(String name, Matrix4f posMat, Matrix3f normMat, VertexConsumer vertexConsumer, int light, float width, float height, float margin) {
        List<Face> faces = ObjectLoader.objects.get(Main.locate(name));
        for (Face face : faces) {
            for (FaceVertex v : face.vertices) {
                vertex(posMat,
                        normMat,
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
        Identifier id = new Identifier(frame.getNamespace(), frame.getPath() + "/" + part + ".obj");
        if (ObjectLoader.objects.containsKey(id)) {
            return ObjectLoader.objects.get(id);
        } else {
            return List.of();
        }
    }

    private void renderFrame(Identifier frame, Matrix4f posMat, Matrix3f normMat, VertexConsumer vertexConsumer, int light, float width, float height) {
        List<Face> faces = getFaces(frame, "bottom");
        for (int x = 0; x < width / 16; x++) {
            float u = width == 16 ? 0.75f : (x == 0 ? 0.0f : x == width / 16 - 1 ? 0.5f : 0.25f);
            for (Face face : faces) {
                for (FaceVertex v : face.vertices) {
                    vertex(posMat, normMat, vertexConsumer, v.v.x + x * 16 - (width - 16) / 2, v.v.y - (height - 16) / 2, v.v.z, v.t.u * 0.25f + u, (1.0f - v.t.v), v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
        faces = getFaces(frame, "top");
        for (int x = 0; x < width / 16; x++) {
            float u = width == 16 ? 0.75f : (x == 0 ? 0.0f : x == width / 16 - 1 ? 0.5f : 0.25f);
            for (Face face : faces) {
                for (FaceVertex v : face.vertices) {
                    vertex(posMat, normMat, vertexConsumer, v.v.x + x * 16 - (width - 16) / 2, v.v.y + (height - 16) / 2, v.v.z, v.t.u * 0.25f + u, (1.0f - v.t.v), v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
        faces = getFaces(frame, "right");
        for (int y = 0; y < height / 16; y++) {
            float u = 0.25f;
            for (Face face : faces) {
                for (FaceVertex v : face.vertices) {
                    vertex(posMat, normMat, vertexConsumer, v.v.x + (width - 16) / 2, v.v.y + y * 16 - (height - 16) / 2, v.v.z, v.t.u * 0.25f + u, (1.0f - v.t.v), v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
        faces = getFaces(frame, "left");
        for (int y = 0; y < height / 16; y++) {
            float u = 0.25f;
            for (Face face : faces) {
                for (FaceVertex v : face.vertices) {
                    vertex(posMat, normMat, vertexConsumer, v.v.x - (width - 16) / 2, v.v.y + y * 16 - (height - 16) / 2, v.v.z, v.t.u * 0.25f + u, (1.0f - v.t.v), v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
    }

    private void vertex(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertexConsumer, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ, int light) {
        vertexConsumer.vertex(positionMatrix, x, y, z - 0.5f).color(255, 255, 255, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, normalX, normalY, normalZ).next();
    }
}