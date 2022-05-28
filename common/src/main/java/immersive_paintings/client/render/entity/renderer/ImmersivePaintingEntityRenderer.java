package immersive_paintings.client.render.entity.renderer;

import immersive_paintings.Main;
import immersive_paintings.data.ObjectLoader;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.resources.ClientPaintingManager;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import owens.oobjloader.Face;
import owens.oobjloader.FaceVertex;

import java.util.List;

public class ImmersivePaintingEntityRenderer extends EntityRenderer<ImmersivePaintingEntity> {
    public ImmersivePaintingEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(ImmersivePaintingEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f - f));
        matrixStack.scale(0.0625f, 0.0625f, 0.0625f);
        renderPainting(matrixStack, vertexConsumerProvider, entity);
        matrixStack.pop();
        super.render(entity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(ImmersivePaintingEntity paintingEntity) {
        return ClientPaintingManager.getPainting(paintingEntity.getMotive()).textureIdentifier;
    }

    private void renderPainting(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, ImmersivePaintingEntity entity) {
        int width = entity.getWidthPixels();
        int height = entity.getHeightPixels();

        //dimensions
        float left = (-width) / 2.0f;
        float right = left + width;
        float bottom = (-height) / 2.0f;
        float top = bottom + height;

        //light
        int centerX = entity.getBlockX();
        int centerY = MathHelper.floor(entity.getY() + (double)((top + bottom) / 2.0f / 16.0f));
        int centerZ = entity.getBlockZ();
        Direction direction = entity.getHorizontalFacing();
        switch (direction) {
            case NORTH -> centerX = MathHelper.floor(entity.getX() + (double)((right + left) / 2.0f / 16.0f));
            case WEST -> centerZ = MathHelper.floor(entity.getZ() - (double)((right + left) / 2.0f / 16.0f));
            case SOUTH -> centerX = MathHelper.floor(entity.getX() - (double)((right + left) / 2.0f / 16.0f));
            case EAST -> centerZ = MathHelper.floor(entity.getZ() + (double)((right + left) / 2.0f / 16.0f));
        }
        int light = WorldRenderer.getLightmapCoordinates(entity.world, new BlockPos(centerX, centerY, centerZ));

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f posMat = entry.getPositionMatrix();
        Matrix3f normMat = entry.getNormalMatrix();

        VertexConsumer vertexConsumer;

        //canvas
        vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(getTexture(entity)));
        renderFaces("canvas", posMat, normMat, vertexConsumer, light, width, height, 1.0f);

        vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(Main.locate("textures/block/frame_oak.png")));
        renderFrame("wooden_frame", posMat, normMat, vertexConsumer, light, width, height);
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
                        v.t.u * (height - margin * 2) / height + margin / height,
                        (1.0f - v.t.v) * (width - margin * 2) / width + margin / width,
                        v.n.x,
                        v.n.y,
                        v.n.z,
                        light);
            }
        }
    }

    private void renderFrame(String name, Matrix4f posMat, Matrix3f normMat, VertexConsumer vertexConsumer, int light, float width, float height) {
        List<Face> faces = ObjectLoader.objects.get(Main.locate(name + "_bottom"));
        for (int x = 0; x < width / 16; x++) {
            for (Face face : faces) {
                for (FaceVertex v : face.vertices) {
                    vertex(posMat, normMat, vertexConsumer, v.v.x + x * 16 - (width - 16) / 2, v.v.y - (height - 16) / 2, v.v.z + 1, v.t.u, (1.0f - v.t.v), v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
        faces = ObjectLoader.objects.get(Main.locate(name + "_top"));
        for (int x = 0; x < width / 16; x++) {
            for (Face face : faces) {
                for (FaceVertex v : face.vertices) {
                    vertex(posMat, normMat, vertexConsumer, v.v.x + x * 16 - (width - 16) / 2, v.v.y + (height - 16) / 2, v.v.z + 1, v.t.u, (1.0f - v.t.v), v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
        faces = ObjectLoader.objects.get(Main.locate(name + "_left"));
        for (int y = 0; y < height / 16; y++) {
            for (Face face : faces) {
                for (FaceVertex v : face.vertices) {
                    vertex(posMat, normMat, vertexConsumer, v.v.x + (width - 16) / 2, v.v.y + y * 16 - (height - 16) / 2, v.v.z + 1, v.t.u, (1.0f - v.t.v), v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
        faces = ObjectLoader.objects.get(Main.locate(name + "_right"));
        for (int y = 0; y < height / 16; y++) {
            for (Face face : faces) {
                for (FaceVertex v : face.vertices) {
                    vertex(posMat, normMat, vertexConsumer, v.v.x - (width - 16) / 2, v.v.y + y * 16 - (height - 16) / 2, v.v.z + 1, v.t.u, (1.0f - v.t.v), v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
    }

    private void vertex(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertexConsumer, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ, int light) {
        vertexConsumer.vertex(positionMatrix, x, y, z).color(255, 255, 255, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, normalX, normalY, normalZ).next();
    }
}
