package co.deepthought.imagine.server;

import co.deepthought.imagine.store.ImageStore;
import co.deepthought.imagine.store.Size;
import com.sleepycat.je.DatabaseException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;

import java.io.FileInputStream;

import java.util.Properties;

public class ImageServer {

    final static Logger LOGGER = Logger.getLogger(ImageServer.class.getCanonicalName());

    public static void main(final String[] args) throws Exception {
        PropertyConfigurator.configure("resources/log4j.properties");
        final Properties prop = new Properties();
        prop.load(new FileInputStream("resources/config.properties"));
        final ImageServer imageServer = new ImageServer(prop);
        imageServer.startServer();
    }

    private final int port;
    private final ImageStore store;
    private final Size fingerprintSizeLarge;
    private final Size fingerprintSizeSmall;
    private final String imagedir;

    public ImageServer(final Properties prop) throws DatabaseException {
        this.port = Integer.parseInt(prop.getProperty("port"));
        this.store = new ImageStore(prop.getProperty("dbfile"));
        this.fingerprintSizeLarge = new Size(prop.getProperty("fingerprintResolutionLarge"));
        this.fingerprintSizeSmall = new Size(prop.getProperty("fingerprintResolutionSmall"));
        this.imagedir = prop.getProperty("imagedir");
    }

    public void startServer() throws Exception {
        Server server = new Server(this.port);
        final HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] {
            new SideloadHandler(this.store, this.fingerprintSizeLarge, this.fingerprintSizeSmall, this.imagedir),
            new ImageHandler()
        });
        server.setHandler(handlers);
        server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", -1);
        server.start();
        server.join();
    }

}
