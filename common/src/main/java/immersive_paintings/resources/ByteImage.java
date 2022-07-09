package immersive_paintings.resources;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static java.awt.Color.RGBtoHSB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class ByteImage {
    private final static int BANDS = 3;
    private final byte[] bytes;
    private final int width, height;

    public ByteImage(byte[] bytes, int width, int height) {
        this.bytes = bytes;
        this.width = width;
        this.height = height;
    }

    public ByteImage(int width, int height) {
        this.bytes = new byte[width * height * BANDS];
        this.width = width;
        this.height = height;
    }

    public static ByteImage read(InputStream stream) throws IOException {
        BufferedImage image = ImageIO.read(stream);

        if (image == null) {
            throw new IOException("Invalid file");
        }

        ByteImage byteImage = new ByteImage(image.getWidth(), image.getHeight());

        int[] data = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int d = data[y * image.getWidth() + x];
                byteImage.setPixel(x, y,
                        (d >> 16) & 0xFF,
                        (d >> 8) & 0xFF,
                        d & 0xFF
                );
            }
        }

        return byteImage;
    }

    public void write(File file) {
        BufferedImage bufferedImage = new BufferedImage(width, height, TYPE_INT_RGB);

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                bufferedImage.setRGB(x, y, getARGB(x, y));
            }
        }

        try {
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPixel(int x, int y, int r, int g, int b) {
        setPixel(x, y, (byte)r, (byte)g, (byte)b);
    }

    public void setPixel(int x, int y, byte r, byte g, byte b) {
        int i = getIndex(x, y);
        bytes[i] = r;
        bytes[i + 1] = g;
        bytes[i + 2] = b;
    }

    public int getIndex(int x, int y) {
        return (x * height + y) * BANDS;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void getHSV(float[] hsv, int x, int y) {
        int index = getIndex(x, y);
        RGBtoHSB(
                bytes[index] & 0xFF,
                bytes[index + 1] & 0xFF,
                bytes[index + 2] & 0xFF,
                hsv
        );
    }

    public void setHSV(int x, int y, float[] hsv) {
        int index = getIndex(x, y);

        byte r = 0, g = 0, b = 0;
        if (hsv[1] == 0) {
            r = g = b = (byte)(hsv[2] * 255.0f + 0.5f);
        } else {
            float h = (hsv[0] - (float)Math.floor(hsv[0])) * 6.0f;
            float f = h - (float)Math.floor(h);
            float p = hsv[2] * (1.0f - hsv[1]);
            float q = hsv[2] * (1.0f - hsv[1] * f);
            float t = hsv[2] * (1.0f - (hsv[1] * (1.0f - f)));
            switch ((int)h) {
                case 0 -> {
                    r = (byte)(hsv[2] * 255.0f + 0.5f);
                    g = (byte)(t * 255.0f + 0.5f);
                    b = (byte)(p * 255.0f + 0.5f);
                }
                case 1 -> {
                    r = (byte)(q * 255.0f + 0.5f);
                    g = (byte)(hsv[2] * 255.0f + 0.5f);
                    b = (byte)(p * 255.0f + 0.5f);
                }
                case 2 -> {
                    r = (byte)(p * 255.0f + 0.5f);
                    g = (byte)(hsv[2] * 255.0f + 0.5f);
                    b = (byte)(t * 255.0f + 0.5f);
                }
                case 3 -> {
                    r = (byte)(p * 255.0f + 0.5f);
                    g = (byte)(q * 255.0f + 0.5f);
                    b = (byte)(hsv[2] * 255.0f + 0.5f);
                }
                case 4 -> {
                    r = (byte)(t * 255.0f + 0.5f);
                    g = (byte)(p * 255.0f + 0.5f);
                    b = (byte)(hsv[2] * 255.0f + 0.5f);
                }
                case 5 -> {
                    r = (byte)(hsv[2] * 255.0f + 0.5f);
                    g = (byte)(p * 255.0f + 0.5f);
                    b = (byte)(q * 255.0f + 0.5f);
                }
            }
        }

        bytes[index] = r;
        bytes[index + 1] = g;
        bytes[index + 2] = b;
    }

    public int getARGB(int x, int y) {
        int index = getIndex(x, y);
        return 0xff000000 | (bytes[index + 2] & 0xFF) | ((bytes[index + 1] & 0xFF) << 8) | ((bytes[index] & 0xFF) << 16);
    }

    public int getABGR(int x, int y) {
        int index = getIndex(x, y);
        return 0xff000000 | (bytes[index] & 0xFF) | ((bytes[index + 1] & 0xFF) << 8) | ((bytes[index + 2] & 0xFF) << 16);
    }
}
