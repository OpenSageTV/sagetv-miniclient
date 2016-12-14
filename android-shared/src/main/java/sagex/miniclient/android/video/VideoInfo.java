package sagex.miniclient.android.video;

import sagex.miniclient.uibridge.Rectangle;

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
    public Rectangle size = new Rectangle(0,0,0,0);
    public Rectangle destRect = new Rectangle(0,0,0,0);

    public VideoInfo update(int w, int h, int sarNum, int sarDen) {
        return this.update(w,h,(float)sarNum/(float)sarDen);
    }

    public VideoInfo update(int w, int h, float ar) {
        this.size.update(size.x, size.y, w, h);
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
