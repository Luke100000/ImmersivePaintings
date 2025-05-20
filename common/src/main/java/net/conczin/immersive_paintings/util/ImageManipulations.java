package net.conczin.immersive_paintings.util;

import com.mojang.blaze3d.platform.NativeImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImageManipulations {
    public static void write(BufferedImage image, File file) {
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage decode(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encode(BufferedImage image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stream.toByteArray();
    }

    public static NativeImage bufferedToNative(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        NativeImage nativeImage = new NativeImage(width, height, false);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = image.getRGB(x, y);
                int a = argb >> 24 & 255;
                int r = argb >> 16 & 255;
                int g = argb >> 8 & 255;
                int b = argb & 255;

                nativeImage.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
        return nativeImage;
    }

    public static void resize(BufferedImage image, BufferedImage source, double zoom, int ox, int oy) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int red = 0, green = 0, blue = 0, alpha = 0;
                int samples = 0;
                for (int px = Math.max(0, (int)(ox + zoom * x)); px < Math.min(source.getWidth(), ox + zoom * (x + 1)); px++) {
                    for (int py = Math.max(0, (int)(oy + zoom * y)); py < Math.min(source.getHeight(), oy + zoom * (y + 1)); py++) {
                        int rgb = source.getRGB(px, py);

                        red += rgb >> 16 & 255;
                        green += rgb >> 8 & 255;
                        blue += rgb & 255;
                        alpha += rgb >> 24 & 255;
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
        float[] hsv = new float[3];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int argb = image.getRGB(x, y);
                Color.RGBtoHSB(argb >> 16 & 255, argb >> 8 & 255, argb & 255, hsv);

                for (int i = 1; i < 3; i++) {
                    if (x % 2 == y % 2) {
                        hsv[i] = (float)Math.min(1.0f, hsv[i] + dither * 0.5);
                    } else {
                        hsv[i] = (float)Math.max(0.0f, hsv[i] - dither * 0.5);
                    }
                }

                image.setRGB(x, y, Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]));
            }
        }
    }

    public static void reduceColors(BufferedImage image, int bins) {
        float[] hsv = new float[3];
        float[][] hist = new float[3][256];

        final int EXCLUDE_HUE = 1;

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
                int argb = image.getRGB(x, y);
                Color.RGBtoHSB(argb >> 16 & 255, argb >> 8 & 255, argb & 255, hsv);

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
                    pixels += v;
                    sum += end * v;
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
                int argb = image.getRGB(x, y);
                Color.RGBtoHSB(argb >> 16 & 255, argb >> 8 & 255, argb & 255, hsv);

                for (int channel = EXCLUDE_HUE; channel < 3; channel++) {
                    hsv[channel] = lookup[channel][toByte(hsv[channel])];
                }

                image.setRGB(x, y, Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]));
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
