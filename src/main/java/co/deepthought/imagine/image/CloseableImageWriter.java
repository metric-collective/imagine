package co.deepthought.imagine.image;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

public class CloseableImageWriter implements AutoCloseable {

    final ImageWriter writer;

    public CloseableImageWriter(final String format) {
        this.writer = ImageIO.getImageWritersByFormatName(format).next();
    }

    @Override
    public void close() {
        this.writer.dispose();
    }

    public void setOutput(final ImageOutputStream os) {
        this.writer.setOutput(os);
    }

    public ImageWriteParam getDefaultWriteParam() {
        return this.writer.getDefaultWriteParam();
    }

    public boolean write(final IIOImage image, final ImageWriteParam param) {
        try {
            this.writer.write(null, image, param);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}