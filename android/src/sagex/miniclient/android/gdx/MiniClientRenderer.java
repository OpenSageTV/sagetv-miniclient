package sagex.miniclient.android.gdx;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import sagex.miniclient.MiniClient;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.android.gl.EGLTexture;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.ImageHolder;
import sagex.miniclient.uibridge.Scale;
import sagex.miniclient.uibridge.UIManager;

/**
 * Created by seans on 26/09/15.
 */
public class MiniClientRenderer implements ApplicationListener, UIManager<GdxTexture> {
    private static final String TAG = "GDXMINICLIENT";
    private final MiniClientGDXActivity activity;

    // GDX stuff
    Stage stage;
    Batch batch;
    Camera camera;
    Viewport viewport;
    ShapeRenderer shapeRenderer;

    // logging stuff
    boolean logFrameBuffer=true;
    boolean logFrameTime=false;
    boolean logTextureTime=false;
    long longestTextureTime=0;
    long totalTextureTime=0;
    long frameTime = 0;
    long frameOps=0;
    long frame=0;
    boolean firstFrame=true;
    boolean ready=false;

    Dimension size;
    Scale scale = new Scale(1,1);

    // render queues
    private List<Runnable> renderQueue = new ArrayList<>();
    private List<Runnable> frameQueue = new ArrayList<>();

    // communication with SageTV
    private MiniClientConnection connection;

    // the pipeline is synchonous so only one operations can affect this at a time
    private Color batchColor=null;

    // Current Surface (when surfaces are enabled)
    GdxTexture currentSurface=null;

    public MiniClientRenderer(MiniClientGDXActivity parent) {
        this.activity=parent;
    }

    @Override
    public void create() {
        Dimension size = getMaxScreenSize();
        Log.d(TAG, "CREATE: UI SIZE: " + size);
        camera = new OrthographicCamera(size.getWidth(), size.getHeight());
        viewport = new StretchViewport(size.getWidth(), size.getHeight(), camera);
        stage = new Stage(viewport);
        batch=stage.getBatch();
        shapeRenderer = new ShapeRenderer();
        Gdx.graphics.setContinuousRendering(false);
    }

    @Override
    public void resize(int width, int height) {
        this.size.width=width;
        this.size.height=height;
        this.scale.setScale(1f * width / Gdx.graphics.getWidth(), 1f * height / Gdx.graphics.getHeight());

        stage.getViewport().setWorldSize(width, height);
        stage.getViewport().update(width, height, true);
        Log.d(TAG, "SIZE: width: " + width + "; height: " + height);
        Log.d(TAG, "VIEWPORT: width: " + stage.getViewport().getScreenWidth() + "; height: " + stage.getViewport().getScreenHeight());
        Log.d(TAG, "VIEWPORT: x: " + stage.getViewport().getScreenX() + "; y: " + stage.getViewport().getScreenY());
        Log.d(TAG, "WORLD: width: " + stage.getViewport().getWorldWidth() + "; height: " + stage.getViewport().getWorldHeight());
        Log.d(TAG, "SCALE: " + scale);
                // scale = getStage().getViewport().getWorldHeight()/ getStage().getViewport().getScreenHeight();

        Log.d(TAG, "Notifying SageTV about the Resize Event: " + this.size);
        connection.postResizeEvent(size);
        ready=true;
    }

    @Override
    public void render() {
        if (renderQueue.size()==0) return;

        long st=System.currentTimeMillis();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        if (batch != null) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            synchronized (renderQueue) {
                for (Runnable r : renderQueue) {
                    r.run();
                }
                renderQueue.clear();
            }
            batch.end();
        }
        long et=System.currentTimeMillis();
        if (logFrameTime) {
            Log.d(TAG, "RENDER: Time: " + (et-st) + "ms");
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
        while (!ready) {
            Log.d(TAG, "Waiting For UI...");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

    @Override
    public void GFXCMD_DEINIT() {
        activity.finish();
    }

    @Override
    public void close() {
        GFXCMD_DEINIT();
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
                shapeRenderer.rect(x, Y(y,height), width, height, getColor(argbTL), getColor(argbTR), getColor(argbBR), getColor(argbBL));
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
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.rect(x, Y(y, height), width, height, getColor(argbTL), getColor(argbTR), getColor(argbBR), getColor(argbBL));
                shapeRenderer.end();

                batch.begin();
            }
        });
    }

    @Override
    public void clearRect(final int x, final int y, final int width, final int height, final int argbTL, final int argbTR, final int argbBR, final int argbBL) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                batch.end();

                camera.update();
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.rect(x, Y(y, height), width, height, getColor(argbTL), getColor(argbTR), getColor(argbBR), getColor(argbBL));
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
                shapeRenderer.circle(x,y,height/2);
                shapeRenderer.end();

                batch.begin();
            }
        });
    }

    @Override
    public void fillOval(final int x, final int y, int width, final int height, final int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        // TODO: make ovals
        invokeLater(new Runnable() {
            @Override
            public void run() {
                batch.end();

                camera.update();
                shapeRenderer.setColor(getColor(argbTL));
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.circle(x,y,height/2);
                shapeRenderer.end();

                batch.begin();
            }
        });
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int thickness, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        // TODO: make it rounded (Maybe use Canvas and get bitmap as texture... Not efficient, but not used a lot either)
        drawRect(x,y,width,height,thickness,argbTL,argbTR,argbBR,argbBL);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        // TODO: make it rounded
        fillRect(x, y, width, height, argbTL, argbTR, argbBR, argbBL);
    }

    // Because getColor() is only called on the render queue, in sequence we can do this
    private Color lastColor=null;
    private int lastColorInt=-1;
    private Color getColor(int color) {
        if (lastColor!=null && color==lastColorInt) return lastColor;
        lastColorInt=color;
        lastColor = new Color(((color >> 16) & 0xFF)/255f, ((color >> 8) & 0xFF)/255f, ((color) & 0xFF)/255f, ((color >> 24) & 0xFF)/255f);
        return lastColor;
    }

    @Override
    public void drawTexture(final int x, final int y, final int width, final int height, int handle, final ImageHolder<GdxTexture> img, final int srcx, final int srcy, final int srcwidth, final int srcheight, final int blend) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                if (img==null) return;

                // we only want to set blending for Non Framebuffer textures
                if (!img.get().isFrameBuffer) {
                    GLES20.glEnable(GLES20.GL_BLEND);
                    GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
                    if (height < 0) {
                        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);
                    }

                    batchColor = batch.getColor();
                    batch.setColor(getColor(blend));
                }

                int w=Math.abs(width);
                int h=Math.abs(height);
                Texture t = img.get().texture();
                batch.draw(t, x, Y(y,h), w, h, srcx, srcy, srcwidth, srcheight, false, img.get().isFrameBuffer);
                batch.setColor(batchColor);
                GLES20.glDisable(GLES20.GL_BLEND);
            }
        });
    }
    float Y(int y, int height) {
        return size.getHeight()-y-height;
    }
    float Y(int y) {
        return size.getHeight()-y;
    }

    @Override
    public ImageHolder<GdxTexture> loadImage(int width, int height) {
        return null;
    }

    @Override
    public void unloadImage(int handle, ImageHolder<GdxTexture> bi) {
        if (bi!=null) {
            bi.get().dispose();
        }
    }

    @Override
    public ImageHolder<GdxTexture> createSurface(int handle, int width, int height) {
        // need to create a FrameBuffer object
        // need to create a SpriteBatch and the set the main batch to use it
        // need to keep a reference to the previous batch
        // need to set the MAIN surface to be this fbo
        if (logFrameBuffer) Log.d(TAG, "Creating Framebuffer["+handle+"]: " + width + "x" + height);
        final GdxTexture t = new GdxTexture(width,height);
        ImageHolder<GdxTexture> h = new ImageHolder<>(t);
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
        if (currentSurface!=null) {
            t.unbindFrameBuffer();
        }
        // close the batch in prep for a new surface
        batch.end();

        // bind it (is, SetTargetSurface)
        t.bindFrameBuffer();
        currentSurface=t;

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
                if (handle==0) {
                    // we are switching to the primary surface (screen)
                    if (currentSurface!=null) {
                        if (logFrameBuffer) Log.d(TAG, "Unbinding Old Framebuffer");
                        batch.end();
                        currentSurface.unbindFrameBuffer();
                        currentSurface=null;

                        // reset the camera and batch
                        camera.update();
                        batch.setProjectionMatrix(camera.combined);
                        batch.begin();
                    } else {
                        // nothing to do, we are being told to set the surface to 0, but, we are 0
                    }
                } else {
                    if (logFrameBuffer) Log.d(TAG, "Using Framebuffer["+handle+"]");
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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

        long time = System.currentTimeMillis()-st;
        totalTextureTime+=time;
        longestTextureTime=Math.max(time,longestTextureTime);

        final GdxTexture t = new GdxTexture(bitmap);
        invokeLater(new Runnable() {
            @Override
            public void run() {
                t.load();
            }
        });
        return new ImageHolder<>(t);
    }

    @Override
    public ImageHolder<GdxTexture> newImage(int destWidth, int destHeight) {
        return null;
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
            Log.d(TAG, "FRAME: " + (frame) + "; Time: " + (System.currentTimeMillis() - frameTime) + "ms; Ops: " + frameOps);
        }
        if (logTextureTime) {
            Log.d(TAG, "FRAME: " + (frame) + "; Texture Load Time: " + totalTextureTime + "ms; Longest Single: " + longestTextureTime + "ms");
        }
        frame++;
    }

    @Override
    public void startFrame() {
        frameTime=System.currentTimeMillis();
        frameOps=0;
        totalTextureTime=0;
        longestTextureTime=0;
    }
    @Override
    public void loadImageLine(int handle, ImageHolder<GdxTexture> image, int line, int len2, byte[] cmddata) {

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
        if (size==null) {
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            this.size = new Dimension(size.x, size.y);
        }
        return size;
    }

    @Override
    public Dimension getScreenSize() {
        return getMaxScreenSize();
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

    public void setConnection(MiniClientConnection connection) {
        this.connection = connection;
    }
}
