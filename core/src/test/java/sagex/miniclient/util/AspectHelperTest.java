package sagex.miniclient.util;

import org.junit.Test;

import sagex.miniclient.uibridge.Rectangle;
import sagex.miniclient.uibridge.RectangleF;

import static org.junit.Assert.*;

/**
 * Created by seans on 16/12/16.
 */
public class AspectHelperTest {
    @Test
    public void test4_3FitInside16_9() throws Exception {
        RectangleF r169 = new RectangleF(0,0,720 * AspectHelper.ar_16_9, 720);
        RectangleF r43 = AspectHelper.fitInside(AspectHelper.RectF_4_3, r169);
        System.out.println(r169);
        System.out.println(r43);

        // 4/3 inside of 16/9 (same height)
        // should be 160,0,960,720
        Rectangle r = r43.asIntRect();
        assertEquals(160, r.x);
        assertEquals(0, r.y);
        assertEquals(960, r.width);
        assertEquals(720, r.height);
        assertEquals(r.height, (int)r169.height);
    }

    @Test
    public void test16_9FitInside4_3() throws Exception {
        RectangleF r43 = new RectangleF(0,0,480 * AspectHelper.ar_4_3, 480);
        RectangleF r169 = AspectHelper.fitInside(AspectHelper.RectF_16_9, r43);
        System.out.println(r43);
        System.out.println(r169);

        // 640x480
        // 16/9 inside 4/3 (same width)
        // should be 0,60,160,640
        Rectangle r = r169.asIntRect();
        assertEquals(0, r.x);
        assertEquals(60, r.y);
        assertEquals(640, r.width);
        assertEquals(360, r.height);
        assertEquals(r.width, (int)r169.width);
    }

    @Test
    public void test4_3FitInside16_9_SameWidth() throws Exception {
        RectangleF r169 = new RectangleF(0,0,720 * AspectHelper.ar_16_9, 720);
        RectangleF r43 = AspectHelper.fitWidth(AspectHelper.RectF_4_3, r169);
        System.out.println(r169);
        System.out.println(r43);

        // 4/3 inside of 16/9 (same width)
        // should be 0,-120,1280,960
        Rectangle r = r43.asIntRect();
        assertEquals(0, r.x);
        assertEquals(-120, r.y);
        assertEquals(1280, r.width);
        assertEquals(960, r.height);
        assertEquals(r.width, (int)r169.width);
        assertEquals(r43.getAR(), AspectHelper.ar_4_3, AspectHelper.ar_tolerance);
    }

    @Test
    public void test16_9FitInside4_3_SameHeight() throws Exception {
        RectangleF r43 = new RectangleF(0,0,480 * AspectHelper.ar_4_3, 480);
        RectangleF r169 = AspectHelper.fitHeight(AspectHelper.RectF_16_9, r43);
        System.out.println(r43);
        System.out.println(r169);

        // 640x480
        // 16/9 inside 4/3
        // should be 0,-107,853,480
        Rectangle r = r169.asIntRect();
        assertEquals(-107, r.x);
        assertEquals(0, r.y);
        assertEquals(853, r.width);
        assertEquals(480, r.height);
        assertEquals(r.height, (int)r169.height);
    }


    @Test
    public void testZoom169() throws Exception {
        // zooming 16x9 assumes that the content is 16x9 letterbox, so we need a smaller
        // 16x9 rectangle inside the larger one.
        // this is done by picking a 4/3 rectangle, and then finding the 16x9 rectangle
        // and then scaling that rectangle around the original 16x9 rectangle
        // the result is a 16x9 super rectangle scaled so that the middle 16x9 rectangle
        // matches the video size you want
        RectangleF r169 = new RectangleF(0,0,1920,1080);
        Rectangle test = AspectHelper.zoom(r169, AspectHelper.ar_16_9).asIntRect();
        assertEquals(-320, test.x);
        assertEquals(-180, test.y);
        assertEquals(2560, test.width);
        assertEquals(1440, test.height);

        // prove that doing the same thing with smaller rectangle can then be translated to other coordinate system
        r169 = new RectangleF(0,0,1280,720);
        RectangleF rtrans = AspectHelper.zoom(r169, AspectHelper.ar_16_9).translateImmutable(r169, new RectangleF(0,0,1920,1080));
        System.out.println(rtrans);
        test= rtrans.asIntRect();
        assertEquals(-320, test.x);
        assertEquals(-180, test.y);
        assertEquals(2560, test.width);
        assertEquals(1440, test.height);
    }

    @Test
    public void testZoom43() throws Exception {
        // zooming a 4x3 assumes that we have a 16x9 video in a 4/3 window, so need to find the
        // 16x9 sub rectangle and then super scale it aroud the original video window to create
        // a rectangle that when applied to the original 16x9 window
        RectangleF r43 = new RectangleF(0,0,640,480);
        Rectangle test = AspectHelper.zoom(r43, AspectHelper.ar_4_3).asIntRect();
        System.out.println(test);
        assertEquals(0, test.x);
        assertEquals(-80, test.y);
        assertEquals(640, test.width);
        assertEquals(640, test.height);

        // prove that scaling this to the 1080 video is a 16x9 video
        RectangleF rtrans = test.asFloatRect().translateImmutable(test.asFloatRect().position(0,0), new RectangleF(0,0,1920,1080));
        System.out.println(rtrans);
        test= rtrans.asIntRect();
        assertEquals(0, test.x);
        assertEquals(-135, test.y);
        assertEquals(1920, test.width);
        assertEquals(1080, test.height);
    }


    @Test
    public void testStretch4_3() {
        RectangleF r43 = new RectangleF(0,0,640,480);
        RectangleF stretch43 = AspectHelper.stretch(r43, r43.getAR());
        Rectangle trans = stretch43.translateImmutable(r43, new RectangleF(0,0,1920,1080)).asIntRect();
        System.out.println(trans);
        assertEquals(0, trans.x);
        assertEquals(0, trans.y);
        assertEquals(1920, trans.width);
        assertEquals(1080, trans.height);
    }

    @Test
    public void testStretch16_9() {
        RectangleF r169 = new RectangleF(0,0,1280,720);
        RectangleF stretch169 = AspectHelper.stretch(r169, r169.getAR());
        Rectangle trans = stretch169.translateImmutable(r169, new RectangleF(0,0,1920,1080)).asIntRect();
        System.out.println(trans);
        assertEquals(-320, trans.x);
        assertEquals(0, trans.y);
        assertEquals(2560, trans.width);
        assertEquals(1080, trans.height);
    }


    @Test
    public void testIsAR() {
        assertTrue(AspectHelper.is_4_3(AspectHelper.ar_4_3));
        assertFalse(AspectHelper.is_4_3(AspectHelper.ar_16_9));
        assertTrue(AspectHelper.is_16_9(AspectHelper.ar_16_9));
        assertFalse(AspectHelper.is_16_9(AspectHelper.ar_4_3));
    }
}