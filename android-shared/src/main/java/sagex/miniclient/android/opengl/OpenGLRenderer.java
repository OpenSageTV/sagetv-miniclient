package sagex.miniclient.android.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.Display;
import android.view.WindowManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import sagex.miniclient.MenuHint;
import sagex.miniclient.MiniClient;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.android.R;
import sagex.miniclient.android.ui.AndroidUIController;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.android.video.exoplayer2.Exo2MediaPlayerImpl;
import sagex.miniclient.android.video.ijkplayer.IJKMediaPlayerImpl;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.ImageHolder;
import sagex.miniclient.uibridge.Rectangle;
import sagex.miniclient.uibridge.Scale;
import sagex.miniclient.uibridge.UIRenderer;
import sagex.miniclient.util.AspectHelper;
import sagex.miniclient.util.VerboseLogging;
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
    boolean logFrameBuffer = true;//VerboseLogging.DETAILED_GFX_TEXTURES;
    boolean logFrameTime = false;
    boolean logTextureTime = false;
    private boolean logTexture = VerboseLogging.DETAILED_GFX_TEXTURES;
    long longestTextureTime = 0;
    long totalTextureTime = 0;
    long frameTime = 0;
    long frame = 0;
    boolean firstFrame = true;
    boolean ready = false;
    boolean inFrame=false;

    boolean disableRenderQueue = true;

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

        // we are using static 1280x720
        //uiSize.setSize(1280, 720);
        //lastScreenSize.updateFrom(uiSize);

        scale.setScale(uiSize, fullScreenSize);

        log.debug("CREATE: UI SIZE: {}, SCREEN SIZE: {}", uiSize, fullScreenSize);

        // load shaders and programs
        try {
            ShaderUtils.loadShaders(this.activity.getContext());
        } catch (IOException e) {
            log.error("Failed to Load Shaders.  UI will not work", e);
        }

        // create the main surface
        OpenGLSurface mainSurfaceGL = new MainOpenGLSurface(uiSize.width, uiSize.height);
        mainSurfaceGL.createSurface();
        mainSurface = new ImageHolder<>(mainSurfaceGL, uiSize.width, uiSize.height);
        mainSurface.setHandle(0);

        setSurface(mainSurface);

        GLES20.glViewport(0, 0, uiSize.width, uiSize.height);

        //clearUI();
        GLES20.glClearColor(0, 1, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);

        Bitmap b = BitmapFactory.decodeResource(activity.getContext().getResources(), R.drawable.sage_logo_256);
        OpenGLTexture t = new OpenGLTexture(256, 89);
        t.set(b, "test");
        t.draw(0, 0, 256, 89, 0, 0, 256, 89, 0, mainSurfaceGL, 0);

        log.debug("* RENDERED BITMAP *");
        flipBuffer();

        //drawLine(0,0, uiSize.width/2, uiSize.height/2, 0, 0);

////        // we just set a green color so that we know something is happening
//        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 0.0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
    }

    public void resize(int width, int height) {
        if (!firstResize && lastResize.equals(width, height) && lastScreenSize.equals(getScreenSize())) {
            log.debug("Resize Already Happened, Ignoring this: {}x{}", width, height);
            return;
        }

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
                log.warn("********* RENDER QUEUE DISABLED *********");
                renderQueue.clear();
                return;
            }

            long st = System.currentTimeMillis();

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

            log.debug("End Render Frame {}", frame);

            //glFlipBuffer();

            long et = System.currentTimeMillis();
            if (logFrameTime) {
                log.debug("RENDER: Time: " + (et - st) + "ms; Ops: " + size);
            }
        }
    }

//    private void glFlipBuffer() {
//        log.debug("begin render frame");
//
//        OpenGLSurface main = OpenGLSurface.get(mainSurface.get());
//        main.unbind();
//
//        // bind to main UI
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
//        GLES20.glViewport(0,0, uiSize.width, uiSize.height);
//
//        // render this surface to the main UI
//        main.draw();
//
//        log.debug("end render frame");
//    }

//    private Color getColor(int color) {
//        if (lastColor != null && color == lastColorInt) return lastColor;
//        lastColorInt = color;
//        lastColor = new Color(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, ((color) & 0xFF) / 255f, ((color >> 24) & 0xFF) / 255f);
//        return lastColor;
//    }

    float Y(int y, int height) {
        return uiSize.getHeight() - y - height;
    }

    float Y(int y) {
        return uiSize.getHeight() - y;
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
    public void drawTexture(final int x, final int y, final int width, final int height, final int handle, final ImageHolder<OpenGLTexture> img, final int srcx, final int srcy, final int srcwidth, final int srcheight, final int blend) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    img.get().draw(x, y, width, height, srcx, srcy, srcwidth, srcheight, blend, (OpenGLSurface) currentSurface.get(), currentSurface.getHandle());
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
    public void drawLine(int x1, int y1, int x2, int y2, int argbTL, int argbTR) {
        ShaderUtils.useProgram(ShaderUtils.defaultShader);
        GLES20.glUniformMatrix4fv(ShaderUtils.defaultShader.u_myPMVMatrix, 1, false, currentSurface().viewMatrix, 0);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        int buffer[] = new int[1];
        GLES20.glGenBuffers(1, buffer, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffer[0]);
        //float vertices[] = {x1, y1, x1, y1};
        float vertices[] = {0, 0, 100, 100};
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.length * 4, FloatBuffer.wrap(vertices), GLES20.GL_STATIC_DRAW);
        GLES20.glVertexAttribPointer(ShaderUtils.defaultShader.a_myVertex, 2, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glEnableVertexAttribArray(ShaderUtils.defaultShader.a_myVertex);

        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

////        GLES20.glUniform4fv(ShaderUtils.GRADIENT_PROGRAM_argb_tl, ShaderUtils.intToFloatArray(argbTL));
////        GLES20.glUniform4fv(ShaderUtils.GRADIENT_PROGRAM_argb_tr, ShaderUtils.intToFloatArray(argbTR));
////        GLES20.glUniform4fv(ShaderUtils.GRADIENT_PROGRAM_argb_bl, ShaderUtils.intToFloatArray(argbTL));
////        GLES20.glUniform4fv(ShaderUtils.GRADIENT_PROGRAM_argb_br, ShaderUtils.intToFloatArray(argbTR));
////
////        GLES20.glUniform2fv(ShaderUtils.GRADIENT_PROGRAM_resolution, new Float32Array([WINDOW_WIDTH, WINDOW_HEIGHT]));
//
//        // Sets the vertex data to this attribute index
//        int vertBuffer[] = ShaderUtils.glCreateBuffer();
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertBuffer);
//        int vertices[] = {x1,y1, x1, y1};
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, Float32Array.from(vertices), GLES20.GL_STATIC_DRAW);
//        GLES20.glVertexAttribPointer(ShaderUtils.VERTEX_ARRAY, 2, GLES20.GL_FLOAT, false, 0, 0);
//        GLES20.glEnableVertexAttribArray(ShaderUtils.VERTEX_ARRAY);
//
//        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
    }

    @Override
    public ImageHolder<OpenGLTexture> loadImage(int width, int height) {
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
                log.debug("createSurface["+handle+"]: Creating Framebuffer: " + width + "x" + height);
                OpenGLSurface.get(h.get()).createSurface();
                setSurface(h);
            }
        });
        return h;
    }

    void setSurface(ImageHolder<? extends OpenGLTexture> t) {
        assert t != null;
        assert t.get() != null;
        // we are the current surface
//        if (currentSurface!=null && (t == currentSurface || t.getHandle() == currentSurface.getHandle())) {
//            if (logFrameBuffer) log.debug("Setting Surface to our self: {}", t.getHandle());
//            return;
//        }

        // now we can unbind the fb..
//        if (currentSurface != null) {
//            System.out.println("Unbinding Surface: " + currentSurface.getHandle());
//            OpenGLSurface.get(currentSurface.get()).unbind();
//        }

        // bind it (is, SetTargetSurface) and set it up
        OpenGLSurface surface = OpenGLSurface.get(t.get());
        surface.bind();
        currentSurface=t;
        log.debug("Set Surface: {}", currentSurface.getHandle());
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
    public ImageHolder<OpenGLTexture> readImage(final File file) throws Exception {
        long st = System.currentTimeMillis();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inDither=true;
        //options.inDensity=32;
        //options.inTargetDensity=32;
        options.inPurgeable=true;

        // TODO: we need to do a smarter decode on the image stream.  OpenGL cannot load images > 2048 pixels
        final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        //bitmap.setHasAlpha(true);
        //bitmap.setDensity(32);

        //bitmap.

        long time = System.currentTimeMillis() - st;
        totalTextureTime += time;
        longestTextureTime = Math.max(time, longestTextureTime);

        final OpenGLTexture t = new OpenGLTexture(bitmap.getWidth(), bitmap.getHeight());

        invokeLater(new Runnable() {
            @Override
            public void run() {
                t.set(bitmap, file.getName());
            }
        });
        return new ImageHolder<>(t, t.width, t.height);
    }

    @Override
    public ImageHolder<OpenGLTexture> readImage(InputStream fis) throws Exception {
        long st = System.currentTimeMillis();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        // TODO: we need to do a smarter decode on the image stream.  OpenGL cannot load images > 2048 pixels
        final Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);

        long time = System.currentTimeMillis() - st;
        totalTextureTime += time;
        longestTextureTime = Math.max(time, longestTextureTime);

        final OpenGLTexture t = new OpenGLTexture(bitmap.getWidth(), bitmap.getHeight());

        invokeLater(new Runnable() {
            @Override
            public void run() {
                t.set(bitmap, "stream");
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
        ((GLSurfaceView) activity.getUIView()).requestRender();

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
        synchronized (renderQueue) {
            log.debug("Blocked Start Frame until Render Frame Complete");
        }
        frameTime = System.currentTimeMillis();
        totalTextureTime = 0;
        longestTextureTime = 0;
        state = STATE_MENU;
        if (firstFrame) {
//            invokeLater(new Runnable() {
//                @Override
//                public void run() {
//                    log.debug("Start Frame: Setting Main Surface");
//                    setSurface(mainSurface);
//                    clearUI();
//                }
//            });
        }
        inFrame=true;
    }

    void clearUI() {
//        Gdx.gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        // must be set to 0,0,0,0 or else overlay on video does not work
        ShaderUtils.logGLErrors("before clearUI");
        GLES20.glClearColor(1, 0, 0, 0);
        ShaderUtils.logGLErrors("clearUI 0");

//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        ShaderUtils.logGLErrors("clearUI 1");
        GLES20.glEnable(GL10.GL_DEPTH_TEST);
        ShaderUtils.logGLErrors("clearUI 2");
        GLES20.glEnable(GL10.GL_TEXTURE_2D);
        ShaderUtils.logGLErrors("clearUI 4");
        GLES20.glEnable(GL10.GL_LINE_SMOOTH);
        ShaderUtils.logGLErrors("clearUI 5");
        GLES20.glDepthFunc(GL10.GL_LEQUAL);
        ShaderUtils.logGLErrors("clearUI 6");
        GLES20.glClearDepthf(1.0F);
        ShaderUtils.logGLErrors("clearUI 7");
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
        if (!useNativeResolution) {
            Dimension real = getMaxScreenSize();
            return new Dimension(real.width / 2, real.height / 2);
        } else {
            Dimension newSize = new Dimension(getMaxScreenSize());
            return newSize;
        }
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
    public void setVideoBounds(Rectangle o, Rectangle o1) {
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

// this is just for testing static drawing...
//        GLES20.glViewport(0,0, uiSize.width, uiSize.height);
//
//        GLES20.glClearColor(1.0f, 0.0f, 0f, 1.0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//
//        float viewMatrix[] = new float[16];
//        Matrix.orthoM(viewMatrix, 0,0, uiSize.width, uiSize.height, 0, 0, 1);
//        float triangleCoords[] = {   // in counterclockwise order:
//                uiSize.width/2,  0f, 0.0f, // top
//                0f, uiSize.height, 0.0f, // bottom left
//                uiSize.width, uiSize.height, 0.0f  // bottom right
//        };
//        Triangle tr=  new Triangle(triangleCoords);
//        tr.draw(viewMatrix);
//
//        Bitmap bmp = BitmapFactory.decodeResource(activity.getContext().getResources(), R.drawable.sage_logo_256);
//
//        OpenGLTexture t = new OpenGLTexture(0,0);
//        t.set(bmp, "sage64");
//        t.draw(viewMatrix);
//
//        System.out.println("Frame Rendered: " + uiSize);
    }
}
