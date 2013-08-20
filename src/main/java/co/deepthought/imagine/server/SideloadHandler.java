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
import java.io.PrintWriter;
import java.net.URL;

public class SideloadHandler extends AbstractHandler {

    final int FILESIZE_LIMIT = 1048576;

    final static Logger LOGGER = Logger.getLogger(ImageServer.class.getCanonicalName());

    private final Size fingerprintSizeLarge;
    private final Size fingerprintSizeSmall;
    private final ImageMetaStore metaStore;
    private final ImageStore imageStore;
    private final int similarityTolerance;

    public SideloadHandler(
            final ImageStore imageStore,
            final ImageMetaStore metaStore,
            final Size fingerprintSizeLarge,
            final Size fingerprintSizeSmall,
            final int similarityTolerance) {
        this.metaStore = metaStore;
        this.fingerprintSizeLarge = fingerprintSizeLarge;
        this.fingerprintSizeSmall = fingerprintSizeSmall;
        this.imageStore = imageStore;
        this.similarityTolerance = similarityTolerance;
    }

    @Override
    public void handle(
        final String path,
        final Request request,
        final HttpServletRequest httpServletRequest,
        final HttpServletResponse response) throws IOException, ServletException
    {
        if(path.equals("/sideload/")) {
            response.setContentType("application/json");
            final String url = request.getParameter("url");
            try {
                final long start = System.currentTimeMillis();
                final URL target = new URL(url);
                final BufferedInputStream imageStream = new BufferedInputStream(target.openStream());
                imageStream.mark(FILESIZE_LIMIT);
                final BufferedImage img = ImageIO.read(imageStream);
                imageStream.reset(); // reset for writing

                final Fingerprinter fingerprinter = new Fingerprinter(img);
                final String fingerprintSmall = fingerprinter.getFingerprint(this.fingerprintSizeSmall);
                final String fingerprintLarge = fingerprinter.getFingerprint(this.fingerprintSizeLarge);

                final Size size = new Size(img.getWidth(), img.getHeight());
                final ImageMeta image = new ImageMeta(fingerprintSmall, fingerprintLarge, size);
                final ImageMeta duplicate = this.metaStore.getSimilar(image, this.similarityTolerance);
                if(duplicate == null) {
                    this.metaStore.persist(image);
                    this.imageStore.saveImage(image, imageStream);
                    this.writeSuccessMessage(response.getWriter(), image, "new");
                    LOGGER.info("Sideload NEW: " + url + " in " + (System.currentTimeMillis() - start));
                }
                else if(duplicate.compareTo(image) < 0) {
                    // If the new image is bigger, replace it
                    image.setId(duplicate.getId());
                    this.metaStore.persist(image);
                    this.imageStore.saveImage(image, imageStream);
                    this.writeSuccessMessage(response.getWriter(), image, "replace-duplicate");
                    LOGGER.info("Sideload REPLACE-DUPE: " + url +
                        " - " + duplicate.getId() +
                        " in " + (System.currentTimeMillis()-start));
                }
                else {
                    // If the duplicate is big enough use it.
                    this.writeSuccessMessage(response.getWriter(), duplicate, "use-duplicate");
                    LOGGER.info("Sideload USE-DUPE: " + url +
                        " - " + duplicate.getId() +
                        " in " + (System.currentTimeMillis()-start));
                }
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (DatabaseException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\": \"database error\",\"status\":\"error\"}");
            } catch (IIOException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print("{\"error\": \"image missing\",\"status\":\"error\"}");
                LOGGER.info("Sideload MISSING: " + url);
            }
            catch(CMMException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\": \"image format problem\",\"status\":\"error\"}");
                LOGGER.info("Sideload ERROR: " + url);
            }
            request.setHandled(true);
        }
    }

    public void writeSuccessMessage(final PrintWriter writer, final ImageMeta image, final String status) {
        writer.print("{");
        writer.print("\"width\":" + image.getSize().getWidth() + ",");
        writer.print("\"height\":" + image.getSize().getHeight() + ",");
        writer.print("\"id\":\"" + image.getId() + "\",");
        writer.print("\"fingerprintSmall\":\"" + image.getFingerprintSmall() + "\",");
        writer.print("\"fingerprintLarge\":\"" + image.getFingerprintLarge() + "\",");
        writer.print("\"status\":\"" + status + "\"");
        writer.print("}");
    }

}