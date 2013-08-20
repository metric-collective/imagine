package co.deepthought.imagine.image;

import co.deepthought.imagine.store.Size;
import com.jhlabs.image.GaussianFilter;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.BaseNCodec;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.nio.ByteBuffer;
import java.util.BitSet;

public class Fingerprinter {

    final private static BaseNCodec encoder = new Base32();
    final private static float BLUR = 3.0f;
    final private static int SAMPLE_RESOLUTION = 128;

    private final BufferedImage image;

    public static int difference(final String fingerprint1, final String fingerprint2) {
        if(fingerprint1.length() != fingerprint2.length()) {
            return Integer.MAX_VALUE;
        }
        final BitSet vector1 = BitSet.valueOf(Fingerprinter.encoder.decode(fingerprint1));
        final BitSet vector2 = BitSet.valueOf(Fingerprinter.encoder.decode(fingerprint2));
        vector1.xor(vector2);
        return vector1.cardinality();
    }

    public Fingerprinter(final BufferedImage image) {
        final BufferedImage resized = new BufferedImage(SAMPLE_RESOLUTION, SAMPLE_RESOLUTION, BufferedImage.TYPE_INT_RGB);

        final Graphics2D g = resized.createGraphics();
        g.drawImage(image, 0, 0, SAMPLE_RESOLUTION, SAMPLE_RESOLUTION, null);
        g.dispose();

        this.image = new BufferedImage(SAMPLE_RESOLUTION, SAMPLE_RESOLUTION, BufferedImage.TYPE_INT_RGB);
        final BufferedImageOp filter = new GaussianFilter(BLUR);
        filter.filter(resized, this.image);
    }

    public String getFingerprint(final Size size) {
        final double average = this.getAverage();
        final double stdev = this.getStdev(average);
        final BufferedImage sample = this.sample(size.getWidth(), size.getHeight());
        final BitSet bitSet = new BitSet();
        int count = 0;
        for(int x = 0; x < size.getWidth(); x++) {
            for(int y = 0; y < size.getHeight(); y++) {
                final double pixel = this.getPixelDarkness(sample, x, y);
                for(double divisor = -1; divisor <= 1; divisor += 1) {
                    bitSet.set(count, pixel > (average + (divisor * stdev)));
                    count++;
                }
            }
        }
        return Fingerprinter.encoder.encodeAsString(bitSet.toByteArray());
    }

    private double getAverage() {
        double sum = 0;
        for(int x = 0; x < SAMPLE_RESOLUTION; x++) {
            for(int y = 0; y < SAMPLE_RESOLUTION; y++) {
                sum += this.getPixelDarkness(this.image, x, y);
            }
        }
        return sum / (SAMPLE_RESOLUTION * SAMPLE_RESOLUTION);
    }

    private double getStdev(final double average) {
        double sum = 0;
        for(int x = 0; x < SAMPLE_RESOLUTION; x++) {
            for(int y = 0; y < SAMPLE_RESOLUTION; y++) {
                final double error = this.getPixelDarkness(this.image, x, y) - average;
                sum += error * error;
            }
        }
        return Math.sqrt(sum/(SAMPLE_RESOLUTION * SAMPLE_RESOLUTION));
    }

    private int getPixelDarkness(final BufferedImage image, final int x, final int y) {
        final int pixel = image.getRGB(x, y);
        final ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(pixel);
        final byte[] bytes = buffer.array();
        return (int)(0.21 * (bytes[3] & 0xFF) + 0.71 * (bytes[2] & 0xFF) + 0.07 * (bytes[1] & 0xFF));
    }

    private BufferedImage sample(final int xRes, final int yRes) {
        final BufferedImage sample = new BufferedImage(xRes, yRes, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = sample.createGraphics();
        g.drawImage(this.image, 0, 0, xRes, yRes, null);
        g.dispose();
        return sample;
    }

}
