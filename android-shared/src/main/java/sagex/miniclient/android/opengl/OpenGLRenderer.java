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
import java.io.FileInputStream;
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
import sagex.miniclient.android.ui.AndroidUIController;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.android.video.exoplayer2.Exo2MediaPlayerImpl;
import sagex.miniclient.android.video.ijkplayer.IJKMediaPlayerImpl;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.ImageHolder;
import sagex.miniclient.uibridge.Rectangle;
import sagex.miniclient.uibridge.Scale;
import sagex.miniclient.uibridge.Texture;
import sagex.miniclient.uibridge.UIRenderer;
import sagex.miniclient.util.AspectHelper;
import sagex.miniclient.util.VerboseLogging;
import sagex.miniclient.video.HasVideoInfo;
import sagex.miniclient.video.VideoInfoResponse;

public class OpenGLRenderer implements UIRenderer<OpenGLTexture>, GLSurfaceView.Renderer {
    private static final Logger log = LoggerFactory.getLogger(OpenGLRenderer.class);


    private final AndroidUIController activity;
    private final MiniClient client;

    // UI states, just MENU (default) and VIDEO.
    int state = STATE_MENU;

    // logging stuff
    boolean logFrameBuffer = VerboseLogging.DETAILED_GFX_TEXTURES;
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
    // Current Surface (when surfaces are enabled)
    ImageHolder<OpenGLTexture> currentSurface = null;
    OpenGLSurface mainSurface = null;

    // render queues
    final private List<Runnable> renderQueue = new ArrayList<>();
    private List<Runnable> frameQueue = new ArrayList<>();

    private MiniPlayerPlugin player;
    // Screen and UI resolutions
    // Total available screan pixels that we have
    Dimension fullScreenSize = new Dimension(0, 0);

    // UI size is the size of the UI that is built, will be scaled up to the screenSize
    // this is the size of the UI that sagetv will build and send to us
    Dimension uiSize = new Dimension(0, 0);

    // if true, the uiSize is set to the Native resolution
    boolean useNativeResolution = true;

    // the scaleAndCenterImmutable of the uiSize to the screenSize
    Scale scale = new Scale(1, 1);

    private Dimension lastResize = new Dimension(0, 0);
    private Dimension lastScreenSize = new Dimension(0, 0);
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

        uiSize.setSize(1280, 720);
        lastScreenSize.updateFrom(uiSize);

        scale.setScale(uiSize, fullScreenSize);

        log.debug("CREATE: UI SIZE: {}, SCREEN SIZE: {}", uiSize, fullScreenSize);

        // load shaders and programs
        ShaderUtils.createPrograms();

        // create the main surface
        mainSurface = new OpenGLSurface(uiSize.width, uiSize.height);
        mainSurface.createSurface();

//        camera = new OrthographicCamera();
//        viewport = new StretchViewport(uiSize.getWidth(), uiSize.getHeight(), camera);
//        stage = new Stage(viewport);
//        batch = stage.getBatch();
//
//        camera.update();
//        batch.setProjectionMatrix(camera.combined);
//
//        shapeRenderer = new SageShapeRenderer();
//        Gdx.graphics.setContinuousRendering(false);
    }

    public void resize(int width, int height) {
        if (!firstResize && lastResize.equals(width, height) && lastScreenSize.equals(getScreenSize())) {
            log.debug("Resize Already Happened, Ignoring this: {}x{}", width, height);
            return;
        }

        fullScreenSize.update(width, height);
        lastResize.update(width, height);

// this seems right but causes a UI shift??
//        if (!useNativeResolution) {
//            uiSize.width = fullScreenSize.width / 2;
//            uiSize.height = fullScreenSize.height / 2;
//        } else {
//            uiSize.width=fullScreenSize.width;
//            uiSize.height=fullScreenSize.height;
//        }

        uiSize.updateFrom(getScreenSize());

        lastScreenSize.updateFrom(uiSize);

        this.scale.setScale(uiSize, fullScreenSize);

//        stage.getViewport().setWorldSize(uiSize.width, uiSize.height);
//        stage.getViewport().update(fullScreenSize.width, fullScreenSize.height, true);
//        log.debug("RESIZE SCREEN: width: " + fullScreenSize.width + "; height: " + fullScreenSize.height);
//        log.debug("VIEWPORT: width: " + stage.getViewport().getScreenWidth() + "; height: " + stage.getViewport().getScreenHeight());
//        log.debug("VIEWPORT: x: " + stage.getViewport().getScreenX() + "; y: " + stage.getViewport().getScreenY());
//        log.debug("WORLD: width: " + stage.getViewport().getWorldWidth() + "; height: " + stage.getViewport().getWorldHeight());
//        log.debug("SCALE: " + scale);
//
//        camera.update();
//        batch.setProjectionMatrix(camera.combined);

        ready = true;
        notifySageTVAboutScreenSize();
    }

    public void notifySageTVAboutScreenSize() {
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
        //if (batch==null) return;
        int size=renderQueue.size();
        if (size==0) return;

        long st = System.currentTimeMillis();

        synchronized (renderQueue) {
            try {
                //batch.begin();
                //batch.setColor(Color.BLACK);
                for (int i=0;i<size;i++) {
                    renderQueue.get(i).run();
                }
            } catch (Throwable t) {
                log.error("Render Failed.  This should never happen.  Developer should figure out why", t);
                // TODO: How should we manage this.. request a re-render??
            } finally {
                //batch.end();
                renderQueue.clear();
            }
        }

        glFlipBuffer();
        
        long et = System.currentTimeMillis();
        if (logFrameTime) {
            log.debug("RENDER: Time: " + (et - st) + "ms; Ops: " + size);
        }
    }

    private void glFlipBuffer() {
            // Flip code...
            float viewMatrix[] =
                    {
                            2.0f / (float)mainSurface.width, 0.0f, 0.0f, 0.0f,
                            0.0f, -2.0f / (float)mainSurface.height, 0.0f, 0.0f,
                            0.0f, 0.0f, 1.0f, 0.0f,
                            -1.0f, 1.0f, 0.0f, 1.0f
                    };

            //GLES20.viewport(0,0,WINDOW_WIDTH, WINDOW_HEIGHT);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            ShaderUtils.useProgram(ShaderUtils.Shader.Base);
            //GLES20.glUseProgram(baseTexturedProgram);
            GLES20.glUniformMatrix4fv(ShaderUtils.BASE_PROGRAM_PMVMatrix_Location, viewMatrix.length, false, viewMatrix, 0);

            GLES20.glDisable(GLES20.GL_BLEND);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mainSurface.texture());

            float pVertices2[] = {0,0, mainSurface.width,
                    0, mainSurface.width, mainSurface.height, mainSurface.width,
                    mainSurface.height, 0, mainSurface.height, 0, 0};

            GLES20.glUniform4fv(ShaderUtils.TEXTURE_PROGRAM_Color_Location, 4, ShaderUtils.rgbToFloatArray(0xff, 0xff, 0xff, 0xff), 0);

            int vertBuffer = ShaderUtils.glCreateBuffer();
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertBuffer);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, pVertices2.length, FloatBuffer.wrap(pVertices2), GLES20.GL_STATIC_DRAW);
            GLES20.glVertexAttribPointer(ShaderUtils.VERTEX_ARRAY, 2, GLES20.GL_FLOAT, false, 0, 0);

            float pCoords2[] = {0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f};
            int coordBuffer = ShaderUtils.glCreateBuffer();
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, coordBuffer);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, pCoords2.length, FloatBuffer.wrap(pCoords2), GLES20.GL_STATIC_DRAW);
            GLES20.glVertexAttribPointer(ShaderUtils.COORD_ARRAY, 2, GLES20.GL_FLOAT, false, 0,0);

            GLES20.glEnableVertexAttribArray(ShaderUtils.VERTEX_ARRAY);
            GLES20.glEnableVertexAttribArray(ShaderUtils.COORD_ARRAY);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

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
    public void drawTexture(int x, int y, int width, int height, int handle, ImageHolder<OpenGLTexture> img, int srcx, int srcy, int srcwidth, int srcheight, int blend) {

    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int argb1, int argb2) {

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
    public ImageHolder<OpenGLTexture> createSurface(int handle, int width, int height) {
        if (logFrameBuffer)
            log.debug("createSurface["+handle+"]: Creating Framebuffer: " + width + "x" + height);
        final OpenGLTexture t = new OpenGLSurface(width, height);
        final ImageHolder<OpenGLTexture> h = new ImageHolder<>(t, width, height);
        h.setHandle(handle);
        invokeLater(new Runnable() {
            @Override
            public void run() {
                OpenGLSurface.get(h.get()).createSurface();
                setupSurface(h);
            }
        });
        return h;
    }

    void setupSurface(ImageHolder<OpenGLTexture> t) {
        // we are the current surface
        if (currentSurface!=null && (t == currentSurface || t.getHandle() == currentSurface.getHandle())) {
            if (logFrameBuffer) log.debug("Setting Surface to ourself: {}", t.getHandle());
            return;
        }

        // close the batch in prep for a new surface, flushed gl
        // batch.end();

        // now we can unbind the fb..
        if (currentSurface != null) {
            //currentSurface.get().unbindFrameBuffer();
        }

        // bind it (is, SetTargetSurface) and set it up
        //t.get().bindFrameBuffer();

        // start the batch in prep for new surface
        //camera.update();
        //batch.setProjectionMatrix(camera.combined);
        //batch.begin();

        // set the current surface
        currentSurface = t;
    }

    @Override
    public void setTargetSurface(final int handle, final ImageHolder<OpenGLTexture> image) {
        // info on Gdx framebufffer
        // http://stackoverflow.com/questions/24434236/libgdx-framebuffer
        // https://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/glutils/FrameBuffer.html

        // set the main batch to be this fbo
        // In Draw Texture, if we are asked to render to FB, then we need to get the FB texture and render that
        invokeLater(new Runnable() {
            @Override
            public void run() {
                if (handle == 0) {
                    // we are switching to the primary surface (screen)
                    if (currentSurface != null) {
                        if (logFrameBuffer) log.debug("setTargetSurface[{}, {}]: Unbinding Old Framebuffer: {}", handle, (image!=null)?image.getHandle():null, currentSurface.getHandle());
                        //batch.end();
                        //currentSurface.get().unbindFrameBuffer();
                        currentSurface = null;
//
                        // reset the camera and batch
                        //camera.update();
                        //batch.setProjectionMatrix(camera.combined);
                        //batch.begin();
                    } else {
                        if (logFrameBuffer) log.debug("setTargetSurface[{},{}]", handle, (image!=null)?image.getHandle():null);
                        // nothing to do, we are being told to set the surface to 0, but, we are 0
                    }
                } else {
                    if (logFrameBuffer) log.debug("setTargetSurface[{},{}]", handle, (image!=null)?image.getHandle():null);
                    setupSurface(image);
                }
            }
        });
    }

    @Override
    public ImageHolder<OpenGLTexture> readImage(File file) throws Exception {
        try {
            FileInputStream fis = new FileInputStream(file);
            try {
                //return readImage(fis);
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
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        final Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);

        long time = System.currentTimeMillis() - st;
        totalTextureTime += time;
        longestTextureTime = Math.max(time, longestTextureTime);

//        final GdxTexture t = new GdxTexture(bitmap);
//        invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                t.load();
//            }
//        });
//        return new ImageHolder<>(t, t.width, t.height);
        return null;
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
        frameTime = System.currentTimeMillis();
        totalTextureTime = 0;
        longestTextureTime = 0;
        state = STATE_MENU;
        if (firstFrame) {
            invokeLater(new Runnable() {
                @Override
                public void run() {
                    clearUI();
                }
            });
        }
        inFrame=true;
    }

    void clearUI() {
//        Gdx.gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        // must be set to 0,0,0,0 or else overlay on video does not work
        //Gdx.gl20.glClearColor(0, 0, 0, 0);
        //Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT | GL20.GL_COVERAGE_BUFFER_BIT_NV);
//        Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
//        Gdx.gl.glEnable(GL10.GL_TEXTURE);
//        Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
//        Gdx.gl.glEnable(GL10.GL_LINE_SMOOTH);
//        Gdx.gl.glDepthFunc(GL10.GL_LEQUAL);
//        Gdx.gl.glClearDepthf(1.0F);
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
        //GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        create();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //GLES20.glViewport(0, 0, width, height);
        resize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        render();
    }
}
