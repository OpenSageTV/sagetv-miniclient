package sagex.miniclient.uibridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import sagex.miniclient.MenuHint;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.MiniPlayerPlugin;

public class LoggingUIRenderer<Image> implements UIRenderer<Image> {
    private static final Logger log = LoggerFactory.getLogger(LoggingUIRenderer.class);

    private static final boolean LOGGING = false;
    Map<String, Boolean> logged = new HashMap<String, Boolean>();
    private UIRenderer<Image> delegate;

    public LoggingUIRenderer(UIRenderer<Image> delegate) {
        this.delegate = delegate;
    }

    void log(String s) {
        if (LOGGING)
            log.debug("UIManager: {}", s);
    }


    @Override
    public int getState() {
        log("getState()");
        return delegate.getState();
    }

    public void GFXCMD_INIT() {
        log("GFXCMD_INIT");
        delegate.GFXCMD_INIT();
    }

    public void GFXCMD_DEINIT() {
        log("GFXCMD_DEINIT");
        delegate.GFXCMD_DEINIT();
    }

    public void close() {
        log("close");
        delegate.close();
    }

    public void refresh() {
        log("refresh");
        delegate.refresh();
    }

    public void hideCursor() {
        log("hideCursor");
        delegate.hideCursor();
    }

    public void showBusyCursor() {
        log("showBusyCursor");
        delegate.showBusyCursor();
    }

    public void drawRect(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL) {
        log("drawRect");
        delegate.drawRect(x, y, width, height, thickness, argbTL, argbTR, argbBR, argbBL);
    }

    public void fillRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL) {
        log("fillRect");
        delegate.fillRect(x, y, width, height, argbTL, argbTR, argbBR, argbBL);
    }

    public void clearRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL) {
        log("clearRect");
        delegate.clearRect(x, y, width, height, argbTL, argbTR, argbBR, argbBL);
    }

    public void drawOval(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL,
                         int clipX, int clipY, int clipW, int clipH) {
        log("drawOval");
        delegate.drawOval(x, y, width, height, thickness, argbTL, argbTR, argbBR, argbBL, clipX, clipY, clipW, clipH);
    }

    public void fillOval(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY,
                         int clipW, int clipH) {
        log("fillOval");
        delegate.fillOval(x, y, width, height, argbTL, argbTR, argbBR, argbBL, clipX, clipY, clipW, clipH);
    }

    public void drawRoundRect(int x, int y, int width, int height, int thickness, int arcRadius, int argbTL, int argbTR, int argbBR,
                              int argbBL, int clipX, int clipY, int clipW, int clipH) {
        log("drawRoundRect");
        delegate.drawRoundRect(x, y, width, height, thickness, arcRadius, argbTL, argbTR, argbBR, argbBL, clipX, clipY, clipW,
                clipH);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL,
                              int clipX, int clipY, int clipW, int clipH) {
        log("fillRoundRect");
        delegate.fillRoundRect(x, y, width, height, arcRadius, argbTL, argbTR, argbBR, argbBL, clipX, clipY, clipW, clipH);
    }

    public void drawTexture(int x, int y, int width, int height, int handle, ImageHolder<Image> img, int srcx, int srcy, int srcwidth,
                            int srcheight, int blend) {
        //log(String.format("drawTexture[%s](%s,%s,%s,%s,%s,%s,%s,%s)", handle, x,y, width, height, srcx, srcy, srcwidth,srcheight));
        delegate.drawTexture(x, y, width, height, handle, img, srcx, srcy, srcwidth, srcheight, blend);
    }

    public void drawLine(int x1, int y1, int x2, int y2, int argb1, int argb2) {
        log("drawLine");
        delegate.drawLine(x1, y1, x2, y2, argb1, argb2);
    }

    public ImageHolder<Image> loadImage(int width, int height) {
        log(String.format("loadImage(%s,%s)", width, height));
        return delegate.loadImage(width, height);
    }

    @Override
    public void unloadImage(int handle, ImageHolder<Image> bi) {
        log(String.format("unloadImage(%s)", bi));
        delegate.unloadImage(handle, bi);
    }

    public ImageHolder<Image> createSurface(int handle, int width, int height) {
        log(String.format("createSurface[%s](%s,%s)", handle, width, height));
        return delegate.createSurface(handle, width, height);
    }

    public ImageHolder<Image> readImage(File cachedFile) throws Exception {
        log("readImage File: " + cachedFile.getName());
        return delegate.readImage(cachedFile);
    }

    public ImageHolder<Image> readImage(InputStream bais) throws Exception {
        log("readImage Stream");
        return delegate.readImage(bais);
    }

    public ImageHolder<Image> newImage(int destWidth, int destHeight) {
        log(String.format("newImage(%s,%s)", destWidth, destHeight));
        return delegate.newImage(destWidth, destHeight);
    }

    public void setTargetSurface(int handle, ImageHolder<Image> image) {
        log(String.format("setTargetSurface[%s]", handle));
        delegate.setTargetSurface(handle, image);
    }

    public void flipBuffer() {
        log("flipBuffer");
        delegate.flipBuffer();
    }

    public void startFrame() {
        log("startFrame");
        delegate.startFrame();
    }

    public void loadImageLine(int handle, ImageHolder<Image> image, int line, int len2, byte[] cmddata) {
        log(String.format("loadImageLine[%s](%s)", handle, line));
        delegate.loadImageLine(handle, image, line, len2, cmddata);
    }

    public void xfmImage(int srcHandle, ImageHolder<Image> srcImg, int destHandle, ImageHolder<Image> destImg, int destWidth,
                         int destHeight, int maskCornerArc) {
        log("xfmImage");
        delegate.xfmImage(srcHandle, srcImg, destHandle, destImg, destWidth, destHeight, maskCornerArc);
    }

    public boolean hasGraphicsCanvas() {
        //log("hasGraphicsCanvas");
        return delegate.hasGraphicsCanvas();
    }

    public Dimension getMaxScreenSize() {
        log("getMaxScreenSize");
        return delegate.getMaxScreenSize();
    }

    public Dimension getScreenSize() {
        log("getScreenSize");
        return delegate.getScreenSize();
    }

    public void setFullScreen(boolean b) {
        log("setFullScreen");
        delegate.setFullScreen(b);
    }

    public void setSize(int w, int h) {
        log("setSize");
        delegate.setSize(w, h);
    }

    public void invokeLater(Runnable runnable) {
        log("invokeLater");
        delegate.invokeLater(runnable);
    }

    @Override
    public Scale getScale() {
        log("getScale");
        return delegate.getScale();
    }

    @Override
    public boolean createVideo(int width, int height, int format) {
        log("createVideo");
        return delegate.createVideo(width, height, format);
    }

    @Override
    public boolean updateVideo(int frametype, ByteBuffer buf) {
        log("updateVideo");
        return delegate.updateVideo(frametype, buf);
    }

    @Override
    public MiniPlayerPlugin newPlayerPlugin(MiniClientConnection connection) {
        log("newPlayer");
        return delegate.newPlayerPlugin(connection);
    }

    @Override
    public void setVideoBounds(Rectangle o, Rectangle o1) {
        log("setVideoBounds");
        delegate.setVideoBounds(o, o1);
    }

    @Override
    public void onMenuHint(MenuHint hint) {
        log("onMenuHint");
        delegate.onMenuHint(hint);
    }

    @Override
    public boolean isFirstFrameRendered() {
        return delegate.isFirstFrameRendered();
    }

    @Override
    public void setVideoAdvancedAspect(String value) {
        log("setVideoAdvancedAspect");
        delegate.setVideoAdvancedAspect(value);
    }

}
