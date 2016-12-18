package sagex.miniclient.util;

import sagex.miniclient.uibridge.Rectangle;
import sagex.miniclient.uibridge.RectangleF;

/**
 * Created by seans on 11/12/16.
 */

public class VideoInfo {
    @Override
    public String toString() {
        return "VideoInfo{" +
                "aspectMode='" + aspectMode + '\'' +
                ", aspectRatio=" + aspectRatio +
                ", size=" + size +
                '}';
    }

    public String aspectMode =AspectModeManager.DEFAULT_ASPECT_MODE;
    public float aspectRatio=0f;
    public RectangleF size = new RectangleF();
    public RectangleF destRect = new RectangleF();
    public boolean changed=false;

    public VideoInfo update(int w, int h, int sarNum, int sarDen) {
        System.out.println(String.format("** UPDATE VIDEO SIZE: %sx%s %s/%s", w, h, sarNum, sarDen ));
        if (sarNum>0 && sarDen>0 && h>0) {
            return this.update(w, h, (float) sarNum / (float) sarDen * (float)w / (float)h);
        } else {
            return this.update(w, h, 0);
        }
    }

    public VideoInfo update(int w, int h, float ar) {
        System.out.println(String.format("** UPDATE VIDEO SIZE: %sx%s WITH AR: %s", w, h, ar ));
        // check if video size changed
        if (w!=Math.round(size.width)  || h!= Math.round(size.height)) {
            changed=true;
            this.size.update(size.x, size.y, w, h);
        }

        // check if the video ar changed
        if ((int)(ar * 1000) != ((int)aspectRatio*1000)) {
            aspectRatio=ar;
            changed=true;
        }

        if (changed) {
            System.out.println(String.format("** UPDATE AR BASED ON VIDEO SIZE: %sx%s WITH AR: %s", w, h, ar));
        }
        return this;
    }

    public void updateARMode(String arMode) {
        if (this.aspectMode==null || !this.aspectMode.equals(arMode)) {
            this.aspectMode=arMode;
            this.changed=true;
        }
    }

    public void updateDestRect(RectangleF dest) {
        if (!dest.equals(destRect)) {
            this.changed=true;
            this.destRect.update(dest);
        }
    }

    public void reset() {
        this.aspectMode=AspectModeManager.DEFAULT_ASPECT_MODE;
        this.aspectRatio=0f;
        this.size.update(0,0,0,0);
        this.destRect.update(0,0,0,0);
        this.changed=false;
    }
}
