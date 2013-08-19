package co.deepthought.imagine.store;

import co.deepthought.imagine.image.Fingerprinter;
import com.sleepycat.je.*;
import com.sleepycat.persist.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 *  ImageMetaStore persists statistics about images to the database.
 */
public class ImageMetaStore {

    final static Logger LOGGER = Logger.getLogger(ImageMetaStore.class.getCanonicalName());

    final private Environment environment;
    final private EntityStore store;
    final private PrimaryIndex<String, ImageMeta> imageIndex;
    final private SecondaryIndex<String, String, ImageMeta> fingerprintIndex;

    public ImageMetaStore(final String filePath) throws DatabaseException {
        final EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true);

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
        System.out.println(file);
        LOGGER.info("Using " + file + " for data persistence.");

        this.environment = new Environment(file, envConfig);

        final StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(true);
        this.store = new EntityStore(this.environment, "stores", storeConfig);

        this.imageIndex = this.store.getPrimaryIndex(String.class, ImageMeta.class);
        this.fingerprintIndex = this.store.getSecondaryIndex(this.imageIndex, String.class, "fingerprintSmall");
    }

    public ImageMeta getById(final String id) throws DatabaseException {
        return this.imageIndex.get(id);
    }

    public ImageMeta getSimilar(final ImageMeta target, final int similarityTolerance) throws DatabaseException {
        final EntityCursor<ImageMeta> nearMatches =
            this.fingerprintIndex.subIndex(target.getFingerprintSmall()).entities();
        try {
            for(final ImageMeta nearMatch : nearMatches) {
                final int difference = Fingerprinter.difference(
                    target.getFingerprintLarge(),
                    nearMatch.getFingerprintLarge()
                );
                if(difference <= similarityTolerance) {
                    return nearMatch;
                }
            }
        }
        finally {
            nearMatches.close();
        }
        return null;
    }

    public void persist(final ImageMeta image) throws DatabaseException {
        this.imageIndex.put(image);
    }

}