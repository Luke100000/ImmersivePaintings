package immersive_paintings.util;

import immersive_paintings.resources.ByteImage;

public class ImageManipulations {
    private static final int MAX_PIXEL_ART_MULTIPLE = 64;
    private static final int TILE_COLOR_TOLERANCE = 16;
    private static final int MAX_OFF_COLOR_PIXELS_PER_TILE = 1;

    public static int scanForPixelArtMultiple(ByteImage image) {
        int maxMultiple = Math.min(MAX_PIXEL_ART_MULTIPLE, Math.min(image.getWidth(), image.getHeight()));
        for (int multiple = maxMultiple; multiple > 1; multiple--) {
            if (image.getWidth() % multiple == 0 && image.getHeight() % multiple == 0 && isPixelArtMultiple(image, multiple)) {
                return multiple;
            }
        }

        return 1;
    }

    private static boolean isPixelArtMultiple(ByteImage image, int multiple) {
        for (int tileX = 0; tileX < image.getWidth(); tileX += multiple) {
            for (int tileY = 0; tileY < image.getHeight(); tileY += multiple) {
                if (!isSingleColorTile(image, tileX, tileY, multiple)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isSingleColorTile(ByteImage image, int tileX, int tileY, int size) {
        long red = 0;
        long green = 0;
        long blue = 0;
        long alpha = 0;
        int samples = size * size;

        for (int x = tileX; x < tileX + size; x++) {
            for (int y = tileY; y < tileY + size; y++) {
                int color = image.getARGB(x, y);
                red += (color >> 16) & 0xFF;
                green += (color >> 8) & 0xFF;
                blue += color & 0xFF;
                alpha += (color >> 24) & 0xFF;
            }
        }

        int averageRed = (int)(red / samples);
        int averageGreen = (int)(green / samples);
        int averageBlue = (int)(blue / samples);
        int averageAlpha = (int)(alpha / samples);
        int offColorPixels = 0;

        for (int x = tileX; x < tileX + size; x++) {
            for (int y = tileY; y < tileY + size; y++) {
                int color = image.getARGB(x, y);
                if (!isCloseColor(color, averageRed, averageGreen, averageBlue, averageAlpha) && ++offColorPixels > MAX_OFF_COLOR_PIXELS_PER_TILE) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isCloseColor(int color, int red, int green, int blue, int alpha) {
        return Math.abs(((color >> 16) & 0xFF) - red) <= TILE_COLOR_TOLERANCE
                && Math.abs(((color >> 8) & 0xFF) - green) <= TILE_COLOR_TOLERANCE
                && Math.abs((color & 0xFF) - blue) <= TILE_COLOR_TOLERANCE
                && Math.abs(((color >> 24) & 0xFF) - alpha) <= TILE_COLOR_TOLERANCE;
    }

    public static void resize(ByteImage image, ByteImage source, double zoom, int ox, int oy) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int red = 0, green = 0, blue = 0, alpha = 0;
                int samples = 0;
                for (int px = Math.max(0, (int)(ox + zoom * x)); px < Math.min(source.getWidth(), ox + zoom * (x + 1)); px++) {
                    for (int py = Math.max(0, (int)(oy + zoom * y)); py < Math.min(source.getHeight(), oy + zoom * (y + 1)); py++) {
                        int index = source.getIndex(px, py);
                        byte[] bytes = source.getBytes();
                        red += (bytes[index] & 0xFF);
                        green += (bytes[index + 1] & 0xFF);
                        blue += (bytes[index + 2] & 0xFF);
                        alpha += (bytes[index + 3] & 0xFF);
                        samples++;
                    }
                }
                if (samples > 0) {
                    red /= samples;
                    green /= samples;
                    blue /= samples;
                    alpha /= samples;
                }
                image.setPixel(x, y, red, green, blue, alpha);
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
        int base = image.getWidth() * image.getHeight();
        for (int channel = EXCLUDE_HUE; channel < 3; channel++) {
            for (int x = 0; x < 256; x++) {
                hist[channel][x] = base / 255.0f;
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
