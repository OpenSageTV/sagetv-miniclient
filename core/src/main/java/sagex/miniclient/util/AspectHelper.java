package sagex.miniclient.util;

import sagex.miniclient.uibridge.RectangleF;

/**
 * Created by seans on 16/12/16.
 */

public class AspectHelper {
    public static final RectangleF RectF_4_3 = new RectangleF(0,0,4,3);
    public static final RectangleF RectF_16_9 = new RectangleF(0,0,16,9);

    public static final float ar_4_3=4f/3f;
    public static final float ar_16_9=16f/9f;
    public static final float ar_tolerance=0.075f;

    public static boolean is_4_3(float ar) {
        return Math.abs(ar - ar_4_3) < ar_tolerance;
    }

    public static boolean is_16_9(float ar) {
        return Math.abs(ar - ar_16_9) < ar_tolerance;
    }

    public static float minScale(RectangleF src, RectangleF dest) {
        return Math.min( dest.width/src.width, dest.height/src.height);
    }

    public static float maxScale(RectangleF src, RectangleF dest) {
        return Math.max( dest.width/src.width, dest.height/src.height);
    }

    /**
     * Will fit src inside of dest ensuring that the result rectangle is exactly the same
     * height or width, scaling accordingly
     *
     * @param src
     * @param dest
     * @return
     */
    public static RectangleF fitInside(RectangleF src, RectangleF dest) {
        return scaleAndCenterRect(src, dest, minScale(src, dest));
    }

    /**
     * Will create a rect whereby src completly covers dest
     *
     * @param src
     * @param dest
     * @return
     */
    public static RectangleF fitOutside(RectangleF src, RectangleF dest) {
        return scaleAndCenterRect(src, dest, maxScale(src, dest));
    }


    /**
     * Fits src inside of dest such that src width is exactly the same as dest.  The resulting
     * rectangle's height will likely be larger than the dest height.
     * @param src
     * @param dest
     * @return
     */
    public static RectangleF fitWidth(RectangleF src, RectangleF dest) {
        return scaleAndCenterRect(src, dest, dest.width/src.width);
    }

    /**
     * Fits src inside of dest such that src height is exactly the same as dest.  The resulting
     * rectangle's width will likely be larger than the dest height.
     * @param src
     * @param dest
     * @return
     */
    public static RectangleF fitHeight(RectangleF src, RectangleF dest) {
        return scaleAndCenterRect(src, dest, dest.height/src.height);
    }


    /**
     * Scales the src rectangle by the scale, and then centers it in the destination rectangle.
     * @param src
     * @param dest
     * @param scale
     * @return
     */
    public static RectangleF scaleAndCenterRect(RectangleF src, RectangleF dest, float scale) {
        return src.scaleImmutable(scale).center(dest);
    }

    /**
     * Given the AR return a Rectangle over the video as it should be stretched.
     *
     * @param src
     * @param ar
     * @return
     */
    public static RectangleF stretch(RectangleF src, float ar) {
        if (is_16_9(ar)) {
            // assumption is that if you are stretching 16/9 then the content must be 4/3
            return fitHeight(RectF_4_3, src);
        } else if (is_4_3(ar)) {
            // assumption is that if you stretching 4/3 content then there is nothing to do
            // it will be stretched during the translation phase
            // we don't ever want to return the actual src, but a copy, because it might be changed
            return src.copy();
        }
        // we don't ever want to return the actual src, but a copy, because it might be changed
        return src.copy();
    }

    /**
     * Given the AR, return a rectangle over the video as it should be zoomed
     *
     * @param src
     * @param ar
     * @return
     */
    public static RectangleF zoom(RectangleF src, float ar) {
        if (is_16_9(ar)) {
            // assumption is that if you are zooming 16/9 then the content must be 16/9 letterbox
            // need to create 4/3 box, and inside that get a 16/9 box
            RectangleF r43 = fitInside(RectF_4_3, src);
            RectangleF r169 = fitInside(RectF_16_9, r43);
            r169.height=r169.width*9/16;
            return r169.center(src).scale(src).center(src);
        } else if (is_4_3(ar)) {
            // assumption is that if you stretching 4/3 content then content is 16/9 inside 4/3 rect
            RectangleF r169 = fitInside(RectF_16_9, src);
            r169.height = r169.width * 9 / 16;
            return r169.center(src).scale(src).center(src);
        }
        // default just stretch it
        return stretch(src, ar);
    }

}
