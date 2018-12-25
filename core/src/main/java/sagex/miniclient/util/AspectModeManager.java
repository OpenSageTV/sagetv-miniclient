package sagex.miniclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.uibridge.RectangleF;

/**
 * Created by seans on 11/12/16.
 */

public class AspectModeManager {
    private static final Logger log = LoggerFactory.getLogger(AspectModeManager.class);

    public static String DEFAULT_ASPECT_MODE = "Source";
    public static String ASPECT_MODES="Source;Stretch;Zoom";

//    public RectangleF doMeasure(VideoInfo info, Dimension uiSize, float uiAR) {
//        return doMeasure(info, new RectangleF(0,0,uiSize.width,uiSize.height), uiAR);
//    }

//    public RectangleF doMeasure(VideoInfo info, RectangleF uiSize) {
//        return doMeasure(info, uiSize, uiSize.getAR());
//    }

    public RectangleF doMeasure(VideoInfo info, RectangleF uiSize, float uiAR) {
        if (info.size.width <= 0) return uiSize.copy();

        RectangleF ui = uiSize.copy();

        // if actual window and ui AR is NOT the same, then we need to adjust for it
        if (!AspectHelper.is_ar_equals(uiAR, uiSize.getAR())) {
            ui.height = ui.width / uiAR;
        }

        RectangleF vid = null;
        if ("Stretch".equalsIgnoreCase(info.aspectMode)) {
            vid =  doMeasureStretch(info, ui);
        } else if ("Zoom".equalsIgnoreCase(info.aspectMode)) {
            vid = doMeasureZoom(info, ui);
        } else {
            // source
            vid =  doMeasureSource(info, ui);
        }

        RectangleF vidFinal = vid.copy();
        if (!AspectHelper.is_ar_equals(uiAR, uiSize.getAR())) {
            // readjust the video to render to our actual screen size
            vidFinal.translate(ui, uiSize);
        }

        // log.debug("doMeasure(): vid: {}, vidFinal: {}, projected screen: {}, actual screen: {}, vidInfo: {}", vid, vidFinal, ui, uiSize, info);
        return vidFinal;
    }

    RectangleF doMeasureZoom(VideoInfo videoInfo, RectangleF screen) {
        RectangleF destRect = AspectHelper.zoom(videoInfo.size, videoInfo.aspectRatio);
        return destRect.translate(videoInfo.size, screen);
    }

    RectangleF doMeasureStretch(VideoInfo videoInfo, RectangleF uiSize) {
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
    RectangleF doMeasureSource(VideoInfo videoInfo, RectangleF screen) {
        if (AspectHelper.is_ar_equals(videoInfo.size.getAR(), videoInfo.aspectRatio)) {
            // we have the same Aspect Ratio, so just "fit" it in the window
            return AspectHelper.fitInside(videoInfo.size, screen);
        } else {
            if (AspectHelper.is_16_9(videoInfo.aspectRatio)) {
                // video is 16/9, so make it fit inside the screen as a 16/9 window
                return AspectHelper.fitInside(AspectHelper.RectF_16_9.copy(), screen);
            } else {
                // we have a 4/3 see if the actual rect is less than 4/3
                if (videoInfo.size.getAR() < videoInfo.aspectRatio) {
                    // stretch the video to 4/3 if the actual video is not really 4/3
                    return AspectHelper.fitInside(videoInfo.size.copy().updateWidthUsingAspectRatio(AspectHelper.ar_4_3), screen);
                } else {
                    return AspectHelper.fitInside(videoInfo.size, screen);
                }
            }
        }
    }

    public String[] getARModes() {
        return ASPECT_MODES.split(";");
    }
}
