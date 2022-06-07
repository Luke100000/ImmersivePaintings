package immersive_paintings.util;

import net.minecraft.client.texture.NativeImage;

import static java.awt.Color.RGBtoHSB;

public class ImageManipulations {
    public static int scanForPixelArtMultiple(NativeImage image) {
        int[] hist = new int[64];
        for (int y = 0; y < image.getHeight(); y += 7) {
            int l = 0;
            int lastColor = 0;
            for (int x = 0; x < image.getHeight(); x++) {
                int color = image.getColor(x, y);
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

    public static void resize(NativeImage image, NativeImage source, double zoom, int ox, int oy) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                double red = 0, green = 0, blue = 0, alpha = 0;
                int samples = 0;
                for (int px = Math.max(0, (int)(ox + zoom * x)); px < Math.min(source.getWidth() - 1, ox + zoom * (x + 1)); px++) {
                    for (int py = Math.max(0, (int)(oy + zoom * y)); py < Math.min(source.getHeight() - 1, oy + zoom * (y + 1)); py++) {
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

        // base
        int base = image.getWidth() * image.getHeight() / 255 * 8;
        for (int i = 0; i < 3; i++) {
            for (int x = 0; x < 256; x++) {
                hist[i][x] = base;
            }
        }

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
        //todo hue
        int binSize = image.getWidth() * image.getHeight() / bins + base * 255 / bins;
        float[][] lookup = new float[3][256];
        for (int channel = 0; channel < 3; channel++) {
            int start = 0;
            int sum = 0;
            for (int bin = 0; bin < bins; bin++) {
                int end = start;
                int avg = 0;
                while ((bin == bins - 1 || sum <= binSize) && end < 255) {
                    sum += hist[channel][end];
                    avg += end * hist[channel][end];
                    end++;
                }

                for (int b = start; b < end + 1; b++) {
                    lookup[channel][b] = (float)avg / sum / 255.0f;
                }

                start = end;
                sum -= binSize;
            }
        }

        // assign to new bins
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                getHSV(hsv, image.getColor(x, y));

                for (int channel = 0; channel < 3; channel++) {
                    hsv[channel] = lookup[channel][toByte(hsv[channel])];
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

    public static int[] imageToInts(NativeImage image) {
        int[] is = new int[image.getWidth() * image.getHeight()];
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                is[x + y * image.getWidth()] = image.getColor(x, y);
            }
        }
        return is;
    }

    public static NativeImage intsToImage(int width, int height, int[] is) {
        NativeImage image = new NativeImage(width, height, false);
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                image.setColor(x, y, is[x + y * image.getWidth()]);
            }
        }
        return image;
    }
}
