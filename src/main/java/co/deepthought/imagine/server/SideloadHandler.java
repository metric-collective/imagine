package co.deepthought.imagine.server;

import co.deepthought.imagine.image.Fingerprinter;
import co.deepthought.imagine.store.Image;
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
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SideloadHandler extends AbstractHandler {

    private final Size fingerprintSizeLarge;
    private final Size fingerprintSizeSmall;
    private final ImageStore store;
    private final String imagedir;

    public SideloadHandler(
            final ImageStore store,
            final Size fingerprintSizeLarge,
            final Size fingerprintSizeSmall,
            final String imagedir) {
        this.store = store;
        this.fingerprintSizeLarge = fingerprintSizeLarge;
        this.fingerprintSizeSmall = fingerprintSizeSmall;
        this.imagedir = imagedir;
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

                final Image image = new Image(fingerprintSmall, fingerprintLarge, size);
                this.store.persist(image);

//                final File outputFile = new File(this.imagedir + image.getId());
//                ImageIO.write(img, "jpg", outputFile);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().print("{");
                response.getWriter().print("\"width\":" + size.getWidth() + ",");
                response.getWriter().print("\"height\":" + size.getHeight() + ",");
                response.getWriter().print("\"id\":\"" + image.getId() + "\",");
                response.getWriter().print("\"fingerprintSmall\":\"" + fingerprintSmall +"\",");
                response.getWriter().print("\"fingerprintLarge\":\"" + fingerprintLarge +"\",");
                response.getWriter().print("\"success\":true");
                response.getWriter().print("}");
            } catch (DatabaseException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\": \"database error\"}");
            } catch (IIOException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print("{\"error\": \"image missing\"}");
            }
            catch(CMMException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\": \"image format proble\"}");
            }
            request.setHandled(true);
        }


    }

}