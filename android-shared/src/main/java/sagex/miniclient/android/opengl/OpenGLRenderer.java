package sagex.miniclient.android.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import sagex.miniclient.MenuHint;
import sagex.miniclient.MiniClient;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.android.R;
import sagex.miniclient.android.opengl.shapes.FillRectangle;
import sagex.miniclient.android.opengl.shapes.Line;
import sagex.miniclient.android.opengl.shapes.LineRectangle;
import sagex.miniclient.android.opengl.shapes.Triangle;
import sagex.miniclient.android.ui.AndroidUIController;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.android.video.exoplayer2.Exo2MediaPlayerImpl;
import sagex.miniclient.android.video.ijkplayer.IJKMediaPlayerImpl;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.ImageHolder;
import sagex.miniclient.uibridge.Scale;
import sagex.miniclient.uibridge.UIRenderer;
import sagex.miniclient.util.AspectHelper;
import sagex.miniclient.video.HasVideoInfo;
import sagex.miniclient.video.VideoInfoResponse;

public class OpenGLRenderer implements UIRenderer<OpenGLTexture>, GLSurfaceView.Renderer {
    private static final Logger log = LoggerFactory.getLogger(OpenGLRenderer.class);

    private static final int DEF_WIDTH=1280;
    private static final int DEF_HEIGHT=720;

    private final AndroidUIController activity;
    private final MiniClient client;

    // UI states, just MENU (default) and VIDEO.
    int state = STATE_MENU;

    // logging stuff
    boolean logFrameTime = false;
    boolean logTextureTime = false;
    private boolean logDetails = false;
    long longestTextureTime = 0;
    long totalTextureTime = 0;
    long frameTime = 0;
    long frame = 0;
    boolean firstFrame = true;
    boolean ready = false;
    boolean inFrame=false;

    boolean disableRenderQueue = false;

    // Current Surface (when surfaces are enabled)
    ImageHolder<? extends OpenGLTexture> currentSurface = null;
    ImageHolder<? extends OpenGLTexture> mainSurface = null;

    // render queues
    final private List<Runnable> renderQueue = new ArrayList<>();
    private List<Runnable> frameQueue = new ArrayList<>();

    private MiniPlayerPlugin player;

    // UI size is the size of the UI that is built, will be scaled up to the screenSize
    // this is the size of the UI that sagetv will build and send to us
    Dimension uiSize = new Dimension(DEF_WIDTH, DEF_HEIGHT);

    // Screen and UI resolutions
    // Total available screan pixels that we have
    Dimension fullScreenSize = uiSize.clone();


    // if true, the uiSize is set to the Native resolution
    boolean useNativeResolution = true;

    // the scaleAndCenterImmutable of the uiSize to the screenSize
    Scale scale = new Scale(1, 1);

    private Dimension lastResize = uiSize.clone();
    private Dimension lastScreenSize = uiSize.clone();
    private boolean firstResize = true; // true until after do the first resize

    private float uiAspectRatio = AspectHelper.ar_16_9;

    FillRectangle fillRectShape = new FillRectangle();
    LineRectangle lineRectShape = new LineRectangle();
    Line lineShape = new Line();
    private OpenGLSurfaceView glView;

    public OpenGLRenderer(AndroidUIController parent, MiniClient client) {
        this.activity = parent;
        this.client = client;
        client.setUIRenderer(this);
    }

    @Override
    public int getState() {
        return state;
    }

    public void create() {
        useNativeResolution = client.properties().getBoolean(PrefStore.Keys.use_native_resolution, true);

        fullScreenSize.updateFrom(getMaxScreenSize());
        lastResize.updateFrom(fullScreenSize);

        if (useNativeResolution) {
            uiSize.updateFrom(fullScreenSize);
        }

        scale.setScale(uiSize, fullScreenSize);

        log.debug("CREATE: UI SIZE: {}, SCREEN SIZE: {}", uiSize, fullScreenSize);

        // load shaders and programs
        try {
            OpenGLUtils.loadShaders(this.activity.getContext());
        } catch (IOException e) {
            log.error("Failed to Load Shaders.  UI will not work", e);
        }

        // create the main surface
        OpenGLSurface mainSurfaceGL = new MainOpenGLSurface(uiSize, fullScreenSize);
        mainSurfaceGL.createSurface();
        mainSurface = new ImageHolder<>(mainSurfaceGL, mainSurfaceGL.width, mainSurfaceGL.height);
        mainSurface.setHandle(0);
        setSurface(mainSurface);

        //debugShapes(mainSurfaceGL);
    }

    private void debugShapes(OpenGLSurface mainSurfaceGL) {
        GLES20.glClearColor(0, 1, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);

        Triangle tr = new Triangle();
        tr.draw(uiSize.width / 2, 0,
                0, uiSize.height,
                uiSize.width, uiSize.height, OpenGLUtils.RGBA_to_ARGB(255, 0, 0, 255), mainSurfaceGL);

        int color = OpenGLUtils.RGBA_to_ARGB(0, 0, 255, 255);
        fillRectShape.draw(100, 100, uiSize.width - 200, uiSize.height - 200, color, color, color, color, mainSurfaceGL);

        int color1 = OpenGLUtils.RGBA_to_ARGB(255, 0, 0, 255);
        int color2 = OpenGLUtils.RGBA_to_ARGB(175, 0, 0, 255);
        fillRectShape.draw(200, 200, 100, 100, color1, color2, color1, color2, mainSurfaceGL);
        fillRectShape.draw(uiSize.width - 300, uiSize.height - 300, 100, 100, color1, color1, color2, color2, mainSurfaceGL);

        int color3 = OpenGLUtils.RGBA_to_ARGB(0, 200, 0, 255);
        lineRectShape.draw(200, 200, 100, 100, color3, color3, color3, color3, 1, mainSurfaceGL);
        lineRectShape.draw(uiSize.width - 300, uiSize.height - 300, 100, 100, color3, color3, color3, color3, 10, mainSurfaceGL);

        lineShape.draw(100, 100, uiSize.width - 100, uiSize.height - 100, color1, color3, 1, mainSurfaceGL);

        Bitmap b = BitmapFactory.decodeResource(activity.getContext().getResources(), R.drawable.sage_logo_256);
        System.out.println("BITMAP: " + b.getWidth());

        OpenGLTexture t = new OpenGLTexture(b.getWidth(), b.getHeight());
        t.set(b, "test");
        t.draw(0, 0, b.getWidth(), b.getHeight(), 0, 0, b.getWidth(), b.getHeight(), OpenGLUtils.RGBA_to_ARGB(255, 255, 255, 255), mainSurfaceGL);

        // draw a partial
        t.draw(0, uiSize.height / 2, 50, 50, 0, 0, 50, 50, OpenGLUtils.RGBA_to_ARGB(255, 255, 255, 255), mainSurfaceGL);


        OpenGLSurface s = new OpenGLSurface(0, 100, 100);
        s.createSurface();
        s.bind();
        GLES20.glClearColor(1, 0, 0, 255);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        int c1 = OpenGLUtils.RGBA_to_ARGB(0, 0, 255, 255);
        lineShape.draw(0, 0, 100, 100, c1, c1, 1, s);

        mainSurfaceGL.bind();

        s.draw(0, 0, s.width, s.height, 0, 0, s.width, s.width, OpenGLUtils.RGBA_to_ARGB(255, 255, 255, 255), mainSurfaceGL);
        lineShape.draw(0, 100, 100, 200, c1, c1, 3, mainSurfaceGL);
        log.debug("* RENDERED BITMAP *");
        ((GLSurfaceView) activity.getUIView()).requestRender();
    }

    public void resize(int width, int height) {
        if (!firstResize && lastResize.equals(width, height) && lastScreenSize.equals(getScreenSize())) {
            log.debug("Resize Already Happened, Ignoring this: {}x{}", width, height);
            return;
        }

        log.debug("Got a Canvas Resize Event: {}x{}", width, height);
        fullScreenSize.update(width, height);
        this.scale.setScale(uiSize, fullScreenSize);

        ready = true;
        notifySageTVAboutScreenSize();
    }

    public void notifySageTVAboutScreenSize() {
        // NOTE: We always tell sagetv we are 1280x720, no matter our "real" size.

        log.debug("Notifying SageTV about the Resize Event: " + this.uiSize);
        try {
            if (!(client != null && client.getCurrentConnection() != null && client.getCurrentConnection().hasEventChannel())) {
                log.warn("Client and/or Client Connection is not ready.  Can't send a resize.");
                return;
            }

            client.getCurrentConnection().postResizeEvent(uiSize);
            firstResize = false;
        } catch (Throwable t) {
            log.info("Error sending Resize Event", t);
        }
    }

    public void render() {
        synchronized (renderQueue) {
            int size = renderQueue.size();
            if (size == 0) return;

            if (disableRenderQueue) {
                log.warn("********* RENDER QUEUE DISABLED for TESTING *********");
                renderQueue.clear();
                return;
            }

            long st = System.currentTimeMillis();

            if (logDetails)
                log.debug("Begin Render Frame {}", frame);

            try {
                for (int i=0;i<size;i++) {
                    try {
                        renderQueue.get(i).run();
                    } catch (Throwable t) {
                        log.error("Failed TO Render Instruction", t);
                    }
                }
            } catch (Throwable t) {
                log.error("Render Failed.  This should never happen.  Developer should figure out why", t);
                // TODO: How should we manage this.. request a re-render??
            } finally {
                renderQueue.clear();
            }
            if (logDetails)
                log.debug("End Render Frame {}", frame);

            long et = System.currentTimeMillis();
            if (logFrameTime) {
                log.debug("RENDER: Time: " + (et - st) + "ms; Ops: " + size);
            }
        }
    }

    @Override
    public void GFXCMD_INIT() {
        // block until the UI is ready
        log.debug("GFXCMD_INIT() called");
        while (!ready) {
            log.debug("Waiting For UI...");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }

        // one last attempt to setup our screen
        notifySageTVAboutScreenSize();
    }

    @Override
    public void GFXCMD_DEINIT() {
        activity.removeVideoFrame();
        activity.finish();

        // TODO: We should do some opengl clean up here
    }

    @Override
    public void close() {
        GFXCMD_DEINIT();
        if (player != null) {
            player.free();
            player = null;
        }

    }

    @Override
    public void refresh() {

    }

    @Override
    public void hideCursor() {

    }

    @Override
    public void showBusyCursor() {

    }

    @Override
    public void drawRect(final int x, final int y, final int width, final int height, final int thickness, final int argbTL, final int argbTR, final int argbBR, final int argbBL) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                lineRectShape.draw(x, y, width, height, argbTL, argbTR, argbBR, argbBL, thickness, currentSurface());
            }
        });
    }

    @Override
    public void fillRect(final int x, final int y, final int width, final int height, final int argbTL, final int argbTR, final int argbBR, final int argbBL) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
//                if (!(argbBL == argbBR && argbBL == argbTL && argbBL == argbTR)) {
//                    log.debug("FILLRECT: {},{} {}x{} - TL:{},TR:{} BR:{},BL:{}", x, y, width, height, argbTL, argbTR, argbBR, argbBL);
//                }
                fillRectShape.draw(x, y, width, height, argbTL, argbTR, argbBR, argbBL, currentSurface());
            }
        });
    }

    @Override
    public void clearRect(final int x, final int y, final int width, final int height, final int argbTL, final int argbTR, final int argbBR, final int argbBL) {
        state = STATE_VIDEO;
        invokeLater(new Runnable() {
            @Override
            public void run() {
                fillRectShape.draw(x, y, width, height, argbTL, argbTR, argbBR, argbBL, currentSurface());
            }
        });
    }

    @Override
    public void drawOval(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        log.warn("Draw Oval not supported");
    }

    @Override
    public void fillOval(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        log.warn("Fill Oval not supported");
    }

    @Override
    public void drawRoundRect(final int x, final int y, final int width, final int height, final int thickness, int arcRadius, final int argbTL, final int argbTR, final int argbBR, final int argbBL, int clipX, int clipY, int clipW, int clipH) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                lineRectShape.draw(x, y, width, height, argbTL, argbTR, argbBR, argbBL, thickness, currentSurface());
            }
        });
    }

    @Override
    public void fillRoundRect(final int x, final int y, final int width, final int height, int arcRadius, final int argbTL, final int argbTR, final int argbBR, final int argbBL, int clipX, int clipY, int clipW, int clipH) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                fillRectShape.draw(x, y, width, height, argbTL, argbTR, argbBR, argbBL, currentSurface());
            }
        });
    }

    @Override
    public void drawTexture(final int x, final int y, final int width, final int height, final int handle, final ImageHolder<OpenGLTexture> img, final int srcx, final int srcy, final int srcwidth, final int srcheight, final int blend) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    //log.debug("{},{},{},{} => {},{},{},{}", x, y, width, height, srcx, srcy, srcwidth, srcheight);
                    img.get().draw(x, y, width, height, srcx, srcy, srcwidth, srcheight, blend, currentSurface());
                } catch (Throwable t) {
                    log.error("Failed to Render Texture {}", handle, t);
                }
            }
        });
    }

    OpenGLSurface currentSurface() {
        return OpenGLSurface.get(currentSurface.get());
    }

    @Override
    public void drawLine(final int x1, final int y1, final int x2, final int y2, final int argbTL, final int argbTR) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                lineShape.draw(x1, y1, x2, y2, argbTL, argbTR, 1, currentSurface());
            }
        });
    }

    @Override
    public ImageHolder<OpenGLTexture> loadImage(int width, int height) {
        if (logDetails)
            log.debug("load image {}x{}", width, height);
        final OpenGLTexture t = new OpenGLTexture(width, height);
        invokeLater(new Runnable() {
            @Override
            public void run() {
                t.createTexture();
            }
        });
        return new ImageHolder<>(t, width, height);
    }

    @Override
    public void unloadImage(int handle, ImageHolder<OpenGLTexture> bi) {
        if (bi != null && bi.get() != null) {
            log.debug("Unloading Image: {}", bi);
            bi.get().delete();
            bi.dispose();
        }
    }

    @Override
    public ImageHolder<OpenGLTexture> createSurface(final int handle, final int width, final int height) {

        if (handle == 0) {
            log.error("WE ALREADY CREATED 0", new Exception("CREATED 0 AGAIN"));
        }

        final OpenGLTexture t = new OpenGLSurface(handle, width, height);
        final ImageHolder<OpenGLTexture> h = new ImageHolder<>(t, width, height);
        h.setHandle(handle);
        invokeLater(new Runnable() {
            @Override
            public void run() {
                /*if (logFrameBuffer)*/
                if (logDetails)
                    log.debug("createSurface[" + handle + "]: Creating Framebuffer: " + width + "x" + height);
                OpenGLSurface.get(h.get()).createSurface();
                setSurface(currentSurface);
            }
        });
        return h;
    }

    void setSurface(ImageHolder<? extends OpenGLTexture> t) {
        assert t != null;
        assert t.get() != null;

        // bind it (is, SetTargetSurface) and set it up
        OpenGLSurface surface = OpenGLSurface.get(t.get());
        surface.bind();
        currentSurface=t;
        if (logDetails)
            log.debug("Current Surface is now: {}", currentSurface.getHandle());
    }

    @Override
    public void setTargetSurface(final int handle, final ImageHolder<OpenGLTexture> image) {
        final ImageHolder<? extends OpenGLTexture> surface = (handle==0 || image==null) ? mainSurface : image;
        invokeLater(new Runnable() {
            @Override
            public void run() {
                setSurface(surface);
            }
        });
    }

    @Override
    public ImageHolder<OpenGLTexture> readImage(File file) throws Exception {
        try {
            FileInputStream fis = new FileInputStream(file);
            try {
                return readImage(fis);
            } finally {
                fis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ImageHolder<OpenGLTexture> readImage(InputStream fis) throws Exception {
        long st = System.currentTimeMillis();

        BitmapFactory.Options options = new BitmapFactory.Options();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // this appears to work better since Android O (documentation says it's prefferred
            // for bitmaps that are only used to render to the screen (immutable)
            options.inPreferredConfig = Bitmap.Config.HARDWARE;
        } else {
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        }
        options.inDither = true;
        options.inDensity = 32;
        options.inTargetDensity = 32;
        options.inPurgeable = true;
        final Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);


        long time = System.currentTimeMillis() - st;
        totalTextureTime += time;
        longestTextureTime = Math.max(time, longestTextureTime);

        final OpenGLTexture t = new OpenGLTexture(bitmap.getWidth(), bitmap.getHeight());

        invokeLater(new Runnable() {
            @Override
            public void run() {
                t.set(bitmap, "");
            }
        });
        return new ImageHolder<>(t, t.width, t.height);
    }

    @Override
    public ImageHolder<OpenGLTexture> newImage(int destWidth, int destHeight) {
        return loadImage(destWidth, destHeight);
    }

    @Override
    public void registerTexture(ImageHolder<OpenGLTexture> texture) {
        // not used here, but, could be used to let the UI know that this texture is being registered.
    }

    @Override
    public void flipBuffer() {
        if (firstFrame) {
            firstFrame = false;
            activity.setConnectingIsVisible(false);
        }

        synchronized (renderQueue) {
            renderQueue.addAll(frameQueue);
            frameQueue.clear();
        }

        // request a render frame
        glView.requestRender();

        if (logFrameTime) {
            log.debug("FRAME: " + (frame) + "; Time: " + (System.currentTimeMillis() - frameTime) + "ms");
        }
        if (logTextureTime) {
            log.debug("FRAME: " + (frame) + "; Texture Load Time: " + totalTextureTime + "ms; Longest Single: " + longestTextureTime + "ms");
        }
        frame++;
        inFrame=false;
    }

    @Override
    public void startFrame() {
        frameTime = System.currentTimeMillis();
        totalTextureTime = 0;
        longestTextureTime = 0;
        state = STATE_MENU;
        invokeLater(new Runnable() {
            @Override
            public void run() {
                setSurface(mainSurface);
                clearUI();
            }
        });
        inFrame=true;
    }

    void clearUI() {
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void loadImageLine(int handle, ImageHolder<OpenGLTexture> image, int line, int len2, byte[] b) {
//        Bitmap bm = image.get().bitmap;
//        int datapos, x;
//        for (datapos = 12, x = 0; x < len2 / 4; x++, datapos += 4) {
//            bm.setPixel(x, line, readInt(datapos, b));
//        }
    }

    @Override
    public void xfmImage(int srcHandle, ImageHolder<OpenGLTexture> srcImg, int destHandle, ImageHolder<OpenGLTexture> destImg, int destWidth, int destHeight, int maskCornerArc) {

    }

    @Override
    public boolean hasGraphicsCanvas() {
        return false;
    }

    @Override
    public Dimension getMaxScreenSize() {
        if (fullScreenSize.getWidth()<=0) {
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return new Dimension(size.x, size.y);
        } else {
            return fullScreenSize;
        }
    }

    @Override
    public Dimension getScreenSize() {
        Dimension newSize = new Dimension(getMaxScreenSize());
        return newSize;
    }

    @Override
    public void setFullScreen(boolean b) {
        log.warn("Set FullScreen was called but we did nothing with {}", b);
    }

    @Override
    public void setSize(int w, int h) {
        log.warn("Set Size was called but we did nothing with {} x {}", w, h);
    }

    @Override
    public void invokeLater(Runnable runnable) {
        frameQueue.add(runnable);
    }

    @Override
    public Scale getScale() {
        return scale;
    }

    @Override
    public boolean createVideo(int width, int height, int format) {
        log.debug("CREATE VIDEO " + width + "x" + height + "; Format: " + format);
        return false;
    }

    @Override
    public boolean updateVideo(int frametype, ByteBuffer buf) {
        log.debug("UPDATE VIDEO FRAMETYPE:" + frametype);
        return false;
    }

    @Override
    public MiniPlayerPlugin newPlayerPlugin(MiniClientConnection connection) {
        log.debug("New Player Requested");
        //return new ExoPlayerMediaPlayer(activity);
        if (player != null) {
            player.free();
        }

        boolean useExoPlayer = client.properties().getBoolean(PrefStore.Keys.use_exoplayer, false);
        if (activity.isSwitchingPlayerOneTime()) {
            useExoPlayer = !useExoPlayer;
        }

        if (useExoPlayer) {
            log.debug("Using ExoPlayer");
            //player = new ExoMediaPlayerImpl(activity);
            player = new Exo2MediaPlayerImpl(activity);
        } else {
            log.debug("Using iJKPlayer");
            player = new IJKMediaPlayerImpl(activity);
        }
        return player;
    }

    @Override
    public void setVideoBounds(sagex.miniclient.uibridge.Rectangle o, sagex.miniclient.uibridge.Rectangle o1) {
        if (logDetails)
            log.debug("Set Video Bounds: SRC:{}, DEST:{}", o, o1);
        state = STATE_VIDEO;
    }

    @Override
    public void onMenuHint(MenuHint hint) {
        activity.showHideKeyboard(hint.hasTextInput);
    }

    @Override
    public boolean isFirstFrameRendered() {
        // we set firstFrame=false after first frame is rendered, so isFirstFrameRendered is !firstFrame
        return !firstFrame;
    }

    @Override
    public void setVideoAdvancedAspect(String value) {
        log.debug("Handling Video Aspect Ration Change Request: " + value);
        if (player!=null) {
            player.setVideoAdvancedAspect(value);
        }
    }

    @Override
    public void setUIAspectRatio(float value) {
        if (value<0) {
            uiAspectRatio = AspectHelper.ar_16_9;
        }
        else {
            uiAspectRatio = value;
        }
        if (player!=null && player instanceof BaseMediaPlayerImpl) {
            ((BaseMediaPlayerImpl)player).notifyUIAspectChanged();
        }
        log.debug("UI Apect Ratio has been set to " + uiAspectRatio);
    }

    @Override
    public float getUIAspectRatio() {
        return uiAspectRatio;
    }

    public VideoInfoResponse getVideoInfo() {
        if (player==null) {
            return null;
        }

        if (player instanceof HasVideoInfo) {
            return ((HasVideoInfo)player).getVideoInfo();
        }

        return null;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        create();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        resize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        render();
    }

    public void setView(OpenGLSurfaceView openGLSurfaceView) {
        this.glView = openGLSurfaceView;
    }
}
