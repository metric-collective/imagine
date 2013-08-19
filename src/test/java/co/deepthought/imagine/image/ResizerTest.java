package co.deepthought.imagine.image;

import co.deepthought.imagine.store.Size;
import junit.framework.Assert;
import org.junit.Test;

public class ResizerTest {

    @Test
     public void testCanvasSizeCropBoth() {
        final Resizer options = new Resizer(
            Resizer.Mode.CROP,
            Resizer.Scale.BOTH,
            new Size(800, 600),
            null,
            0f
        );
        // * guarantees output size
        this.assertCanvasMatch(options, 800, 600, 800, 600);
        this.assertCanvasMatch(options, 800, 600, 1600, 1200);
        this.assertCanvasMatch(options, 800, 600, 3200, 1200);
        this.assertCanvasMatch(options, 800, 600, 1600, 2400);

        this.assertCanvasMatch(options, 800, 600, 400, 1200);
        this.assertCanvasMatch(options, 800, 600, 1600, 300);

        this.assertCanvasMatch(options, 800, 600, 400, 300);
        this.assertCanvasMatch(options, 800, 600, 400, 150);
        this.assertCanvasMatch(options, 800, 600, 200, 300);
    }

    @Test
    public void testCanvasSizeCropDown() {
        final Resizer options = new Resizer(
            Resizer.Mode.CROP,
            Resizer.Scale.DOWN,
            new Size(800, 600),
            null,
            0f
        );
        this.assertCanvasMatch(options, 800, 600, 800, 600);
        this.assertCanvasMatch(options, 800, 600, 1600, 1200);
        this.assertCanvasMatch(options, 800, 600, 3200, 1200);
        this.assertCanvasMatch(options, 800, 600, 1600, 2400);

        this.assertCanvasMatch(options, 400, 600, 400, 1200);
        this.assertCanvasMatch(options, 800, 300, 1600, 300);

        this.assertCanvasMatch(options, 400, 300, 400, 300);
        this.assertCanvasMatch(options, 400, 150, 400, 150);
        this.assertCanvasMatch(options, 200, 300, 200, 300);
    }

    @Test
    public void testCanvasSizeMaxBoth() {
        final Resizer options = new Resizer(
            Resizer.Mode.MAX,
            Resizer.Scale.BOTH,
            new Size(800, 600),
            null,
            0f
        );
        this.assertCanvasMatch(options, 800, 600, 800, 600);
        this.assertCanvasMatch(options, 800, 600, 1600, 1200);
        this.assertCanvasMatch(options, 800, 300, 3200, 1200);
        this.assertCanvasMatch(options, 400, 600, 1600, 2400);

        this.assertCanvasMatch(options, 200, 600, 400, 1200);
        this.assertCanvasMatch(options, 800, 150, 1600, 300);

        this.assertCanvasMatch(options, 800, 600, 400, 300);
        this.assertCanvasMatch(options, 800, 300, 400, 150);
        this.assertCanvasMatch(options, 400, 600, 200, 300);
    }

    @Test
    public void testCanvasSizePadBoth() {
        final Resizer options = new Resizer(
            Resizer.Mode.PAD,
            Resizer.Scale.BOTH,
            new Size(800, 600),
            null,
            0f
        );
        // * guarantees output size
        this.assertCanvasMatch(options, 800, 600, 800, 600);
        this.assertCanvasMatch(options, 800, 600, 1600, 1200);
        this.assertCanvasMatch(options, 800, 600, 3200, 1200);
        this.assertCanvasMatch(options, 800, 600, 1600, 2400);

        this.assertCanvasMatch(options, 800, 600, 400, 1200);
        this.assertCanvasMatch(options, 800, 600, 1600, 300);

        this.assertCanvasMatch(options, 800, 600, 400, 300);
        this.assertCanvasMatch(options, 800, 600, 400, 150);
        this.assertCanvasMatch(options, 800, 600, 200, 300);
    }

    @Test
    public void testCanvasSizePadDown() {
        final Resizer options = new Resizer(
            Resizer.Mode.PAD,
            Resizer.Scale.DOWN,
            new Size(800, 600),
            null,
            0f
        );
        // Not 100% clear on whether this is desired functionality
        this.assertCanvasMatch(options, 800, 600, 800, 600);
        this.assertCanvasMatch(options, 800, 600, 1600, 1200);
        this.assertCanvasMatch(options, 800, 600, 3200, 1200);
        this.assertCanvasMatch(options, 800, 600, 1600, 2400);

        this.assertCanvasMatch(options, 800, 600, 400, 1200);
        this.assertCanvasMatch(options, 800, 600, 1600, 300);

        this.assertCanvasMatch(options, 800, 600, 400, 300);
        this.assertCanvasMatch(options, 800, 600, 400, 150);
        this.assertCanvasMatch(options, 800, 600, 200, 300);
    }

    @Test
    public void testCanvasSizeStretchBoth() {
        final Resizer options = new Resizer(
            Resizer.Mode.STRETCH,
            Resizer.Scale.BOTH,
            new Size(800, 600),
            null,
            0f
        );
        // * guarantees output size
        this.assertCanvasMatch(options, 800, 600, 800, 600);
        this.assertCanvasMatch(options, 800, 600, 1600, 1200);
        this.assertCanvasMatch(options, 800, 600, 3200, 1200);
        this.assertCanvasMatch(options, 800, 600, 1600, 2400);

        this.assertCanvasMatch(options, 800, 600, 400, 1200);
        this.assertCanvasMatch(options, 800, 600, 1600, 300);

        this.assertCanvasMatch(options, 800, 600, 400, 300);
        this.assertCanvasMatch(options, 800, 600, 400, 150);
        this.assertCanvasMatch(options, 800, 600, 200, 300);
    }

    @Test
    public void testCanvasSizeMaxCanvas() {
        // * guarantees output size
        // this covers all options with canvas, really
        // but maybe we should be more rigorous
        final Resizer options = new Resizer(
            Resizer.Mode.MAX,
            Resizer.Scale.CANVAS,
            new Size(800, 600),
            null,
            0f
        );
        this.assertCanvasMatch(options, 800, 600, 800, 600);
        this.assertCanvasMatch(options, 800, 600, 1600, 1200);
        this.assertCanvasMatch(options, 800, 600, 400, 1200);
        this.assertCanvasMatch(options, 800, 600, 400, 150);
        this.assertCanvasMatch(options, 800, 600, 200, 300);
    }

    @Test
    public void testImageSizeCropBoth() {
        final Resizer options = new Resizer(
            Resizer.Mode.CROP,
            Resizer.Scale.BOTH,
            new Size(800, 600),
            null,
            0f
        );
        this.assertSizeMatch(options, 800, 600, 800, 600);
        this.assertSizeMatch(options, 800, 600, 1600, 1200);
        this.assertSizeMatch(options, 1600, 600, 3200, 1200);
        this.assertSizeMatch(options, 800, 1200, 1600, 2400);

        this.assertSizeMatch(options, 800, 2400, 400, 1200);
        this.assertSizeMatch(options, 3200, 600, 1600, 300);

        this.assertSizeMatch(options, 800, 600, 400, 300);
        this.assertSizeMatch(options, 1600, 600, 400, 150);
        this.assertSizeMatch(options, 800, 1200, 200, 300);
    }

    @Test
    public void testImageSizeCropDown() {
        final Resizer options = new Resizer(
            Resizer.Mode.CROP,
            Resizer.Scale.DOWN,
            new Size(800, 600),
            null,
            0f
        );
        this.assertSizeMatch(options, 800, 600, 800, 600);
        this.assertSizeMatch(options, 800, 600, 1600, 1200);
        this.assertSizeMatch(options, 1600, 600, 3200, 1200);
        this.assertSizeMatch(options, 800, 1200, 1600, 2400);

        this.assertSizeMatch(options, 400, 1200, 400, 1200);
        this.assertSizeMatch(options, 1600, 300, 1600, 300);

        this.assertSizeMatch(options, 400, 300, 400, 300);
        this.assertSizeMatch(options, 400, 150, 400, 150);
        this.assertSizeMatch(options, 200, 300, 200, 300);
    }

    @Test
    public void testImageSizeMaxBoth() {
        final Resizer options = new Resizer(
            Resizer.Mode.MAX,
            Resizer.Scale.BOTH,
            new Size(800, 600),
            null,
            0f
        );
        this.assertSizeMatch(options, 800, 600, 800, 600);
        this.assertSizeMatch(options, 800, 600, 1600, 1200);
        this.assertSizeMatch(options, 800, 300, 3200, 1200);
        this.assertSizeMatch(options, 400, 600, 1600, 2400);

        this.assertSizeMatch(options, 200, 600, 400, 1200);
        this.assertSizeMatch(options, 800, 150, 1600, 300);

        this.assertSizeMatch(options, 800, 600, 400, 300);
        this.assertSizeMatch(options, 800, 300, 400, 150);
        this.assertSizeMatch(options, 400, 600, 200, 300);
    }

    @Test
    public void testImageSizeMaxDown() {
        final Resizer options = new Resizer(
            Resizer.Mode.MAX,
            Resizer.Scale.DOWN,
            new Size(800, 600),
            null,
            0f
        );
        this.assertSizeMatch(options, 800, 600, 800, 600);
        this.assertSizeMatch(options, 800, 600, 1600, 1200);
        this.assertSizeMatch(options, 800, 300, 3200, 1200);
        this.assertSizeMatch(options, 400, 600, 1600, 2400);

        this.assertSizeMatch(options, 200, 600, 400, 1200);
        this.assertSizeMatch(options, 800, 150, 1600, 300);

        this.assertSizeMatch(options, 400, 300, 400, 300);
        this.assertSizeMatch(options, 400, 150, 400, 150);
        this.assertSizeMatch(options, 200, 300, 200, 300);
    }

    @Test
    public void testImageSizeStretchBoth() {
        final Resizer options = new Resizer(
            Resizer.Mode.STRETCH,
            Resizer.Scale.BOTH,
            new Size(800, 600),
            null,
            0f
        );
        this.assertSizeMatch(options, 800, 600, 800, 600);
        this.assertSizeMatch(options, 800, 600, 1600, 1200);
        this.assertSizeMatch(options, 800, 600, 3200, 1200);
        this.assertSizeMatch(options, 800, 600, 1600, 2400);

        this.assertSizeMatch(options, 800, 600, 400, 1200);
        this.assertSizeMatch(options, 800, 600, 1600, 300);

        this.assertSizeMatch(options, 800, 600, 400, 300);
        this.assertSizeMatch(options, 800, 600, 400, 150);
        this.assertSizeMatch(options, 800, 600, 200, 300);
    }

    @Test
    public void testImageSizeStretchDown() {
        // who would ever do this combo hahaha
        final Resizer options = new Resizer(
            Resizer.Mode.STRETCH,
            Resizer.Scale.DOWN,
            new Size(800, 600),
            null,
            0f
        );
        this.assertSizeMatch(options, 800, 600, 800, 600);
        this.assertSizeMatch(options, 800, 600, 1600, 1200);
        this.assertSizeMatch(options, 800, 600, 3200, 1200);
        this.assertSizeMatch(options, 800, 600, 1600, 2400);

        this.assertSizeMatch(options, 400, 600, 400, 1200);
        this.assertSizeMatch(options, 800, 300, 1600, 300);

        this.assertSizeMatch(options, 400, 300, 400, 300);
        this.assertSizeMatch(options, 400, 150, 400, 150);
        this.assertSizeMatch(options, 200, 300, 200, 300);
    }

    private void assertCanvasMatch(
            final Resizer options,
            final int expectedWidth,
            final int expectedHeight,
            final int sourceWidth,
            final int sourceHeight) {
        Assert.assertEquals(
            new Size(expectedWidth, expectedHeight),
            options.getCanvasSize(new Size(sourceWidth, sourceHeight))
        );
    }

    private void assertSizeMatch(
            final Resizer options,
            final int expectedWidth,
            final int expectedHeight,
            final int sourceWidth,
            final int sourceHeight) {
        Assert.assertEquals(
            new Size(expectedWidth, expectedHeight),
            options.getImageSize(new Size(sourceWidth, sourceHeight))
        );
    }
}
