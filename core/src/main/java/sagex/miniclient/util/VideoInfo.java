package sagex.miniclient.util;

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
        this.size.update(size.x, size.y, w, h);
        if (ar<=0 && w>0 && h>0) {
            ar = (float)w/(float)h;
            System.out.println(String.format("** UPDATE AR BASED ON VIDEO SIZE: %sx%s WITH AR: %s", w, h, ar ));
        }
        this.aspectRatio=ar;
        return this;
    }

    public void reset() {
        this.aspectMode=AspectModeManager.DEFAULT_ASPECT_MODE;
        this.aspectRatio=0f;
        this.size.update(0,0,0,0);
        this.destRect.update(0,0,0,0);
    }
}
