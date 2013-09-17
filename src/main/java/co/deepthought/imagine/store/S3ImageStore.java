package co.deepthought.imagine.store;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class S3ImageStore implements ImageStore {

    final AmazonS3Client client;
    final String bucket;
    final String prefix;

    public S3ImageStore(final String bucket, final String prefix, final String key, final String secret) {
        this.client = new AmazonS3Client(new BasicAWSCredentials(key, secret));
        this.bucket = bucket;
        this.prefix = prefix;
    }

    @Override
    public BufferedImage readImage(final String imagePath) {
        final String keyName = this.prefix + imagePath;
        final S3Object obj = this.client.getObject(new GetObjectRequest(this.bucket, keyName));
        try (final InputStream is = obj.getObjectContent()){
            return ImageIO.read(is);
        } catch (IOException e) {
            return null;
        } catch (AmazonS3Exception e) {
            return null;
        }
    }

    @Override
    public boolean saveImage(final ImageMeta imageMeta, final InputStream image) {
        final ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("image/jpeg");
        final String keyName = this.prefix + imageMeta.getId();
        this.client.putObject(new PutObjectRequest(this.bucket, keyName, image, meta));
        return true;
    }
}