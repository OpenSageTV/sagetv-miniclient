package sagex.miniclient.uibridge;

import org.junit.Test;

import sagex.miniclient.util.AspectHelper;

import static org.junit.Assert.*;

/**
 * Created by seans on 16/12/16.
 */
public class RectangleFTest {
    @Test
    public void testTranslate() throws Exception {
        RectangleF r = new RectangleF(10,10,90,90);
        Rectangle test = r.translateImmutable(new RectangleF(0,0,100,100), new RectangleF(0,0,10,10)).asIntRect();
        assertEquals(1, test.x);
        assertEquals(1, test.y);
        assertEquals(9, test.width);
        assertEquals(9, test.height);
    }

    @Test
    public void testCenter() throws Exception {
        RectangleF r = new RectangleF(0,0,1920,1080);
        RectangleF sub = new RectangleF(0,0,1000,1020);
        Rectangle test = sub.centerImmutable(r).asIntRect();
        assertEquals(test.x, 460);
        assertEquals(test.y, 30);
        assertEquals(test.width, 1000);
        assertEquals(test.height, 1020);
    }

    @Test
    public void testScaleRect() throws Exception {
        RectangleF r = new RectangleF(0,0,1920,1080);
        RectangleF sub = new RectangleF(240,135,1440,810);
        Rectangle test = sub.scaleImmutable(r).asIntRect();
        assertEquals(2560, test.width);
        assertEquals(1440, test.height);
        test = test.asFloatRect().center(r).asIntRect();
        assertEquals(-320, test.x);
        assertEquals(-180, test.y);
    }

    @Test
    public void testScale() throws Exception {
        Rectangle r = new RectangleF(0,0,1920,1080).scale(2).asIntRect();
        assertEquals(1920*2, r.width);
        assertEquals(1080*2, r.height);
    }

    @Test
    public void testEquals() {
        assertTrue(AspectHelper.RectF_16_9.equals(AspectHelper.RectF_16_9.copy()));
        assertFalse(AspectHelper.RectF_16_9.equals(AspectHelper.RectF_4_3));
    }

}