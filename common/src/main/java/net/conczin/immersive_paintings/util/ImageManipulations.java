package net.conczin.immersive_paintings.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.twelvemonkeys.image.ImageUtil;
import net.conczin.immersive_paintings.Painting.Size;
import net.conczin.immersive_paintings.registration.Configs;
import org.apache.logging.log4j.util.TriConsumer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ImageManipulations {
    public static BufferedImage decode(byte[] bytes) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    public static byte[] encode(BufferedImage image) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", stream);
        return stream.toByteArray();
    }

    public static void processByteArrayInChunks(byte[] input, TriConsumer<byte[], Integer, Integer> consumer) {
        int packetSize = Configs.COMMON.packetSize;
        int splits = (int)Math.ceil((double)input.length / packetSize);
        int split = 0;
        for (int i = 0; i < input.length; i += packetSize) {
            byte[] b = Arrays.copyOfRange(input, i, Math.min(input.length, i + packetSize));
            consumer.accept(b, split, splits);
            split++;
        }
    }

    public static NativeImage bufferedToNative(BufferedImage image) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
        ColorModel model = image.getColorModel();

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Object elements = image.getRaster().getDataElements(x, y, null);

                int abgr = (model.getAlpha(elements) << 24) |
                        (model.getBlue(elements) << 16) |
                        (model.getGreen(elements) << 8) |
                        model.getRed(elements);

                nativeImage.setPixelABGR(x, y, abgr);
            }
        }
        return nativeImage;
    }

    public static BufferedImage resizeImage(BufferedImage in, Size size) {
        int w = in.getWidth();
        int h = in.getHeight();

        switch (size) {
            case Size.FULL -> {
                return in;
            }
            case Size.HALF -> {
                w /= 2;
                h /= 2;
            }
            case Size.QUARTER -> {
                w /= 4;
                h /= 4;
            }
            case Size.EIGHTH -> {
                w /= 8;
                h /= 8;
            }
            case Size.THUMBNAIL -> {
                float z = Math.min(
                        (float) Configs.CLIENT.thumbnailSize / w,
                        (float) Configs.CLIENT.thumbnailSize / h
                );

                // Additional check for tiny thumbnails relative to their parent image
                // This is roughly 128 / 4000, so any image that large should have a slightly larger thumbnail
                if (z < 0.032)
                    z = 0.032f;

                // The thumbnail would not be smaller than the actual painting
                // NOTE: This cannot be (int) cast because rounding errors can produce 0x0 images
                if (z < 1.0f) {
                    w *= z;
                    h *= z;
                }

                // If the zoom didn't change, then the original image is small enough already
                if (w == in.getWidth())
                    return in;
            }
            case Size.NSFW -> {
                // NSFW Images can only be resized from thumbnails, so there's no need to downscale
                return ImageUtil.blur(in, (float) Configs.CLIENT.thumbnailSize / 8);
            }
        }

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        resize(out, in, (float)in.getWidth() / w, 0, 0);
        return out;
    }

    // Color.HSBtoRGB returns 255 alpha for all pixels, so we need to apply our own alpha
    // 16777215 is the opposite of the number set in Color.HSBtoRGB
    private static int HSBtoARGB(float[] hsv, int alpha) {
        int colorNoAlpha = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) & 16777215;
        return (alpha << 24) | colorNoAlpha;
    }

    public static void resize(BufferedImage image, BufferedImage source, float zoom, int ox, int oy) {
        ColorModel sourceModel = source.getColorModel();

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int red = 0, green = 0, blue = 0, alpha = 0;
                int samples = 0;
                for (int px = Math.max(0, (int)(ox + zoom * x)); px < Math.min(source.getWidth(), ox + zoom * (x + 1)); px++) {
                    for (int py = Math.max(0, (int)(oy + zoom * y)); py < Math.min(source.getHeight(), oy + zoom * (y + 1)); py++) {
                        Object elements = source.getRaster().getDataElements(px, py, null);

                        red += sourceModel.getRed(elements);
                        green += sourceModel.getGreen(elements);
                        blue += sourceModel.getBlue(elements);
                        alpha += sourceModel.getAlpha(elements);

                        samples++;
                    }
                }

                if (samples > 0) {
                    red /= samples;
                    green /= samples;
                    blue /= samples;
                    alpha /= samples;
                }

                image.setRGB(x, y,(alpha << 24) | (red << 16) | (green << 8) | blue);
            }
        }
    }

    public static void dither(BufferedImage image, double dither) {
        ColorModel model = image.getColorModel();

        float[] hsv = new float[3];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Object elements = image.getRaster().getDataElements(x, y, null);
                Color.RGBtoHSB(model.getRed(elements), model.getGreen(elements), model.getBlue(elements), hsv);

                for (int i = 1; i < 3; i++) {
                    if (x % 2 == y % 2) {
                        hsv[i] = (float)Math.min(1.0f, hsv[i] + dither * 0.5);
                    } else {
                        hsv[i] = (float)Math.max(0.0f, hsv[i] - dither * 0.5);
                    }
                }

                image.setRGB(x, y, HSBtoARGB(hsv, model.getAlpha(elements)));
            }
        }
    }

    public static void reduceColors(BufferedImage image, int bins) {
        float[] hsv = new float[3];
        float[][] hist = new float[3][256];

        final int EXCLUDE_HUE = 1;

        ColorModel model = image.getColorModel();

        // base
        int base = image.getWidth() * image.getHeight();
        for (int channel = EXCLUDE_HUE; channel < 3; channel++) {
            for (int x = 0; x < 256; x++) {
                hist[channel][x] = base / 255.0f;
            }
        }

        // create histogram
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Object elements = image.getRaster().getDataElements(x, y, null);
                Color.RGBtoHSB(model.getRed(elements), model.getGreen(elements), model.getBlue(elements), hsv);

                for (int i = 0; i < 3; i++) {
                    hist[i][toByte(hsv[i])]++;
                }
            }
        }

        // find bin boundaries and calculate centers
        int binSize = (image.getWidth() * image.getHeight() + base) / bins;
        float[][] lookup = new float[3][256];
        for (int channel = EXCLUDE_HUE; channel < 3; channel++) {
            int start = 0;
            for (int bin = 0; bin < bins; bin++) {
                int end = start;
                int sum = 0;
                int pixels = 0;
                while (pixels <= binSize && end < 256) {
                    float v = hist[channel][end];
                    pixels += (int) v;
                    sum += (int) (end * v);
                    end++;
                }

                for (int b = start; b < end; b++) {
                    lookup[channel][b] = (float)sum / pixels / 255.0f;
                }

                start = end;
            }
        }

        // assign to new bins
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Object elements = image.getRaster().getDataElements(x, y, null);
                Color.RGBtoHSB(model.getRed(elements), model.getGreen(elements), model.getBlue(elements), hsv);

                for (int channel = EXCLUDE_HUE; channel < 3; channel++) {
                    hsv[channel] = lookup[channel][toByte(hsv[channel])];
                }

                image.setRGB(x, y, HSBtoARGB(hsv, model.getAlpha(elements)));
            }
        }
    }

    private static int toByte(float v) {
        return Math.min(255, Math.max(0, (int)(v * 255)));
    }

    public static int scanForPixelArtMultiple(BufferedImage image) {
        int[] hist = new int[64];
        for (int y = 0; y < image.getHeight(); y += 7) {
            int l = 0;
            int lastColor = 0;
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getRGB(x, y);
                if (x == 0 || lastColor == color) {
                    l++;
                } else {
                    if (l < hist.length) {
                        hist[l]++;
                    }
                    l = 1;
                }
                lastColor = color;
            }
        }

        int bestScore = 0;
        int best = 1;
        for (int i = 1; i < hist.length; i++) {
            if (hist[i] > bestScore) {
                bestScore = hist[i];
                best = i;
            }
        }

        return best;
    }
}
