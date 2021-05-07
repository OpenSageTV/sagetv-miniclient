package sagex.miniclient.uibridge;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;

import sagex.miniclient.MenuHint;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.MiniPlayerPlugin;

public interface UIRenderer<Image extends Texture> {
    /**
     * Determining States
     * START,CLEAR,FLIP == VIDEO ONLY
     * START,FLIP (no clear) == MENU
     * START,CLEAR,DRAW,FLIP == VIDEO TIMEBAR
     *
     * if if there is a CLEAR we are in a video
     */

    /**
     * SageTV is in a Menu or Dialog waiting for input, or we are in a video.
     */
    static int STATE_MENU = 1;
    static int STATE_VIDEO = 2;

    int getState();

    void GFXCMD_INIT();

    void GFXCMD_DEINIT();

    void close();

    void refresh();

    void hideCursor();

    void showBusyCursor();

    void drawRect(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL);

    void fillRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL);

    void clearRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL);

    void drawOval(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL, int clipX,
                  int clipY, int clipW, int clipH);

    void fillOval(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY,
                  int clipW, int clipH);

    void drawRoundRect(int x, int y, int width, int height, int thickness, int arcRadius, int argbTL, int argbTR, int argbBR,
                       int argbBL, int clipX, int clipY, int clipW, int clipH);

    void fillRoundRect(int x, int y, int width, int height, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL,
                       int clipX, int clipY, int clipW, int clipH);

    void drawTexture(int x, int y, int width, int height, int handle, ImageHolder<Image> img, int srcx, int srcy, int srcwidth, int srcheight, int blend);

    void drawLine(int x1, int y1, int x2, int y2, int argb1, int argb2);

    ImageHolder<Image> loadImage(int width, int height);

    void unloadImage(int handle, ImageHolder<Image> bi);

    ImageHolder<Image> createSurface(int handle, int width, int height);

    ImageHolder<Image> readImage(File cachedFile) throws Exception;

    ImageHolder<Image> readImage(InputStream bais) throws Exception;

    ImageHolder<Image> newImage(int destWidth, int destHeight);

    void registerTexture(ImageHolder<Image> texture);

    void setTargetSurface(int handle, ImageHolder<Image> image);

    void flipBuffer();

    void startFrame();

    void loadImageLine(int handle, ImageHolder<Image> image, int line, int len2, byte[] cmddata);

    void xfmImage(int srcHandle, ImageHolder<Image> srcImg, int destHandle, ImageHolder<Image> destImg, int destWidth, int destHeight, int maskCornerArc);

    boolean hasGraphicsCanvas();

    /**
     * Total Available Screen Size
     *
     * @return
     */
    Dimension getMaxScreenSize();

    /**
     * Size at which we are rendering, almost always is getMaxScreenSize() but can be smaller if we are windowed.
     * @return
     */
    Dimension getScreenSize();

    /**
     * Size we report to SageTV
     *
     * @return
     */
    Dimension getUISize();

    void setFullScreen(boolean b);

    void setSize(int w, int h);

    void invokeLater(Runnable runnable);

    Scale getScale();

    // video playback; NOT USED currently in Android
    boolean createVideo(int width, int height, int format);
    boolean updateVideo(int frametype, ByteBuffer buf);

    MiniPlayerPlugin newPlayerPlugin(MiniClientConnection connection, String urlString);

    void setVideoBounds(Rectangle o, Rectangle o1);

    void onMenuHint(MenuHint hint);

    /**
     * Returns true if the first frame has been rendered.
     *
     * @return
     */
    boolean isFirstFrameRendered();

    /**
     * Will be the name of an Advanced Apect Ratio Mode, like, 'Source', 'Fill', etc.  Client must
     * have sent the list of valid aspects in the client VIDEO_ADVANCED_ASPECT_LIST property
     * @param value
     */
    void setVideoAdvancedAspect(String value);

    void setUIAspectRatio(float value);

    float getUIAspectRatio();
}
