package immersive_paintings.client.render.entity.renderer;

import immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

import java.util.Objects;

public class ImmersivePaintingEntityRenderer extends EntityRenderer<ImmersivePaintingEntity> {
    private final NativeImageBackedTexture texture;
    private final Identifier identifier;

    public ImmersivePaintingEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);

        texture = new NativeImageBackedTexture(92, 92, true);
        identifier = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("immersive_painting/", texture);
    }

    @Override
    public void render(ImmersivePaintingEntity paintingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        assert MinecraftClient.getInstance().world != null;
        double time = (MinecraftClient.getInstance().world.getTimeOfDay() % 24000) / 24000.0;
        NativeImage img = Objects.requireNonNull(texture.getImage());
        for (int x = 0; x < 92; ++x) {
            for (int y = 0; y < 92; ++y) {
                img.setColor(x, y, 0xFFFFFFFF);
            }
        }
        for (int p = 0; p < 38; p++) {
            int x = (int)(47 + p * Math.cos(time * Math.PI * 2));
            int y = (int)(47 + p * Math.sin(time * Math.PI * 2));
            img.setColor(x, y, 0xFF000000);
        }
        for (int p = 0; p < 44; p++) {
            int x = (int)(47 + p * Math.cos(time * Math.PI * 2 * 60));
            int y = (int)(47 + p * Math.sin(time * Math.PI * 2 * 60));
            img.setColor(x, y, 0xFF000000);
        }
        texture.upload();

        matrixStack.push();
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f - f));
        matrixStack.scale(0.0625f, 0.0625f, 0.0625f);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(getTexture(paintingEntity)));
        renderPainting(matrixStack, vertexConsumer, paintingEntity, paintingEntity.getWidthPixels(), paintingEntity.getHeightPixels());
        matrixStack.pop();
        super.render(paintingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(ImmersivePaintingEntity paintingEntity) {
        return identifier;
    }

    private void renderPainting(MatrixStack matrices, VertexConsumer vertexConsumer, ImmersivePaintingEntity entity, int width, int height) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f matrix4f = entry.getPositionMatrix();
        Matrix3f matrix3f = entry.getNormalMatrix();

        float left = (-width) / 2.0f;
        float right = left + width;
        float bottom = (-height) / 2.0f;
        float top = bottom + height;

        int centerX = entity.getBlockX();
        int centerY = MathHelper.floor(entity.getY() + (double)((top + bottom) / 2.0f / 16.0f));
        int centerZ = entity.getBlockZ();
        Direction direction = entity.getHorizontalFacing();
        if (direction == Direction.NORTH) {
            centerX = MathHelper.floor(entity.getX() + (double)((right + left) / 2.0f / 16.0f));
        }
        if (direction == Direction.WEST) {
            centerZ = MathHelper.floor(entity.getZ() - (double)((right + left) / 2.0f / 16.0f));
        }
        if (direction == Direction.SOUTH) {
            centerX = MathHelper.floor(entity.getX() - (double)((right + left) / 2.0f / 16.0f));
        }
        if (direction == Direction.EAST) {
            centerZ = MathHelper.floor(entity.getZ() + (double)((right + left) / 2.0f / 16.0f));
        }
        int light = WorldRenderer.getLightmapCoordinates(entity.world, new BlockPos(centerX, centerY, centerZ));

        //back
        vertex(matrix4f, matrix3f, vertexConsumer, right, bottom, 1, 0, -0.5f, 0, 0, -1, light);
        vertex(matrix4f, matrix3f, vertexConsumer, left, bottom, 0, 0, -0.5f, 0, 0, -1, light);
        vertex(matrix4f, matrix3f, vertexConsumer, left, top, 0, 1, -0.5f, 0, 0, -1, light);
        vertex(matrix4f, matrix3f, vertexConsumer, right, top, 1, 1, -0.5f, 0, 0, -1, light);

        //front
        vertex(matrix4f, matrix3f, vertexConsumer, right, top, 1, 0, 0.5f, 0, 0, 1, light);
        vertex(matrix4f, matrix3f, vertexConsumer, left, top, 0, 0, 0.5f, 0, 0, 1, light);
        vertex(matrix4f, matrix3f, vertexConsumer, left, bottom, 0, 1, 0.5f, 0, 0, 1, light);
        vertex(matrix4f, matrix3f, vertexConsumer, right, bottom, 1, 1, 0.5f, 0, 0, 1, light);

        //top
        vertex(matrix4f, matrix3f, vertexConsumer, right, top, 1, 1, -0.5f, 0, 1, 0, light);
        vertex(matrix4f, matrix3f, vertexConsumer, left, top, 0, 1, -0.5f, 0, 1, 0, light);
        vertex(matrix4f, matrix3f, vertexConsumer, left, top, 0, 1 - 1.0f / height, 0.5f, 0, 1, 0, light);
        vertex(matrix4f, matrix3f, vertexConsumer, right, top, 1, 1 - 1.0f / height, 0.5f, 0, 1, 0, light);

        //bottom
        vertex(matrix4f, matrix3f, vertexConsumer, right, bottom, 1, 1.0f / height, 0.5f, 0, -1, 0, light);
        vertex(matrix4f, matrix3f, vertexConsumer, left, bottom, 0, 1.0f / height, 0.5f, 0, -1, 0, light);
        vertex(matrix4f, matrix3f, vertexConsumer, left, bottom, 0, 0, -0.5f, 0, -1, 0, light);
        vertex(matrix4f, matrix3f, vertexConsumer, right, bottom, 1, 0, -0.5f, 0, -1, 0, light);

        //left
        vertex(matrix4f, matrix3f, vertexConsumer, right, top, 1 - 1.0f / width, 1, 0.5f, -1, 0, 0, light);
        vertex(matrix4f, matrix3f, vertexConsumer, right, bottom, 1 - 1.0f / width, 0, 0.5f, -1, 0, 0, light);
        vertex(matrix4f, matrix3f, vertexConsumer, right, bottom, 1, 0, -0.5f, -1, 0, 0, light);
        vertex(matrix4f, matrix3f, vertexConsumer, right, top, 1, 1, -0.5f, -1, 0, 0, light);

        //right
        vertex(matrix4f, matrix3f, vertexConsumer, left, top, 0, 1, -0.5f, 1, 0, 0, light);
        vertex(matrix4f, matrix3f, vertexConsumer, left, bottom, 0, 0, -0.5f, 1, 0, 0, light);
        vertex(matrix4f, matrix3f, vertexConsumer, left, bottom, 1.0f / width, 0, 0.5f, 1, 0, 0, light);
        vertex(matrix4f, matrix3f, vertexConsumer, left, top, 1.0f / width, 1, 0.5f, 1, 0, 0, light);
    }

    private void vertex(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertexConsumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int light) {
        vertexConsumer.vertex(positionMatrix, x, y, z).color(255, 255, 255, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, normalX, normalY, normalZ).next();
    }
}
