package co.deepthought.imagine.server;

import co.deepthought.imagine.image.Fingerprinter;
import co.deepthought.imagine.store.ImageMeta;
import co.deepthought.imagine.store.ImageMetaStore;
import co.deepthought.imagine.store.ImageStore;
import co.deepthought.imagine.store.Size;
import com.sleepycat.je.DatabaseException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.imageio.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.color.CMMException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

public class SideloadHandler extends AbstractHandler {

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
            try {
                final String url = request.getParameter("url");
                final BufferedImage img = ImageIO.read(new URL(url));

                final Fingerprinter fingerprinter = new Fingerprinter(img);
                final String fingerprintSmall = fingerprinter.getFingerprint(this.fingerprintSizeSmall);
                final String fingerprintLarge = fingerprinter.getFingerprint(this.fingerprintSizeLarge);

                final Size size = new Size(img.getWidth(), img.getHeight());
                final ImageMeta image = new ImageMeta(fingerprintSmall, fingerprintLarge, size);
                final ImageMeta duplicate = this.metaStore.getSimilar(image, this.similarityTolerance);
                if(duplicate == null) {
                    this.metaStore.persist(image);
                    this.imageStore.saveImage(image, img);
                    this.writeSuccessMessage(response.getWriter(), image, "new");
                }
                else if(duplicate.compareTo(image) < 0) {
                    // If the new image is bigger, replace it
                    image.setId(duplicate.getId());
                    this.metaStore.persist(image);
                    this.imageStore.saveImage(image, img);
                    this.writeSuccessMessage(response.getWriter(), image, "replace-duplicate");
                }
                else {
                    // If the duplicate is big enough use it.
                    this.writeSuccessMessage(response.getWriter(), duplicate, "use-duplicate");
                }
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (DatabaseException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\": \"database error\",\"status\":\"error\"}");
            } catch (IIOException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print("{\"error\": \"image missing\",\"status\":\"error\"}");
            }
            catch(CMMException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\": \"image format problem\",\"status\":\"error\"}");
            }
            request.setHandled(true);
        }
    }

    public void writeSuccessMessage(final PrintWriter writer, final ImageMeta image, final String status) {
        writer.print("{");
        writer.print("\"width\":" + image.getSize().getWidth() + ",");
        writer.print("\"height\":" + image.getSize().getHeight() + ",");
        writer.print("\"id\":\"" + image.getId() + "\",");
        writer.print("\"fingerprintSmall\":\"" + image.getFingerprintSmall() +"\",");
        writer.print("\"fingerprintLarge\":\"" + image.getFingerprintLarge() +"\",");
        writer.print("\"status\":\"" + status + "\"");
        writer.print("}");
    }

}