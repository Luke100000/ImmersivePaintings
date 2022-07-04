package immersive_paintings.client;

import immersive_paintings.resources.ByteImage;
import net.minecraft.client.texture.NativeImage;

public class ClientUtils {
    public static NativeImage byteImageToNativeImage(ByteImage image) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                nativeImage.setColor(x, y, image.getABGR(x, y));
            }
        }
        return nativeImage;
    }
}
