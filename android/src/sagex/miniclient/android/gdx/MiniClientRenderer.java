package sagex.miniclient.android.gdx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import sagex.miniclient.MiniClient;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.android.video.exoplayer.ExoMediaPlayerImpl;
import sagex.miniclient.android.video.ijkplayer.IJKMediaPlayerImpl;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.ImageHolder;
import sagex.miniclient.uibridge.Scale;
import sagex.miniclient.uibridge.UIRenderer;

/**
 * Created by seans on 26/09/15.
 */
public class MiniClientRenderer implements ApplicationListener, UIRenderer<GdxTexture> {
    private static final Logger log = LoggerFactory.getLogger(MiniClientRenderer.class);
    
    private final MiniClientGDXActivity activity;
    private final MiniClient client;

    // Screen and UI resolutions
    // Total available screan pixels that we have
    Dimension fullScreenSize = null;

    // UI size is the size of the UI that is built, will be scaled up to the screenSize
    // this is the size of the UI that sagetv will build and send to us
    Dimension uiSize = new Dimension(1280, 720);

    // if true, the uiSize is set to the Native resolution
    boolean useNativeResolution = true;

    // the scale of the uiSize to the screenSize
    Scale scale = new Scale(1, 1);


    // GDX stuff
    Stage stage;
    Batch batch;
    Camera camera;
    Viewport viewport;
    ShapeRenderer shapeRenderer;
    // logging stuff
    boolean logFrameBuffer = false;
    boolean logFrameTime = false;
    boolean logTextureTime = false;
    long longestTextureTime = 0;
    long totalTextureTime = 0;
    long frameTime = 0;
    long frameOps = 0;
    long frame = 0;
    boolean firstFrame = true;
    boolean ready = false;
    // Current Surface (when surfaces are enabled)
    GdxTexture currentSurface = null;
    // render queues
    final private List<Runnable> renderQueue = new ArrayList<>();
    private List<Runnable> frameQueue = new ArrayList<>();
    // the pipeline is synchonous so only one operations can affect this at a time
    private Color batchColor = null;
    // Because getColor() is only called on the render queue, in sequence we can do this
    private Color lastColor = null;
    private int lastColorInt = -1;

    private MiniPlayerPlugin player;

    boolean resizeHappened = false;

    public MiniClientRenderer(MiniClientGDXActivity parent, MiniClient client) {
        this.activity = parent;
        this.client = client;
        client.setUIRenderer(this);
    }

    public static int readInt(int pos, byte[] cmddata) {
        pos += 4; // for the 4 bytes for the header
        return ((cmddata[pos] & 0xFF) << 24) | ((cmddata[pos + 1] & 0xFF) << 16) | ((cmddata[pos + 2] & 0xFF) << 8) | (cmddata[pos + 3] & 0xFF);
    }

    @Override
    public void create() {
        fullScreenSize = getMaxScreenSize();
        uiSize = getScreenSize();
        scale.setScale(uiSize, fullScreenSize);

        log.debug("CREATE: UI SIZE: {}, SCREEN SIZE: {}", uiSize, fullScreenSize);
        camera = new OrthographicCamera();
        viewport = new StretchViewport(uiSize.getWidth(), uiSize.getHeight(), camera);
        stage = new Stage(viewport);
        batch = stage.getBatch();
        shapeRenderer = new ShapeRenderer();
        Gdx.graphics.setContinuousRendering(false);
    }

    @Override
    public void resize(int width, int height) {
        if (resizeHappened) {
            log.debug("Resize Already Happened, Ignoring this: {}x{}", width, height);
            return;
        }

        fullScreenSize.width = width;
        fullScreenSize.height = height;

        this.scale.setScale(uiSize, fullScreenSize);

        stage.getViewport().setWorldSize(uiSize.width, uiSize.height);
        stage.getViewport().update(width, height, true);
        log.debug("SIZE: width: " + width + "; height: " + height);
        log.debug("VIEWPORT: width: " + stage.getViewport().getScreenWidth() + "; height: " + stage.getViewport().getScreenHeight());
        log.debug("VIEWPORT: x: " + stage.getViewport().getScreenX() + "; y: " + stage.getViewport().getScreenY());
        log.debug("WORLD: width: " + stage.getViewport().getWorldWidth() + "; height: " + stage.getViewport().getWorldHeight());
        log.debug("SCALE: " + scale);

        ready = true;
        if (client == null || client.getCurrentConnection() == null) {
            return;
        }

        notifySageTVAboutScreenSize();
    }

    public void notifySageTVAboutScreenSize() {
        log.debug("Notifying SageTV about the Resize Event: " + this.uiSize);
        try {
            client.getCurrentConnection().postResizeEvent(uiSize);
            resizeHappened = true;
        } catch (Throwable t) {
            log.info("Error sending Resize Event", t);
        }
    }

    @Override
    public void render() {
        if (renderQueue.size() == 0) return;

        long st = System.currentTimeMillis();


//        Gdx.gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        // must be set to 0,0,0,0 or else overlay on video does not work
        Gdx.gl20.glClearColor(0, 0, 0, 0);
        //Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
//        Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
//        Gdx.gl.glEnable(GL10.GL_TEXTURE);
//        Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
//        Gdx.gl.glEnable(GL10.GL_LINE_SMOOTH);
//        Gdx.gl.glDepthFunc(GL10.GL_LEQUAL);
//        Gdx.gl.glClearDepthf(1.0F);

        camera.update();

        if (batch != null) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            batch.setColor(Color.BLACK);
            synchronized (renderQueue) {
                try {
                    for (Runnable r : renderQueue) {
                        r.run();
                    }
                } catch (Throwable t) {
                    log.error("Render Failed.  This should never happen.  Developer should figure out why", t);
                    // TODO: How should we manage this.. request a re-render??
                }
                renderQueue.clear();
            }
            batch.end();
        }


        long et = System.currentTimeMillis();
        if (logFrameTime) {
            log.debug("RENDER: Time: " + (et - st) + "ms");
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
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
    public void drawLine(final int x1, final int y1, final int x2, final int y2, final int argb1, final int argb2) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                batch.end();

                camera.update();
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.line(x1, Y(y1), x2, Y(y2), getColor(argb1), getColor(argb2));
                shapeRenderer.end();

                batch.begin();
            }
        });
    }

    @Override
    public void drawRect(final int x, final int y, final int width, final int height, int thickness, final int argbTL, final int argbTR, final int argbBR, final int argbBL) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                batch.end();

                camera.update();
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.rect(x, Y(y, height), width, height, getColor(argbTL), getColor(argbTR), getColor(argbBR), getColor(argbBL));
                shapeRenderer.end();

                batch.begin();
            }
        });
    }

    @Override
    public void fillRect(final int x, final int y, final int width, final int height, final int argbTL, final int argbTR, final int argbBR, final int argbBL) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                batch.end();

                camera.update();
                Gdx.gl20.glEnable(GL20.GL_BLEND);
                Gdx.gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.rect(x, Y(y, height), width, height, getColor(argbTL), getColor(argbTR), getColor(argbBR), getColor(argbBL));
                shapeRenderer.end();

                Gdx.gl20.glDisable(GL20.GL_BLEND);

                batch.begin();
            }
        });
    }

    @Override
    public void clearRect(final int x, final int y, final int width, final int height, final int argbTL, final int argbTR, final int argbBR, final int argbBL) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                log.debug("*** CLEAR RECT ** {},{}", width, height);
                batch.end();

                camera.update();

                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.rect(x, Y(y, height), width, height, Color.CLEAR, Color.CLEAR, Color.CLEAR, Color.CLEAR);
                // clear rect full screen calls are taking long time... so clear full screen always
                // shapeRenderer.rect(0, Y(0, fullScreenSize.height), fullScreenSize.width, fullScreenSize.height, Color.CLEAR, Color.CLEAR, Color.CLEAR, Color.CLEAR);
                shapeRenderer.end();
                batch.begin();
            }
        });
    }

    @Override
    public void drawOval(final int x, final int y, final int width, final int height, int thickness, final int argbTL, final int argbTR, final int argbBR, final int argbBL, int clipX, int clipY, int clipW, int clipH) {
        // TODO: make ovals
        invokeLater(new Runnable() {
            @Override
            public void run() {
                batch.end();

                camera.update();
                shapeRenderer.setColor(getColor(argbTL));
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.ellipse(x, Y(y, height), width, height);
                //shapeRenderer.circle(x, y, height / 2);
                shapeRenderer.end();

                batch.begin();
            }
        });
    }

    @Override
    public void fillOval(final int x, final int y, final int width, final int height, final int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        // TODO: make ovals
        invokeLater(new Runnable() {
            @Override
            public void run() {
                batch.end();

                camera.update();
                shapeRenderer.setColor(getColor(argbTL));
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                //shapeRenderer.circle(x, y, height / 2);
                shapeRenderer.ellipse(x, Y(y, height), width, height);
                shapeRenderer.end();

                batch.begin();
            }
        });
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int thickness, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        // TODO: make it rounded (libgdx support arcs, curves, and lines, just need to figure out the right combination)
        drawRect(x, y, width, height, thickness, argbTL, argbTR, argbBR, argbBL);
    }

    @Override
    public void fillRoundRect(final int x, final int y, final int width, final int height, final int arcRadius, final int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
//        invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                batch.end();
//
//                camera.update();
//                shapeRenderer.setColor(getColor(argbTL));
//                shapeRenderer.setProjectionMatrix(camera.combined);
//                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//
//                shapeRenderer.rect(x, Y(y, height) + arcRadius, width, height - (2 * arcRadius));
//
//                shapeRenderer.rect(x + arcRadius, Y(y, height), width - (2 * arcRadius), height);
//
//                shapeRenderer.circle(x + arcRadius, Y(y, height) + arcRadius, arcRadius);
//
//                shapeRenderer.circle(x + arcRadius, Y(y, height) + height - arcRadius, arcRadius);
//
//                shapeRenderer.circle(x + width - arcRadius, Y(y, height) + arcRadius, arcRadius);
//
//                shapeRenderer.circle(x+width-arcRadius, Y(y,height)+height-arcRadius, arcRadius);
//
//                shapeRenderer.end();
//
//                batch.begin();
//            }
//        });
        fillRect(x, y, width, height, argbTL, argbTR, argbBR, argbBL);
    }

    private Color getColor(int color) {
        if (lastColor != null && color == lastColorInt) return lastColor;
        lastColorInt = color;
        lastColor = new Color(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, ((color) & 0xFF) / 255f, ((color >> 24) & 0xFF) / 255f);
        return lastColor;
    }

    @Override
    public void drawTexture(final int x, final int y, final int width, final int height, int handle, final ImageHolder<GdxTexture> img, final int srcx, final int srcy, final int srcwidth, final int srcheight, final int blend) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                if (img == null) return;

                // we only want to set blending for Non Framebuffer textures
//                if (!img.get().isFrameBuffer) {
                    Gdx.gl20.glEnable(GL20.GL_BLEND);
                    Gdx.gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
                    if (height < 0) {
                        Gdx.gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ZERO);
                    }

                    batchColor = batch.getColor();
                    batch.setColor(getColor(blend));
//                }

                int w = Math.abs(width);
                int h = Math.abs(height);
                Texture t = img.get().texture();
                batch.draw(t, x, Y(y, h), w, h, srcx, srcy, srcwidth, srcheight, false, img.get().isFrameBuffer);
//                if (!img.get().isFrameBuffer) {
                    batch.setColor(batchColor);
                    Gdx.gl20.glDisable(GL20.GL_BLEND);
//                }
            }
        });
    }

    float Y(int y, int height) {
        return uiSize.getHeight() - y - height;
    }

    float Y(int y) {
        return uiSize.getHeight() - y;
    }

    @Override
    public ImageHolder<GdxTexture> loadImage(int width, int height) {
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bm.setHasAlpha(true);
        bm.prepareToDraw();
        GdxTexture t = new GdxTexture(bm);
        return new ImageHolder<>(t, t.width, t.height);
    }

    @Override
    public void unloadImage(int handle, ImageHolder<GdxTexture> bi) {
        if (bi != null) {
            bi.get().dispose();
        }
    }

    @Override
    public ImageHolder<GdxTexture> createSurface(int handle, int width, int height) {
        // need to create a FrameBuffer object
        // need to create a SpriteBatch and the set the main batch to use it
        // need to keep a reference to the previous batch
        // need to set the MAIN surface to be this fbo
        if (logFrameBuffer)
            log.debug("Creating Framebuffer[" + handle + "]: " + width + "x" + height);
        final GdxTexture t = new GdxTexture(width, height);
        ImageHolder<GdxTexture> h = new ImageHolder<>(t, width, height);
        invokeLater(new Runnable() {
            @Override
            public void run() {
                t.load();
                setupSurface(t);
            }
        });
        return h;
    }

    void setupSurface(GdxTexture t) {
        if (currentSurface != null) {
            t.unbindFrameBuffer();
        }
        // close the batch in prep for a new surface
        batch.end();

        // bind it (is, SetTargetSurface)
        t.bindFrameBuffer();
        currentSurface = t;

        // start the batch in prep for new surface
        batch.begin();
    }

    @Override
    public void setTargetSurface(final int handle, final ImageHolder<GdxTexture> image) {
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
                        if (logFrameBuffer) log.debug("Unbinding Old Framebuffer");
                        batch.end();
                        currentSurface.unbindFrameBuffer();
                        currentSurface = null;

                        // reset the camera and batch
                        camera.update();
                        batch.setProjectionMatrix(camera.combined);
                        batch.begin();
                    } else {
                        // nothing to do, we are being told to set the surface to 0, but, we are 0
                    }
                } else {
                    if (logFrameBuffer) log.debug("Using Framebuffer[" + handle + "]");
                    setupSurface(image.get());
                }
            }
        });
    }

    @Override
    public ImageHolder<GdxTexture> readImage(File file) throws Exception {
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
    public ImageHolder<GdxTexture> readImage(InputStream fis) throws Exception {
        long st = System.currentTimeMillis();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        final Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);

        long time = System.currentTimeMillis() - st;
        totalTextureTime += time;
        longestTextureTime = Math.max(time, longestTextureTime);

        final GdxTexture t = new GdxTexture(bitmap);
        invokeLater(new Runnable() {
            @Override
            public void run() {
                t.load();
            }
        });
        return new ImageHolder<>(t, t.width, t.height);
    }

    @Override
    public ImageHolder<GdxTexture> newImage(int destWidth, int destHeight) {
        return loadImage(destWidth, destHeight);
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
        Gdx.graphics.requestRendering();
        if (logFrameTime) {
            log.debug("FRAME: " + (frame) + "; Time: " + (System.currentTimeMillis() - frameTime) + "ms; Ops: " + frameOps);
        }
        if (logTextureTime) {
            log.debug("FRAME: " + (frame) + "; Texture Load Time: " + totalTextureTime + "ms; Longest Single: " + longestTextureTime + "ms");
        }
        frame++;
    }

    @Override
    public void startFrame() {
        frameTime = System.currentTimeMillis();
        frameOps = 0;
        totalTextureTime = 0;
        longestTextureTime = 0;
    }

    @Override
    public void loadImageLine(int handle, ImageHolder<GdxTexture> image, int line, int len2, byte[] b) {
        Bitmap bm = image.get().bitmap;
        int datapos, x;
        for (datapos = 12, x = 0; x < len2 / 4; x++, datapos += 4) {
            bm.setPixel(x, line, readInt(datapos, b));
        }
    }

    @Override
    public void xfmImage(int srcHandle, ImageHolder<GdxTexture> srcImg, int destHandle, ImageHolder<GdxTexture> destImg, int destWidth, int destHeight, int maskCornerArc) {

    }

    @Override
    public boolean hasGraphicsCanvas() {
        return false;
    }

    @Override
    public Dimension getMaxScreenSize() {
        if (fullScreenSize == null) {
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            this.fullScreenSize = new Dimension(size.x, size.y);
        }
        return fullScreenSize;
    }

    @Override
    public Dimension getScreenSize() {
        if (useNativeResolution) {
            uiSize = getMaxScreenSize();
        }
        return uiSize;
    }

    @Override
    public void setFullScreen(boolean b) {

    }

    @Override
    public void setSize(int w, int h) {

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

        if (client.properties().getBoolean(PrefStore.Keys.use_exoplayer, false)) {
            log.debug("Using ExoPlayer");
            player = new ExoMediaPlayerImpl(activity);
        } else {
            log.debug("Using iJKPlayer");
            player = new IJKMediaPlayerImpl(activity);
        }
        return player;
    }

    @Override
    public void setVideoBounds(Object o, Object o1) {
        log.debug("Set Video Bounds:" + o + " - " + o1);
    }
}
