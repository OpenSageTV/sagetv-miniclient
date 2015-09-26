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
    Stage stage;
    Batch batch;
    Camera camera;
    Viewport viewport;

    // logging stuff
    boolean logFrameTime=true;
    boolean logTextureTime=true;
    long longestTextureTime=0;
    long totalTextureTime=0;

    long frameTime = 0;
    long frameOps=0;
    long frame=0;

    boolean firstFrame=true;

    private Dimension size;
    private List<Runnable> renderQueue = new ArrayList<>();
    private List<Runnable> frameQueue = new ArrayList<>();
    private MiniClientConnection connection;

    // the pipeline is synchonous so only one operations can affect this at a time
    private Color batchColor=null;

    Scale scale = new Scale(1,1);
    private ShapeRenderer shapeRenderer;

    public MiniClientRenderer(MiniClientGDXActivity parent) {
        this.activity=parent;
    }

    @Override
    public void create() {
        Dimension size = getMaxScreenSize();
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
        stage.getViewport().update(width, height, true);
        System.out.println("SIZE: width: " + width + "; height: " + height);
        System.out.println("VIEWPORT: width: " + stage.getViewport().getScreenWidth() + "; height: " + stage.getViewport().getScreenHeight());
        System.out.println("VIEWPORT: x: " + stage.getViewport().getScreenX() + "; y: " + stage.getViewport().getScreenY());
        System.out.println("WORLD: width: " + stage.getViewport().getWorldWidth() + "; height: " + stage.getViewport().getWorldHeight());
        // scale = getStage().getViewport().getWorldHeight()/ getStage().getViewport().getScreenHeight();
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

    private Color lastColor=null;
    private Color getColor(int color) {
        return new Color(((color >> 16) & 0xFF)/255f, ((color >> 8) & 0xFF)/255f, ((color) & 0xFF)/255f, ((color >> 24) & 0xFF)/255f);
    }

    @Override
    public void drawTexture(final int x, final int y, final int width, final int height, int handle, final ImageHolder<GdxTexture> img, final int srcx, final int srcy, final int srcwidth, final int srcheight, final int blend) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
                if (height < 0) {
                    GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);
                }

                batchColor = batch.getColor();
                batch.setColor(getColor(blend));

                int w=Math.abs(width);
                int h=Math.abs(height);
                Texture t = img.get().texture();
                batch.draw(t, x, size.getHeight()-y-h, w, h, srcx, srcy, srcwidth, srcheight, false, false);
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
    public ImageHolder<GdxTexture> loadImage(int width, int height) {
        return null;
    }

    @Override
    public void unloadImage(int handle, ImageHolder<GdxTexture> bi) {

    }

    @Override
    public ImageHolder<GdxTexture> createSurface(int handle, int width, int height) {
        return null;
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
    public void setTargetSurface(int handle, ImageHolder<GdxTexture> image) {

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
            //size = new Dimension(720,480);
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
