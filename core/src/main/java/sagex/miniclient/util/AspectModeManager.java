package sagex.miniclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Rectangle;
import sagex.miniclient.uibridge.RectangleF;

/**
 * Created by seans on 11/12/16.
 */

public class AspectModeManager {
    private static final Logger log = LoggerFactory.getLogger(AspectModeManager.class);

    public static String DEFAULT_ASPECT_MODE = "Source";
    public static String ASPECT_MODES="Source;Stretch;Zoom";

    public RectangleF doMeasure(VideoInfo info, Dimension uiSize) {
        return doMeasure(info, new RectangleF(0,0,uiSize.width,uiSize.height));
    }

    public RectangleF doMeasure(VideoInfo info, Rectangle uiSize) {
        return doMeasure(info, uiSize.asFloatRect());
    }

    public RectangleF doMeasure(VideoInfo info, RectangleF uiSize) {
        if (info.size.width <= 0) return uiSize.copy();

        RectangleF vid = null;
        if ("Stretch".equalsIgnoreCase(info.aspectMode)) {
            vid =  doMeasureStretch(info, uiSize);
        } else if ("Zoom".equalsIgnoreCase(info.aspectMode)) {
            vid = doMeasureZoom(info, uiSize);
        } else {
            // source
            vid =  doMeasureSource(info, uiSize);
        }
        log.debug("doMeasure(): final: {}, screen: {}, vidInfo: {}", vid, uiSize, info);
        return vid;
    }

    private RectangleF doMeasureZoom(VideoInfo videoInfo, RectangleF screen) {
        RectangleF destRect = AspectHelper.zoom(videoInfo.size, videoInfo.aspectRatio);
        return destRect.translate(videoInfo.size, screen);
    }

    private RectangleF doMeasureStretch(VideoInfo videoInfo, RectangleF uiSize) {
        RectangleF destRect = AspectHelper.stretch(videoInfo.size, videoInfo.aspectRatio);
        return destRect.translate(videoInfo.size, uiSize);
    }

    /**
     * Provides AR that "fits" within the viewing window, preserving the video AR,
     * such that nothing is cropped and black bars may be added to the top/side if needed.
     *
     * @param videoInfo
     * @param screen
     * @return
     */
    private RectangleF doMeasureSource(VideoInfo videoInfo, RectangleF screen) {
        return AspectHelper.fitInside(videoInfo.size, screen);
    }
}
