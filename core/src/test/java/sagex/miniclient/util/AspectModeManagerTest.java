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
        RectangleF r = mgr.doMeasure(vi, uiCopy);
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
}