package sagex.miniclient.video;

import sagex.miniclient.uibridge.RectangleF;
import sagex.miniclient.util.VideoInfo;

/**
 * Created by seans on 19/12/16.
 */

public class VideoInfoResponse {
    public VideoInfo videoInfo;
    public float uiAspectRatio;
    // size of our screen native size
    public RectangleF uiScreenSizePixels;
    public String uri;
    public long mediaTime;
    public int state;
    public boolean pushMode;
    // size of sagetv surface
    public RectangleF renderScreenSize;
}
