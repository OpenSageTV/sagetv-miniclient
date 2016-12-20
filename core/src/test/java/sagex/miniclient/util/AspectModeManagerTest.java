package sagex.miniclient.util;

import org.junit.Test;

import sagex.miniclient.uibridge.Rectangle;
import sagex.miniclient.uibridge.RectangleF;

import static org.junit.Assert.*;

/**
 * Created by seans on 18/12/16.
 */
public class AspectModeManagerTest {
    @Test
    public void testMeasureImmutable() {
        AspectModeManager mgr = new AspectModeManager();
        String[] arModes = mgr.getARModes();
        for (String mode: arModes) {
            testImmutables(mgr, mode, newVideoRect(), newUIRect(), AspectHelper.ar_4_3);
            testImmutables(mgr, mode, newVideoRect(), newUIRect(), AspectHelper.ar_16_9);
        }
    }

    private void testImmutables(AspectModeManager mgr, String mode, RectangleF vidRect, RectangleF uiRect, float ar) {
        System.out.println("Begin Testing " + mode + "; " + ar);
        VideoInfo vi = new VideoInfo();
        vi.update(vidRect.asIntRect().width, vidRect.asIntRect().height, ar);
        RectangleF uiCopy = uiRect.copy();
        RectangleF r = mgr.doMeasure(vi, uiCopy, AspectHelper.ar_16_9);
        verifyRect("VIDEO",vidRect, vi.size);
        verifyRect("UI",uiRect, uiCopy);
        System.out.println("PASSED " + mode + "; " + ar);
    }

    private void verifyRect(String type, RectangleF expectedRect, RectangleF actualRect) {
        Rectangle re = expectedRect.asIntRect();
        Rectangle ra = actualRect.asIntRect();
        assertEquals(type + ": x", re.x, ra.x);
        assertEquals(type + ": y", re.y, ra.y);
        assertEquals(type + ": width", re.width, ra.width);
        assertEquals(type + ": height", re.height, ra.height);
    }

    public RectangleF newVideoRect() {
        return new RectangleF(0,0,640,480);
    }

    public RectangleF newUIRect() {
        return new RectangleF(0,0,1920,1080);
    }

    @Test
    public void testMeasureSource16_9_in_16_9() {
        RectangleF ui = new RectangleF(0,0,1920, 1920 / AspectHelper.ar_16_9);
        RectangleF vid = new RectangleF(0,0,1280, 1280 / AspectHelper.ar_16_9);
        System.out.println("UI: " + ui);
        System.out.println("VID: " + vid);
        assertTrue(AspectHelper.is_16_9(ui.getAR()));
        assertTrue(AspectHelper.is_16_9(vid.getAR()));

        VideoInfo vi = new VideoInfo();
        vi.update(vid.asIntRect().width, vid.asIntRect().height, vid.getAR());
        vi.updateARMode("Source");

        AspectModeManager amm = new AspectModeManager();

        Rectangle destVid = amm.doMeasureSource(vi, ui).asIntRect();
        System.out.println("DESTVID: " + destVid);
        assertTrue("Destination and UI should be same size", destVid.equals(ui.asIntRect()));
    }

    @Test
    public void testMeasureSource16_9_in_wide_2_35() {
        RectangleF ui = new RectangleF(0,0,1920, 1920f / AspectHelper.ar_16_9);
        RectangleF vid = new RectangleF(0,0,1920, 1920f / AspectHelper.ar_16_9);
        System.out.println("UI: " + ui);
        System.out.println("VID: " + vid);
        assertTrue(AspectHelper.is_16_9(ui.getAR()));
        assertTrue(AspectHelper.is_16_9(vid.getAR()));

        VideoInfo vi = new VideoInfo();
        vi.update(vid.asIntRect().width, vid.asIntRect().height, vid.getAR());
        vi.updateARMode("Source");

        AspectModeManager amm = new AspectModeManager();

        // screen is 1920 but 2.4 screen AR...
        Rectangle destVid = amm.doMeasure(vi, ui, 2.4f).asIntRect();
        System.out.println("DESTVID: " + destVid);

        // since our src video was 1920 and the ui was 1920 but 2.4 AR... video
        // is actually much narrower
        assertEquals(new Rectangle(249, 0, 1422, 1080), destVid);
    }


    @Test
    public void testMeasureSource4_3_in_16_9() {
        RectangleF ui = new RectangleF(0,0,1920, 1920 / AspectHelper.ar_16_9);
        RectangleF vid = new RectangleF(0,0,640, 640 / AspectHelper.ar_4_3);
        System.out.println("UI: " + ui);
        System.out.println("VID: " + vid);
        assertTrue(AspectHelper.is_16_9(ui.getAR()));
        assertTrue(AspectHelper.is_4_3(vid.getAR()));

        VideoInfo vi = new VideoInfo();
        vi.update(vid.asIntRect().width, vid.asIntRect().height, vid.getAR());
        vi.updateARMode("Source");

        AspectModeManager amm = new AspectModeManager();

        Rectangle destVid = amm.doMeasureSource(vi, ui).asIntRect();
        System.out.println("DESTVID: " + destVid);

        // destination height should be the same
        assertEquals(ui.asIntRect().height, destVid.height);

        // sum of x * 2 + width should be ui width
        assertEquals(destVid.x * 2 + destVid.width, ui.asIntRect().width);
    }
}