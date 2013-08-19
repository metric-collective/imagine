package co.deepthought.imagine.store;

import java.awt.image.BufferedImage;

public interface ImageStore {

    public BufferedImage readImage(final String imagePath);

    public boolean saveImage(final ImageMeta imageMeta, final BufferedImage image);

}