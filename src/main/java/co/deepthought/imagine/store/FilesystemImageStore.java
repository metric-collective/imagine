package co.deepthought.imagine.store;

import com.google.common.io.ByteStreams;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class FilesystemImageStore implements ImageStore {

    private final String imagedir;

    public FilesystemImageStore(final String imagedir) {
        this.imagedir = imagedir;
    }

    @Override
    public BufferedImage readImage(String imagePath) {
        final File inputFile = new File(this.imagedir + imagePath);
        try {
            return ImageIO.read(inputFile);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean saveImage(final ImageMeta imageMeta, final InputStream image) {
        final File outputFile = new File(this.imagedir + imageMeta.getId());
        try (final FileOutputStream fileStream = new FileOutputStream(outputFile)){
            ByteStreams.copy(image, fileStream);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}