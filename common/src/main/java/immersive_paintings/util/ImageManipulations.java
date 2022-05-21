package immersive_paintings.util;

import net.minecraft.client.texture.NativeImage;

import static java.awt.Color.RGBtoHSB;

public class ImageManipulations {
    public static void resize(NativeImage image, NativeImage source, double zoom, double offsetX, double offsetY) {
        float fx = (float)source.getWidth() / image.getWidth();
        float fy = (float)source.getHeight() / image.getHeight();
        double f = Math.min(fx, fy) / zoom;
        int ox = (int)((source.getWidth() - image.getWidth() * f) * offsetX);
        int oy = (int)((source.getHeight() - image.getHeight() * f) * offsetY);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                double red = 0, green = 0, blue = 0, alpha = 0;
                int samples = 0;
                for (int px = (int)(ox + f * x); px < Math.min(source.getWidth() - 1, ox + f * (x + 1)); px++) {
                    for (int py = (int)(oy + f * y); py < Math.min(source.getWidth() - 1, oy + f * (y + 1)); py++) {
                        int c = source.getColor(px, py);
                        red += NativeImage.getRed(c);
                        green += NativeImage.getGreen(c);
                        blue += NativeImage.getBlue(c);
                        alpha += NativeImage.getAlpha(c);
                        samples++;
                    }
                }
                red /= samples;
                green /= samples;
                blue /= samples;
                alpha /= samples;
                image.setColor(x, y, NativeImage.packColor((int)alpha, (int)blue, (int)green, (int)red));
            }
        }
    }

    public static void dither(NativeImage image, double dither) {
        float[] hsv = new float[3];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                getHSV(hsv, image.getColor(x, y));


                for (int i = 1; i < 3; i++) {
                    if (x % 2 == y % 2) {
                        hsv[i] = (float)Math.min(1.0f, hsv[i] + dither * 0.5);
                    } else {
                        hsv[i] = (float)Math.max(0.0f, hsv[i] - dither * 0.5);
                    }
                }

                int c = HSBtoRGB(hsv[0], hsv[1], hsv[2]);
                image.setColor(x, y, c);
            }
        }
    }

    public static void reduceColors(NativeImage image, int bins) {
        float[] hsv = new float[3];
        float[][] hist = new float[3][256];

        // create histogram
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                getHSV(hsv, image.getColor(x, y));

                for (int i = 0; i < 3; i++) {
                    hist[i][toByte(hsv[i])]++;
                }
            }
        }

        // find bin boundaries and calculate centers
        int binSize = image.getWidth() * image.getHeight() / bins;
        float[][] lookup = new float[3][256];
        for (int i = 0; i < 3; i++) {
            int start = 0;
            int sum = 0;
            for (int bin = 0; bin < bins; bin++) {
                int end = start;
                int avg = 0;
                while ((bin == bins - 1 || sum <= binSize) && end < 255) {
                    sum += hist[i][end];
                    avg += end * hist[i][end];
                    end++;
                }

                for (int b = start; b < end + 1; b++) {
                    lookup[i][b] = (float)avg / sum / 255.0f;
                }

                start = end;
                sum -= binSize;
            }
        }

        // assign to new bins
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                getHSV(hsv, image.getColor(x, y));

                for (int i = 0; i < 3; i++) {
                    hsv[i] = lookup[i][toByte(hsv[i])];
                }

                int c = HSBtoRGB(hsv[0], hsv[1], hsv[2]);
                image.setColor(x, y, c);
            }
        }
    }

    private static int toByte(float v) {
        return Math.min(255, Math.max(0, (int)(v * 255)));
    }

    private static void getHSV(float[] hsv, int c) {
        RGBtoHSB(
                NativeImage.getRed(c),
                NativeImage.getGreen(c),
                NativeImage.getBlue(c),
                hsv
        );
    }

    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int)(brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0f;
            float f = h - (float)java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int)h) {
                case 0 -> {
                    r = (int)(brightness * 255.0f + 0.5f);
                    g = (int)(t * 255.0f + 0.5f);
                    b = (int)(p * 255.0f + 0.5f);
                }
                case 1 -> {
                    r = (int)(q * 255.0f + 0.5f);
                    g = (int)(brightness * 255.0f + 0.5f);
                    b = (int)(p * 255.0f + 0.5f);
                }
                case 2 -> {
                    r = (int)(p * 255.0f + 0.5f);
                    g = (int)(brightness * 255.0f + 0.5f);
                    b = (int)(t * 255.0f + 0.5f);
                }
                case 3 -> {
                    r = (int)(p * 255.0f + 0.5f);
                    g = (int)(q * 255.0f + 0.5f);
                    b = (int)(brightness * 255.0f + 0.5f);
                }
                case 4 -> {
                    r = (int)(t * 255.0f + 0.5f);
                    g = (int)(p * 255.0f + 0.5f);
                    b = (int)(brightness * 255.0f + 0.5f);
                }
                case 5 -> {
                    r = (int)(brightness * 255.0f + 0.5f);
                    g = (int)(p * 255.0f + 0.5f);
                    b = (int)(q * 255.0f + 0.5f);
                }
            }
        }
        return 0xff000000 | (r) | (g << 8) | (b << 16);
    }
}
