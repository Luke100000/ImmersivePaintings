package immersive_paintings.resources;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ImageTexture extends AbstractTexture {
    protected final Identifier location;
    protected final NativeImage image;

    public ImageTexture(Identifier location) {
        this.location = location;
        image = new NativeImage(32, 32, false);
    }

    @Override
    public void load(ResourceManager manager) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> this.upload(image));
        } else {
            this.upload(image);
        }
    }

    private void upload(NativeImage image) {
        TextureUtil.prepareImage(this.getGlId(), 0, image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false, false, false, true);
    }
}

