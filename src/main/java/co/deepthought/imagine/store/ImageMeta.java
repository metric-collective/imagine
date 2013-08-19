package co.deepthought.imagine.store;

import com.sleepycat.je.Sequence;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import java.util.UUID;

@Entity
public class ImageMeta implements Comparable<ImageMeta> {

    @PrimaryKey
    private String id;
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String fingerprintSmall;
    private String fingerprintLarge;
    private Size size;

    private ImageMeta() {}

    public ImageMeta(final String fingerprintSmall, final String fingerprintLarge, final Size size) {
        this.id = UUID.randomUUID().toString();
        this.fingerprintSmall = fingerprintSmall;
        this.fingerprintLarge = fingerprintLarge;
        this.size = size;
    }

    public String getId() {
        return this.id;
    }

    public String getFingerprintSmall() {
        return this.fingerprintSmall;
    }

    public String getFingerprintLarge() {
        return this.fingerprintLarge;
    }

    public Size getSize() {
        return this.size;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public int compareTo(final ImageMeta other) {
        final int thisArea = this.size.getHeight() * this.size.getWidth();
        final int otherArea = other.size.getHeight() * other.size.getWidth();
        return Integer.compare(thisArea, otherArea);
    }
}