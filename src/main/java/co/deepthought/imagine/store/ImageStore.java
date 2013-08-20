package co.deepthought.imagine.store;

import java.awt.image.BufferedImage;
import java.io.InputStream;

public interface ImageStore {

    public BufferedImage readImage(final String imagePath);

    public boolean saveImage(final ImageMeta imageMeta, final InputStream image);

}