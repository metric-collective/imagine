package co.deepthought.imagine.store;

import com.sleepycat.je.*;
import com.sleepycat.persist.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 *  ImageStore persists statistics about images to the database.
 */
public class ImageStore {

    final static Logger LOGGER = Logger.getLogger(ImageStore.class.getCanonicalName());

    final private Environment environment;
    final private EntityStore store;
    final private PrimaryIndex<String, Image> imageIndex;

    public ImageStore(final String filePath) throws DatabaseException {
        final EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(false);

        final File file;
        if(filePath.equals(":tmp")) {
            final Random random = new Random();
            final String dbName = "/tmp/db-" + random.nextInt();
            file = new File(dbName);
        }
        else {
            file = new File(filePath);
        }
        file.mkdir();
        LOGGER.info("Using " + file + " for data persistence.");

        this.environment = new Environment(file, envConfig);

        final StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(false);
        this.store = new EntityStore(this.environment, "store", storeConfig);

        this.imageIndex = this.store.getPrimaryIndex(String.class, Image.class);
    }

    public Image getById(final String id) throws DatabaseException {
        return this.imageIndex.get(id);
    }

    public void persist(final Image image) throws DatabaseException {
        this.imageIndex.put(image);
    }

}