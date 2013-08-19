package co.deepthought.imagine.store;

import com.sleepycat.persist.model.Persistent;

@Persistent
public class Size {

    private int height;
    private int width;

    private Size() {}

    public Size(final String dimensions) {
        final String[] split = dimensions.split("x");
        this.width = Integer.parseInt(split[0]);
        this.height = Integer.parseInt(split[1]);
    }

    public Size(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public boolean equals(final Object other) {
        if(!(other instanceof Size)) {
            return false;
        }
        else {
            final Size otherSize = (Size) other;
            return this.width == otherSize.width && this.height == otherSize.height;
        }
    }

    public String toString() {
        return this.width + "x" + this.height;
    }

}
