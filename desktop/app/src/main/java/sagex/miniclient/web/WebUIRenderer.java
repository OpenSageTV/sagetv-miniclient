package sagex.miniclient.web;

import javafx.scene.paint.Color;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sagex.miniclient.MenuHint;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.desktop.DesktopClientRenderer;
import sagex.miniclient.desktop.swing.BufferedImageTexture;
import sagex.miniclient.uibridge.*;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Rectangle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

public class WebUIRenderer implements UIRenderer<WebTexture> {
    Logger log = LoggerFactory.getLogger(DesktopClientRenderer.class);

    private final Session session;
    private final RemoteEndpoint remote;

    Dimension size;
    Dimension maxSize;

    public WebUIRenderer(Session session, int w, int h) {
        this.session=session;
        this.remote=session.getRemote();
        this.size=new Dimension(w,h);
        this.maxSize=size;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public void GFXCMD_INIT() {
        sendString("init");
    }

    private void sendString(String init) {
        try {
            remote.sendString(init);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendBytes(ByteBuffer buffer) {
        try {
            remote.sendBytesByFuture(buffer).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void GFXCMD_DEINIT() {
        sendString("deinit");
    }

    @Override
    public void close() {
        sendString("close");
    }

    @Override
    public void refresh() {
        sendString("refresh");
    }

    @Override
    public void hideCursor() {

    }

    @Override
    public void showBusyCursor() {

    }

    @Override
    public void drawRect(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL) {

    }

    @Override
    public void fillRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL) {

    }

    @Override
    public void clearRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL) {

    }

    @Override
    public void drawOval(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {

    }

    @Override
    public void fillOval(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {

    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int thickness, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {

    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {

    }

    @Override
    public void drawTexture(int x, int y, int width, int height, int handle, ImageHolder<WebTexture> img, int srcx, int srcy, int srcwidth, int srcheight, int blend) {

        sendString("draw_texture " + handle + " " + x + " " + y + " " + width + " " + height + " " + srcx + " " + srcy + " " + srcwidth + " " + srcheight + " " + getColor(blend) + " " + (((blend >> 24) & 0xFF)/255f));
    }

    public static String getColor(int color) {
        return String.format("#%02x%02x%02x", ((color >> 16) & 0xFF), ((color >> 8) & 0xFF), ((color) & 0xFF));
    }


    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int argb1, int argb2) {

    }

    @Override
    public ImageHolder<WebTexture> loadImage(int width, int height) {
        return new ImageHolder<>(new WebTexture(), width, height);
    }

    @Override
    public void unloadImage(int handle, ImageHolder<WebTexture> bi) {
    }

    @Override
    public ImageHolder<WebTexture> createSurface(int handle, int width, int height) {
        sendString("create_surface " + handle + " " + width + " " + height);
        return new ImageHolder<>(new WebTexture(), width, height);
    }

    @Override
    public ImageHolder readImage(File cachedFile) throws Exception {
        if (cachedFile.exists()) {
            BufferedImage bi = ImageIO.read(cachedFile);
            ImageHolder h = new ImageHolder(new WebTexture(ByteBuffer.wrap(Files.readAllBytes(cachedFile.toPath()))), bi.getWidth(), bi.getHeight());
            bi.flush();
            return h;
        }
        return null;
    }

    @Override
    public ImageHolder readImage(InputStream bais) throws Exception {
        log.debug("readImage(): Stream");
        return null;
    }

    @Override
    public ImageHolder<WebTexture> newImage(int destWidth, int destHeight) {
        return new ImageHolder<>(new WebTexture(), destWidth, destHeight);
    }

    @Override
    public void registerTexture(ImageHolder<WebTexture> texture) {
        // send the image and texture bytes
        sendString("register " + texture.getHandle() + " " + texture.getWidth() + " " + texture.getHeight());
        sendBytes(texture.get().getTexture());
    }

    @Override
    public void setTargetSurface(int handle, ImageHolder<WebTexture> image) {
        sendString("set_surface " + handle);
    }

    @Override
    public void flipBuffer() {
        sendString("flip");
    }

    @Override
    public void startFrame() {
        sendString("start");
    }

    @Override
    public void loadImageLine(int handle, ImageHolder<WebTexture> image, int line, int len2, byte[] cmddata) {

    }

    @Override
    public void xfmImage(int srcHandle, ImageHolder<WebTexture> srcImg, int destHandle, ImageHolder<WebTexture> destImg, int destWidth, int destHeight, int maskCornerArc) {

    }

    @Override
    public boolean hasGraphicsCanvas() {
        return true;
    }

    @Override
    public Dimension getMaxScreenSize() {
        return size;
    }

    @Override
    public Dimension getScreenSize() {
        return size;
    }

    @Override
    public void setFullScreen(boolean b) {

    }

    @Override
    public void setSize(int w, int h) {

    }

    @Override
    public void invokeLater(Runnable runnable) {

    }

    @Override
    public Scale getScale() {
        return new Scale(1,1);
    }

    @Override
    public boolean createVideo(int width, int height, int format) {
        return false;
    }

    @Override
    public boolean updateVideo(int frametype, ByteBuffer buf) {
        return false;
    }

    @Override
    public MiniPlayerPlugin newPlayerPlugin(MiniClientConnection connection) {
        return null;
    }

    @Override
    public void setVideoBounds(Rectangle o, Rectangle o1) {

    }

    @Override
    public void onMenuHint(MenuHint hint) {

    }

    @Override
    public boolean isFirstFrameRendered() {
        return true;
    }

    @Override
    public void setVideoAdvancedAspect(String value) {

    }

    @Override
    public void setUIAspectRatio(float value) {

    }

    @Override
    public float getUIAspectRatio() {
        return 1;
    }
}
