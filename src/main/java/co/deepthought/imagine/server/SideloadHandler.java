package co.deepthought.imagine.server;

import co.deepthought.imagine.image.Fingerprinter;
import co.deepthought.imagine.store.ImageMeta;
import co.deepthought.imagine.store.ImageMetaStore;
import co.deepthought.imagine.store.ImageStore;
import co.deepthought.imagine.store.Size;
import com.sleepycat.je.DatabaseException;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.imageio.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.color.CMMException;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class SideloadHandler extends AbstractHandler {

    final int FILESIZE_LIMIT = 1024*1024;

    final static Logger LOGGER = Logger.getLogger(ImageServer.class.getCanonicalName());

    private final Size fingerprintSizeLarge;
    private final Size fingerprintSizeSmall;
    private final ImageMetaStore metaStore;
    private final ImageStore imageStore;

    public SideloadHandler(
            final ImageStore imageStore,
            final ImageMetaStore metaStore,
            final Size fingerprintSizeLarge,
            final Size fingerprintSizeSmall) {
        this.metaStore = metaStore;
        this.fingerprintSizeLarge = fingerprintSizeLarge;
        this.fingerprintSizeSmall = fingerprintSizeSmall;
        this.imageStore = imageStore;
    }

    @Override
    public void handle(
        final String path,
        final Request request,
        final HttpServletRequest httpServletRequest,
        final HttpServletResponse response)
    {
        if(path.equals("/sideload/")) {
            response.setContentType("application/json");
            request.setHandled(true);

            final String url = request.getParameter("url");
            final long start = System.currentTimeMillis();

            final URLConnection connec;
            try {
                final URL target = new URL(url);
                connec = target.openConnection();
                connec.setConnectTimeout(1000); // TODO: configurable
            } catch (IOException e) {
                this.writeErrorMessage(response, HttpServletResponse.SC_BAD_REQUEST, "download problem 1: " + url);
                return;
            }

            try (
                final InputStream urlInputStream = connec.getInputStream();
                final BufferedInputStream imageStream = new BufferedInputStream(urlInputStream)
            ){
                imageStream.mark(FILESIZE_LIMIT);
                final BufferedImage img = ImageIO.read(imageStream);
                imageStream.reset(); // reset for writing

                if(img == null) {
                    this.writeErrorMessage(response, HttpServletResponse.SC_BAD_REQUEST, "download problem 2: " + url);
                    return;
                }

                final Fingerprinter fingerprinter = new Fingerprinter(img);
                final String fingerprintSmall = fingerprinter.getFingerprint(this.fingerprintSizeSmall);
                final String fingerprintLarge = fingerprinter.getFingerprint(this.fingerprintSizeLarge);

                final Size size = new Size(img.getWidth(), img.getHeight());
                final ImageMeta image = new ImageMeta(fingerprintSmall, fingerprintLarge, size);
                final ImageMeta duplicate = this.dedupe(image, request);

                if(duplicate == null) {
                    this.metaStore.persist(image);
                    this.imageStore.saveImage(image, imageStream);
                    this.writeSuccessMessage(response, image, "new");
                    LOGGER.info("Sideload NEW: " + url + " in " + (System.currentTimeMillis() - start));
                }
                else if(duplicate.compareTo(image) < 0) {
                    // If the new image is bigger, replace it
                    image.setId(duplicate.getId());
                    this.metaStore.persist(image);
                    this.imageStore.saveImage(image, imageStream);
                    this.writeSuccessMessage(response, image, "replace-duplicate");
                    LOGGER.info("Sideload REPLACE-DUPE: " + url +
                        " - " + duplicate.getId() +
                        " in " + (System.currentTimeMillis()-start));
                }
                else {
                    // If the duplicate is big enough use it.
                    this.writeSuccessMessage(response, duplicate, "use-duplicate");
                    LOGGER.info("Sideload USE-DUPE: " + url +
                        " - " + duplicate.getId() +
                        " in " + (System.currentTimeMillis()-start));
                }
            } catch (DatabaseException e) {
                this.writeErrorMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "database error");
            } catch (IOException e) {
                this.writeErrorMessage(response, HttpServletResponse.SC_BAD_REQUEST, "download problem 3: " + url);
            }
        }
    }

    public ImageMeta dedupe(final ImageMeta imageMeta, final Request request) throws DatabaseException {
        final String tolerance = request.getParameter("tolerance");
        if(tolerance == null) {
            return null;
        }
        else {
            try {
                final int toleranceValue = Integer.parseInt(tolerance);
                return this.metaStore.getSimilar(imageMeta, toleranceValue);
            } catch(NumberFormatException e) {
                return null;
            }
        }
    }

    public void writeSuccessMessage(final HttpServletResponse response, final ImageMeta image, final String status) {
        response.setStatus(HttpServletResponse.SC_OK);
        final PrintWriter writer;
        try {
            writer = response.getWriter();
            writer.print("{");
            writer.print("\"width\":" + image.getSize().getWidth() + ",");
            writer.print("\"height\":" + image.getSize().getHeight() + ",");
            writer.print("\"id\":\"" + image.getId() + "\",");
            writer.print("\"fingerprintSmall\":\"" + image.getFingerprintSmall() + "\",");
            writer.print("\"fingerprintLarge\":\"" + image.getFingerprintLarge() + "\",");
            writer.print("\"status\":\"" + status + "\"");
            writer.print("}");
        } catch (IOException e) {
            // fuck it
        }
    }

    private void writeErrorMessage(final HttpServletResponse response, final int status, final String msg) {
        LOGGER.info("Error: " + msg);
        response.setStatus(status);
        try {
            response.getWriter().print("{\"error\": \"" + msg + "\",\"status\":\"error\"}");
        } catch (IOException e) {
            // fuck it
        }
    }

}