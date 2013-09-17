package co.deepthought.imagine.server;

import co.deepthought.imagine.image.Resizer;
import co.deepthought.imagine.store.ImageStore;
import co.deepthought.imagine.store.Size;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class ImageHandler extends AbstractHandler {

    final static Logger LOGGER = Logger.getLogger(ImageServer.class.getCanonicalName());

    private final File cacheDir;
    private final ImageStore store;

    public ImageHandler(final ImageStore store, final File cacheDir) {
        this.store = store;
        this.cacheDir = cacheDir;
    }

    @Override
    public void handle(
        final String path,
        final Request request,
        final HttpServletRequest httpServletRequest,
        final HttpServletResponse response) throws IOException, ServletException
    {
        if(path.startsWith("/image/")) {
            request.setHandled(true);
            final long start = System.currentTimeMillis();
            response.setContentType("image/jpeg");
            final String imagePath = path.substring(7);
            final BufferedImage srcImage = this.store.readImage(imagePath);
            if(srcImage == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            else {
                try (final OutputStream os = response.getOutputStream()){
                    final Resizer resizer = this.getResizer(srcImage, request);
                    resizer.writeImage(srcImage, os, this.cacheDir);
                }
                response.setStatus(HttpServletResponse.SC_OK);
                LOGGER.info("Image " + path +
                    " in " + (System.currentTimeMillis() - start));
            }
        }
    }

    private Resizer getResizer(final BufferedImage image, final Request request) {
        final Resizer.Mode mode = this.getMode(request);
        final Resizer.Scale scale = this.getScale(request);
        final int width = this.getWidth(image, request);
        final int height = this.getHeight(image, request);
        final Color color = this.getColor(request);
        final float quality = this.getQuality(request);
        return new Resizer(mode, scale, new Size(width, height), color, quality);
    }

    private float getQuality(Request request) {
        final String qualityParam = request.getParameter("quality");
        float quality;
        try {
            quality = Integer.parseInt(qualityParam) / 100.f;
        }
        catch (final NumberFormatException exc) {
            quality = 0.6f;
        }
        return quality;
    }

    private Color getColor(Request request) {
        final String colorParam = request.getParameter("bgcolor");
        Color color;
        try {
            color = Color.decode("0x"+colorParam);
        }
        catch (final NumberFormatException exc) {
            color = Color.WHITE;
        }
        return color;
    }

    private int getHeight(BufferedImage image, Request request) {
        String heightParam = request.getParameter("height");
        if(heightParam == null) {
            heightParam = request.getParameter("h");
        }
        try {
            return Integer.parseInt(heightParam);
        }
        catch (final NumberFormatException exc) {
            return image.getHeight();
        }
    }

    private int getWidth(BufferedImage image, Request request) {
        String widthParam = request.getParameter("width");
        if(widthParam == null) {
            widthParam = request.getParameter("w");
        }
        try {
            return Integer.parseInt(widthParam);
        }
        catch (final NumberFormatException exc) {
            return image.getWidth();
        }
    }

    private Resizer.Scale getScale(Request request) {
        final String scaleParam = request.getParameter("scale");
        final Resizer.Scale scale;
        if("both".equals(scaleParam)) {
            scale = Resizer.Scale.BOTH;
        }
        else if("canvas".equals(scaleParam)) {
            scale = Resizer.Scale.CANVAS;
        }
        else {
            scale = Resizer.Scale.DOWN;
        }
        return scale;
    }

    private Resizer.Mode getMode(Request request) {
        final String modeParam = request.getParameter("mode");
        final Resizer.Mode mode;
        if("max".equals(modeParam)) {
            mode = Resizer.Mode.MAX;
        }
        else if("crop".equals(modeParam)) {
            mode = Resizer.Mode.CROP;
        }
        else if("stretch".equals(modeParam)) {
            mode = Resizer.Mode.STRETCH;
        }
        else {
            mode = Resizer.Mode.PAD;
        }
        return mode;
    }

}
