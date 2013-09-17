package co.deepthought.imagine.image;

import co.deepthought.imagine.store.Size;
import org.imgscalr.Scalr;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class Resizer {

    public static enum Mode {
        MAX,
        PAD,
        CROP,
        STRETCH
    }

    public static enum Scale {
        DOWN,
        BOTH,
        CANVAS
    }

    private final Mode mode;
    private final Scale scale;
    private final Size size;
    private final Color bgcolor;
    private final float quality;

    public Resizer(
        final Mode mode,
        final Scale scale,
        final Size size,
        final Color bgcolor,
        final float quality) {
        this.mode = mode;
        this.scale = scale;
        this.size = size;
        this.bgcolor = bgcolor;
        this.quality = quality;
    }

    public BufferedImage resizeImage(final BufferedImage sourceImage) {
        final Size sourceSize = new Size(sourceImage.getWidth(), sourceImage.getHeight());
        final Size imageSize = this.getImageSize(sourceSize);

        final BufferedImage resized = Scalr.resize(
            sourceImage,
            Scalr.Method.AUTOMATIC,
            Scalr.Mode.FIT_EXACT,
            imageSize.getWidth(),
            imageSize.getHeight()
        );

        final Size canvasSize = this.getCanvasSize(sourceSize);
        final BufferedImage newImage = new BufferedImage(
            canvasSize.getWidth(),
            canvasSize.getHeight(),
            BufferedImage.TYPE_INT_RGB
        );

        final Graphics2D g = newImage.createGraphics();
        g.setColor(this.bgcolor);
        g.fillRect(0, 0, canvasSize.getWidth(), canvasSize.getHeight());

        final int left = (canvasSize.getWidth() - imageSize.getWidth()) / 2;
        final int top = (canvasSize.getHeight() - imageSize.getHeight()) / 2;
        g.drawImage(resized, left, top, imageSize.getWidth(), imageSize.getHeight(), null);
        g.dispose();

        return newImage;
    }

    public Size getCanvasSize(final Size sourceSize) {
        if(this.scale == Scale.CANVAS) {
            return this.size;
        }
        else {
            final Size imageSize = this.getImageSize(sourceSize);
            if(this.mode == Mode.MAX || this.mode == Mode.STRETCH) {
                return imageSize;
            }
            else if(this.mode == Mode.CROP) {
                return new Size(
                    Math.min(this.size.getWidth(), imageSize.getWidth()),
                    Math.min(this.size.getHeight(), imageSize.getHeight())
                );
            }
            else {
                return new Size(
                    Math.max(this.size.getWidth(), imageSize.getWidth()),
                    Math.max(this.size.getHeight(), imageSize.getHeight())
                );
            }
        }
    }

    public Size getImageSize(final Size sourceSize) {
        double widthProportion = this.size.getWidth() / (double) sourceSize.getWidth();
        double heightProportion = this.size.getHeight() / (double) sourceSize.getHeight();

        if(this.scale != Scale.BOTH) {
            widthProportion = Math.min(1.0, widthProportion);
            heightProportion = Math.min(1.0, heightProportion);
        }

        if(this.mode == Mode.MAX || this.mode == Mode.PAD) {
            widthProportion = Math.min(widthProportion, heightProportion);
            heightProportion = widthProportion;
        }
        else if(this.mode == Mode.CROP) {
            widthProportion = Math.max(widthProportion, heightProportion);
            heightProportion = widthProportion;
        }

        return new Size(
            (int)(widthProportion * sourceSize.getWidth()),
            (int)(heightProportion * sourceSize.getHeight())
        );

    }

    public void writeImage(
            final BufferedImage sourceImage,
            final OutputStream outputStream,
            final File cacheDir) throws IOException {

        try (
            final CloseableImageWriter writer = new CloseableImageWriter("jpg");
            // TODO: should this be in-memory?
            final ImageOutputStream imageOutputStream = new FileCacheImageOutputStream(outputStream, cacheDir)
        ) {
            writer.setOutput(imageOutputStream);
            final BufferedImage resizedImage = this.resizeImage(sourceImage);
            final IIOImage outputImage = new IIOImage(resizedImage, null, null);
            final ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(this.quality);
            writer.write(outputImage, param);
        }
    }

}
