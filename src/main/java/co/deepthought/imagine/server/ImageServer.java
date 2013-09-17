package co.deepthought.imagine.server;

import co.deepthought.imagine.store.*;
import com.sleepycat.je.DatabaseException;

import org.apache.log4j.PropertyConfigurator;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;

import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;

public class ImageServer {

    public static void main(final String[] args) throws Exception {
        PropertyConfigurator.configure("resources/log4j.properties");
        final Properties prop = new Properties();
        prop.load(new FileInputStream("resources/config.properties"));
        final ImageServer imageServer = new ImageServer(prop);
        imageServer.startServer();
    }

    private final int port;
    private final ImageMetaStore store;
    private final Size fingerprintSizeLarge;
    private final Size fingerprintSizeSmall;
    private final ImageStore imageStore;
    private final File cacheDir;

    public ImageServer(final Properties prop) throws DatabaseException {
        this.port = Integer.parseInt(prop.getProperty("port"));
        this.store = new ImageMetaStore(prop.getProperty("dbfile"));
        this.fingerprintSizeLarge = new Size(prop.getProperty("fingerprint_large"));
        this.fingerprintSizeSmall = new Size(prop.getProperty("fingerprint_small"));
        this.cacheDir = new File(prop.getProperty("cache_dir"));

        final String storeType = prop.getProperty("store_type");
        if(storeType.equals("fs")) {
            this.imageStore = new FilesystemImageStore(prop.getProperty("store_fs_dir"));
        }
        else { // s3?
            this.imageStore = new S3ImageStore(
                prop.getProperty("store_s3_bucket"),
                prop.getProperty("store_s3_prefix"),
                prop.getProperty("store_s3_key"),
                prop.getProperty("store_s3_secret")
            );
        }
    }

    public void startServer() throws Exception {
        Server server = new Server(this.port);
        final HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] {
            new SideloadHandler(
                this.imageStore,
                this.store,
                this.fingerprintSizeLarge,
                this.fingerprintSizeSmall),
            new ImageHandler(
                this.imageStore,
                this.cacheDir
            )
        });
        server.setHandler(handlers);
        server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", -1);
        server.start();
        server.join();
    }

}