package sagex.miniclient.android.video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Rectangle;

/**
 * Created by seans on 11/12/16.
 */

public class AspectModeManager {
    private static final Logger log = LoggerFactory.getLogger(AspectModeManager.class);

    public static String DEFAULT_ASPECT_MODE = "Fit";
    public static String ASPECT_MODES="Fit;Stretch;Zoom;Cover";

    public Rectangle doMeasure(VideoInfo info, Dimension uiSize) {
        return doMeasure(info, new Rectangle(0,0,uiSize.width,uiSize.height));
    }

    public Rectangle doMeasure(VideoInfo info, Rectangle uiSize) {
        log.debug("doMeasure(): info: {}, uiSize:{}", info, uiSize);
        if ("Cover".equalsIgnoreCase(info.aspectMode)) {
            // Covers the screen area, stretching both horizontally and vertically to cover the
            // the UI area
            return uiSize.copy();
        } else if ("Zoom".equalsIgnoreCase(info.aspectMode)) {
            return doMeasureZoom(info, uiSize);
        } else {
            return doMeasureFit(info, uiSize);
        }
    }

    /**
     * Provides AR that "fits" within the viewing window, preserving the video AR,
     * such that nothing is cropped and black bars may be added to the top/side if needed.
     *
     * @param videoInfo
     * @param screen
     * @return
     */
    private Rectangle doMeasureFit(VideoInfo videoInfo, Rectangle screen) {
        // if we don't know, then use the uiSize
        if (videoInfo.size.width==0) return screen.copy();

        Rectangle destRect = new Rectangle(0,0,screen.width,screen.height);

        float scale = Math.min( (float)screen.width/(float)videoInfo.size.width, (float)screen.height/(float)videoInfo.size.height);
        destRect.width = (int)(videoInfo.size.width*scale);
        destRect.height = (int)(videoInfo.size.height*scale);
        destRect.x = (screen.width-destRect.width)/2;
        destRect.y = (screen.height-destRect.height)/2;

        return destRect;
    }

    /**
     * Provides an AR that will "zoom" the video so that no black bars are present,
     * preserving the video AR.  The video will likely be cropped horizontally or vertically.
     * @param videoInfo
     * @param screen
     * @return
     */
    private Rectangle doMeasureZoom(VideoInfo videoInfo, Rectangle screen) {
        // if we don't know, then use the uiSize
        if (videoInfo.size.width==0) return screen.copy();

        Rectangle destRect = new Rectangle(0,0,screen.width,screen.height);

        float scale = Math.max( (float)screen.width/(float)videoInfo.size.width, (float)screen.height/(float)videoInfo.size.height);
        destRect.width = (int)(videoInfo.size.width*scale);
        destRect.height = (int)(videoInfo.size.height*scale);
        destRect.x = (screen.width - destRect.width) / 2;
        destRect.y = (screen.height-destRect.height)/2;

        return destRect;
    }
}
