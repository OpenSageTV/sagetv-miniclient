package sagex.miniclient.desktop;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sagex.miniclient.MenuHint;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.desktop.swing.BufferedImageTexture;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.*;
import sagex.miniclient.uibridge.Rectangle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class DesktopClientRenderer implements UIRenderer<BufferedImageTexture>,MiniClientWindow.ResizeListener {
    private final MiniClientWindow clientWindow;

    Logger log = LoggerFactory.getLogger(DesktopClientRenderer.class);

    boolean firstFrameRendered=false;

    BufferedImageTexture canvasZero = null;
    BufferedImageTexture currentSurface = null;

    Dimension screenSize;
    Dimension maxScreenSize;

    public DesktopClientRenderer(MiniClientWindow miniClientWindow) {
        this.clientWindow=miniClientWindow;
        this.clientWindow.setResizeListener(this);
    }

    @Override
    public int getState() {
        log.debug("getState()");
        return 0;
    }

    @Override
    public void GFXCMD_INIT() {
        log.debug("GFXCMD_INIT()");
        Dimension d = getScreenSize();
        canvasZero = new BufferedImageTexture(d.getWidth(), d.getHeight());
        currentSurface = canvasZero;
        notifySageTVAboutScreenSize();
    }

    public void initCanvas() {
        Dimension d = getScreenSize();
        if (canvasZero!=null) {
            canvasZero.getGraphics().dispose();
        }
        canvasZero = new BufferedImageTexture(d.getWidth(), d.getHeight());
        currentSurface = canvasZero;
    }

    public void notifySageTVAboutScreenSize() {
        log.debug("Notifying SageTV about the Resize Event: " + getScreenSize());
        try {
            if (!MiniClientInstance.get().getClient().isReady()) {
                log.warn("Client and/or Client Connection is not ready.  Can't send a resize.");
                Platform.runLater(() -> notifySageTVAboutScreenSize());
                return;
            }

            log.debug("Notified SageTV about resize event.");
            MiniClientInstance.get().getClient().getCurrentConnection().postResizeEvent(getScreenSize());
            //firstResize = false;
        } catch (Throwable t) {
            log.info("Error sending Resize Event", t);
        }
    }

    @Override
    public void GFXCMD_DEINIT() {
        log.debug("GFXCMD_DEINIT()");
        currentSurface =null;
    }

    @Override
    public void close() {
        log.debug("close()");
    }

    @Override
    public void refresh() {
        log.debug("refresh()");
    }

    @Override
    public void hideCursor() {
        log.debug("hideCursor()");
    }

    @Override
    public void showBusyCursor() {
        log.debug("showBusyCursor()");
    }

    @Override
    public void drawRect(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL) {
        log.debug("drawRect()");
    }

    @Override
    public void fillRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL) {
        log.debug("fillRect()");
    }

    @Override
    public void clearRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL) {
        log.debug("clearRect()");
        Graphics2D g2 = currentSurface.getGraphics();
        g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC));
        g2.setBackground(new java.awt.Color(argbTL, true));
        g2.clearRect(x, y, width, height);
    }

    @Override
    public void drawOval(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        log.debug("drawOval()");

    }

    @Override
    public void fillOval(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        log.debug("fillOval()");

    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int thickness, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        log.debug("drawRoundRect()");

    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        log.debug("fillRoundRect()");

    }

    @Override
    public void drawTexture(int x, int y, int width, int height, int handle, ImageHolder<BufferedImageTexture> img, int srcx, int srcy, int srcwidth, int srcheight, int blend) {
        //log.debug(String.format("drawTexture[%s](%s,%s,%s,%s,%s,%s,%s,%s)", handle, x,y, width, height, srcx, srcy, srcwidth,srcheight));
        Graphics2D g2 = currentSurface.getGraphics();
        try
        {
            if (width > 0)
            {
                int blendMode = java.awt.AlphaComposite.SRC_OVER;
                if (height < 0)
                {
                    blendMode = java.awt.AlphaComposite.SRC;
                    height = height * -1;
                }
                g2.setComposite(java.awt.AlphaComposite.getInstance(blendMode, ((blend >> 24) & 0xFF) / 255.0f));
                g2.drawImage(img.get().getImage(), x, y, x + width, y + height, srcx, srcy, srcx + srcwidth, srcy + srcheight, null);

//                java.awt.Color c=g2.getColor();
//                g2.setColor(java.awt.Color.RED);
//                g2.drawRect(x,y,width,height);
//                g2.setColor(c);
            }
            else
            {
                // font
                java.awt.Color blendColor = new java.awt.Color(blend, true);
                java.awt.image.RescaleOp colorScaler = new java.awt.image.RescaleOp(
                        blendColor.getRGBComponents(null), new float[] { 0f, 0f, 0f, 0f }, null);
                java.awt.image.BufferedImage subImage = img.get().getImage().getSubimage(srcx, srcy, srcwidth, srcheight);
                java.awt.image.BufferedImage bi2 = colorScaler.filter(subImage, null);
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER));
                g2.drawImage(bi2, x, y, x - width, y + height, 0, 0, srcwidth, srcheight, null);
                bi2.flush();
                bi2 = null;
            }
        }
        catch (Exception e)
        {
            System.out.println("ERROR: " + e);
            e.printStackTrace();
        }
        if (width > 0)
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER));

    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int argb1, int argb2) {
        log.debug("drawLine()");

    }

    @Override
    public ImageHolder<BufferedImageTexture> loadImage(int width, int height) {
        log.debug("loadImage()");
        BufferedImageTexture texture = new BufferedImageTexture(width, height);
        return new ImageHolder(texture, width, height);
    }

    @Override
    public void unloadImage(int handle, ImageHolder bi) {
        log.debug("unloadImage()");
    }

    @Override
    public ImageHolder createSurface(int handle, int width, int height) {
        log.debug("createSurface(): " + handle + "; "+width+"x"+height);
        BufferedImageTexture texture = new BufferedImageTexture(width, height);
        return new ImageHolder(texture, width, height);
    }

    @Override
    public ImageHolder readImage(File cachedFile) throws Exception {
        log.debug("readImage(): " + cachedFile);
        if (cachedFile.exists()) {
            try (FileInputStream fis = new FileInputStream(cachedFile)) {
                //Image image = new Image(fis);
                // BufferedImage bi = SwingFXUtils.fromFXImage(image, null);
                BufferedImage bi = ImageIO.read(cachedFile);
                log.debug("readImage: " + bi.getWidth() +"," + bi.getHeight());
                return new ImageHolder(new BufferedImageTexture(bi), bi.getWidth(), bi.getHeight());
            } catch (Throwable t) {
                log.debug("Failed to load Image: " + cachedFile);
                return null;
            }
        }
        return null;
    }

    @Override
    public ImageHolder readImage(InputStream bais) throws Exception {
        log.debug("readImage(): Stream");
        try {
            BufferedImage image = ImageIO.read(bais);
            return new ImageHolder(new BufferedImageTexture(image), (int)image.getWidth(), (int)image.getHeight());
        } catch (Throwable t) {
            log.debug("Failed to load Image from stream",t);
            return null;
        }
    }

    @Override
    public ImageHolder newImage(int destWidth, int destHeight) {
        log.debug("newImage()");
        BufferedImageTexture texture = new BufferedImageTexture(destWidth, destHeight);
        return new ImageHolder(texture, destWidth, destHeight);
    }

    @Override
    public void registerTexture(ImageHolder<BufferedImageTexture> texture) {
        // not needed, just tells us that this texture is being used and is loaded.
    }

    @Override
    public void setTargetSurface(int handle, ImageHolder<BufferedImageTexture> image) {
        log.debug("setTargetSurface(): " + handle);
        if (handle==0) {
            currentSurface=canvasZero;
        } else {
            currentSurface=image.get();
        }
    }

    @Override
    public void flipBuffer() {
        log.debug("flipBuffer()");
        if (!firstFrameRendered) {
            clientWindow.showCanvas(getScreenSize());
        }
        firstFrameRendered=true;
        copyCanvas(currentSurface, clientWindow.getCanvas());
    }

    @Override
    public void startFrame() {
        log.debug("startFrame()");
        //currentSurface.getGraphics().clearRect(0,0, currentSurface.getImage().getWidth(), currentSurface.getImage().getHeight());
    }

    @Override
    public void loadImageLine(int handle, ImageHolder image, int line, int len2, byte[] cmddata) {
        log.debug("loadImageLine()");

    }

    @Override
    public void xfmImage(int srcHandle, ImageHolder srcImg, int destHandle, ImageHolder destImg, int destWidth, int destHeight, int maskCornerArc) {
        log.debug("xfmImage()");

    }

    @Override
    public boolean hasGraphicsCanvas() {
        //log.debug("hasGraphicsCanvas()");
        return currentSurface !=null;
    }

    @Override
    public Dimension getMaxScreenSize() {
        if (maxScreenSize==null) {
            Rectangle2D size = Screen.getPrimary().getVisualBounds();
            maxScreenSize = new Dimension((int) size.getWidth(), (int) size.getHeight());
            log.debug("getMaxScreenSize(): " + maxScreenSize);
        }
        return maxScreenSize;
    }

    @Override
    public Dimension getScreenSize() {
        if (screenSize==null) {
            screenSize = clientWindow.getWindowSize();
            log.debug("getScreenSize(): " + screenSize);
        }

        return screenSize;
    }

    @Override
    public void setFullScreen(boolean b) {
        log.debug("setFullScreen()");
    }

    @Override
    public void setSize(int w, int h) {
        log.debug("setSize(): " + w + "x" + h);
    }

    @Override
    public void invokeLater(Runnable runnable) {
        // run on the UI thread
        Platform.runLater(runnable);
    }

    @Override
    public Scale getScale() {
        log.debug("getScale()");
        return new Scale(1,1);
    }

    @Override
    public boolean createVideo(int width, int height, int format) {
        log.debug("createVideo()");
        return false;
    }

    @Override
    public boolean updateVideo(int frametype, ByteBuffer buf) {
        log.debug("updateVideo()");
        return false;
    }

    @Override
    public MiniPlayerPlugin newPlayerPlugin(MiniClientConnection connection) {
        log.debug("newPlayerPlugin()");
        return null;
    }

    @Override
    public void setVideoBounds(Rectangle o, Rectangle o1) {
        log.debug("setVideoBounds()");
    }

    @Override
    public void onMenuHint(MenuHint hint) {
        log.debug("onMenuHint()");
    }

    @Override
    public boolean isFirstFrameRendered() {
        log.debug("isFirstFrameRendered()");
        return firstFrameRendered;
    }

    @Override
    public void setVideoAdvancedAspect(String value) {
        log.debug("setVideoAdvancedAspect()");

    }

    @Override
    public void setUIAspectRatio(float value) {
        log.debug("setUIAspectRatio()");

    }

    @Override
    public float getUIAspectRatio() {
        log.debug("getUIAspectRatio()");
        return 1;
    }

    private void copyCanvas(final BufferedImageTexture src, final Canvas dest) {
        final Image image = SwingFXUtils.toFXImage(src.getImage(), null);
        src.getImage().flush();
        Platform.runLater(()->{
            GraphicsContext g = dest.getGraphicsContext2D();
            g.drawImage(image, 0, 0);
        });
        if (log.isDebugEnabled()) {
            System.gc();
            log.debug("Mem: {} used of {}", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024f / 1024f, Runtime.getRuntime().totalMemory() / 1024f / 1024f);
        }
    }

    public static double map(double value, double start, double stop, double targetStart, double targetStop) {
        return targetStart + (targetStop - targetStart) * ((value - start) / (stop - start));
    }

    public static Color getColor(int color) {
        return new Color(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, ((color) & 0xFF) / 255f, ((color >> 24) & 0xFF) / 255f);
    }

    @Override
    public void onResized(int w, int h) {
        screenSize=null;
        maxScreenSize=null;
        log.debug("RESIZE: {}x{}", w,h);
        getScreenSize();
        log.debug("SCREEN: {}x{}", getScreenSize().width,getScreenSize().height);
        getMaxScreenSize();
        initCanvas();
        clientWindow.getCanvas().setWidth(w);
        clientWindow.getCanvas().setHeight(h);

        notifySageTVAboutScreenSize();
    }
}
