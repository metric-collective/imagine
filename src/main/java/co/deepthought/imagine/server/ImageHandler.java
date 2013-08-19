package co.deepthought.imagine.server;

import co.deepthought.imagine.image.Resizer;
import co.deepthought.imagine.store.Size;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class ImageHandler extends AbstractHandler {

    @Override
    public void handle(
        final String path,
        final Request request,
        final HttpServletRequest httpServletRequest,
        final HttpServletResponse response) throws IOException, ServletException
    {
        if(path.startsWith("/image/")) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("image/jpeg");
            BufferedImage sourceImage = ImageIO.read(new File("/Users/kevindolan/rentenna/041.jpg"));
            final OutputStream os = response.getOutputStream();
            final Resizer resizer = new Resizer(
                Resizer.Mode.CROP,
                Resizer.Scale.DOWN,
                new Size(600, 400),
                Color.decode("0xCCCCCC"),
                0.65f
            );
            resizer.writeImage(sourceImage, os);
            request.setHandled(true);
        }
    }

}
