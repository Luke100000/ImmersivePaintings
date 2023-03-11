package immersive_paintings.util;

import immersive_paintings.resources.ByteImage;

public class ImageManipulations {
    public static int scanForPixelArtMultiple(ByteImage image) {
        int[] hist = new int[64];
        for (int y = 0; y < image.getHeight(); y += 7) {
            int l = 0;
            int lastColor = 0;
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getARGB(x, y);
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

    public static void resize(ByteImage image, ByteImage source, double zoom, int ox, int oy) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int red = 0, green = 0, blue = 0;
                int samples = 0;
                for (int px = Math.max(0, (int)(ox + zoom * x)); px < Math.min(source.getWidth(), ox + zoom * (x + 1)); px++) {
                    for (int py = Math.max(0, (int)(oy + zoom * y)); py < Math.min(source.getHeight(), oy + zoom * (y + 1)); py++) {
                        int index = source.getIndex(px, py);
                        byte[] bytes = source.getBytes();
                        red += (bytes[index] & 0xFF);
                        green += (bytes[index + 1] & 0xFF);
                        blue += (bytes[index + 2] & 0xFF);
                        samples++;
                    }
                }
                if (samples > 0) {
                    red /= samples;
                    green /= samples;
                    blue /= samples;
                }
                image.setPixel(x, y, red, green, blue);
            }
        }
    }

    public static void dither(ByteImage image, double dither) {
        float[] hsv = new float[3];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.getHSV(hsv, x, y);

                for (int i = 1; i < 3; i++) {
                    if (x % 2 == y % 2) {
                        hsv[i] = (float)Math.min(1.0f, hsv[i] + dither * 0.5);
                    } else {
                        hsv[i] = (float)Math.max(0.0f, hsv[i] - dither * 0.5);
                    }
                }

                image.setHSV(x, y, hsv);
            }
        }
    }

    public static void reduceColors(ByteImage image, int bins) {
        float[] hsv = new float[3];
        float[][] hist = new float[3][256];

        final int EXCLUDE_HUE = 1;

        // base
        int base = image.getWidth() * image.getHeight() / 255;
        for (int channel = EXCLUDE_HUE; channel < 3; channel++) {
            for (int x = 0; x < 256; x++) {
                hist[channel][x] = base;
            }
        }

        // create histogram
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.getHSV(hsv, x, y);

                for (int i = 0; i < 3; i++) {
                    hist[i][toByte(hsv[i])]++;
                }
            }
        }

        // find bin boundaries and calculate centers
        int binSize = image.getWidth() * image.getHeight() / bins + base * 255 / bins;
        float[][] lookup = new float[3][256];
        for (int channel = EXCLUDE_HUE; channel < 3; channel++) {
            int start = 0;
            int sum = 0;
            for (int bin = 0; bin < bins; bin++) {
                int end = start;
                int avg = 0;
                while ((bin == bins - 1 || sum <= binSize) && end < 255) {
                    float v = hist[channel][end];
                    sum += v;
                    avg += end * v;
                    end++;
                }

                if (start != end) {
                    for (int b = start; b < end + 1; b++) {
                        lookup[channel][b] = (float)avg / sum / 255.0f;
                    }
                }

                start = end;
                sum -= binSize;
            }
        }

        // assign to new bins
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.getHSV(hsv, x, y);

                for (int channel = EXCLUDE_HUE; channel < 3; channel++) {
                    hsv[channel] = lookup[channel][toByte(hsv[channel])];
                }

                image.setHSV(x, y, hsv);
            }
        }
    }

    private static int toByte(float v) {
        return Math.min(255, Math.max(0, (int)(v * 255)));
    }
}
