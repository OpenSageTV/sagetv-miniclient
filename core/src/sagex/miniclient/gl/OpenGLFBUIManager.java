package sagex.miniclient.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import sagex.miniclient.GFXCMD2;
import sagex.miniclient.ImageHolder;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.MiniClientConnectionGateway;
import sagex.miniclient.MiniClientMain;
import sagex.miniclient.UIManager;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.UIFactory;
import sagex.miniclient.util.IOUtil;

public class OpenGLFBUIManager extends Actor implements UIManager<Object> {
    private MiniClientConnectionGateway myConn;

    private List<Runnable> renderQueue = new ArrayList<Runnable>(64);
    private List<Runnable> frameQueue = new ArrayList<Runnable>(64);

    FrameBuffer mainbuffer = null;
    TextureRegion lastFrame = null;

    public OpenGLFBUIManager(MiniClientConnectionGateway conn) {
        this.myConn = conn;
        setSize(MiniClientMain.WIDTH, MiniClientMain.HEIGHT);
        setPosition(0, 0);
    }

    GL20 gl = Gdx.gl20;
    Batch batch;

    SageTVKeyListener keyListener;
    SageTVGestureListener gestureListener;

    ShapeRenderer shapeRenderer = null;

    public void init() {
        System.out.println("BEGIN INIT");
        batch=MiniClientMain.INSTANCE.getStage().getBatch();

        keyListener=new SageTVKeyListener(myConn);
        gestureListener=new SageTVGestureListener(myConn);

        MiniClientMain.INSTANCE.getStage().addActor(this);
        MiniClientMain.INSTANCE.getStage().addListener(keyListener);
        MiniClientMain.INSTANCE.getStage().addListener(gestureListener);

        renderQueue.add(new Runnable() {
            @Override
            public void run() {
                System.out.println("Rendering Background");
                try {
                    Texture t = new Texture("Background.jpg");
                    batch.draw(t, 0, 0, MiniClientMain.WIDTH, MiniClientMain.HEIGHT);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                System.out.println("Done Rendering Background");
                //t.dispose();
            }
        });
        //flipBuffer();

        if (!MiniClientMain.CONTINUOUS_RENDERING) Gdx.graphics.requestRendering();
        System.out.println("END INIT");
    }

    boolean disposed=false;
    public void dispose() {
        if (disposed) return;
        disposed=true;
        MiniClientMain.INSTANCE.getStage().removeListener(keyListener);
        MiniClientMain.INSTANCE.getStage().removeListener(gestureListener);
        remove();
        Gdx.app.exit();
    }

    public void close() {
        dispose();
    }

    public void refresh() {
        Gdx.graphics.requestRendering();
    }

    public void hideCursor() {
        // TODO Auto-generated method stub
    }

    public void showBusyCursor() {
        // TODO Auto-generated method stub
    }

    float Y(int y, int height) {
        return MiniClientMain.HEIGHT-y-height;
    }

    public void drawLine(final int x1, final int y1, final int x2, final int y2, final int argb1, final int argb2) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                batch.end();

                getStage().getCamera().update();
                shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.line(x1, Y(y1, 0), x2, Y(y2, 0), getColor(argb1), getColor(argb2));
                shapeRenderer.end();

                batch.begin();
            }
        });
    }

    public void drawRect(final int x, final int y, final int width, final int height, int thickness, final int argbTL, final int argbTR, final int argbBR, final int argbBL) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                batch.end();

                getStage().getCamera().update();
                shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.rect(x, Y(y, height), width, height, getColor(argbTL), getColor(argbTR), getColor(argbBR), getColor(argbBL));
                shapeRenderer.end();

                batch.begin();
            }
        });
    }

    public void fillRect(final int x, final int y, final int width, final int height, final int argbTL, final int argbTR, final int argbBR, final int argbBL) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                batch.end();

                getStage().getCamera().update();
                shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.rect(x, Y(y, height), width, height, getColor(argbTL), getColor(argbTR), getColor(argbBR), getColor(argbBL));
                shapeRenderer.end();

                batch.begin();
            }
        });
    }

    public void clearRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL) {
        throw new UnsupportedOperationException("clearRect unsopported");
    }

    public void drawOval(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL,
                         int clipX, int clipY, int clipW, int clipH) {
        throw new UnsupportedOperationException("drawOval unsopported");
    }

    public void fillOval(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY,
                         int clipW, int clipH) {
        throw new UnsupportedOperationException("fillOval unsopported");
    }

    public void drawRoundRect(int x, int y, int width, int height, int thickness, int arcRadius, int argbTL, int argbTR, int argbBR,
                              int argbBL, int clipX, int clipY, int clipW, int clipH) {
        //throw new UnsupportedOperationException("drawRoundRect unsopported");
        drawRect(x, y, width, height, thickness, argbTL, argbTR, argbBR, argbBL);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL,
                              int clipX, int clipY, int clipW, int clipH) {
        fillRect(x,y,width,height,argbTL,argbTR, argbBR, argbBL);
    }

    private Color getColor(int color) {
        Color c= new Color();
        c.a = ((color & 0xff000000) >>> 24) / 255f;
        c.r = ((color & 0x00ff0000) >>> 16) / 255f;
        c.g = ((color & 0x0000ff00) >>> 8) / 255f;
        c.b = ((color & 0x000000ff)) / 255f;
        return c;
    }

    private Color getAlpha(int color) {
        Color c= new Color();
        c.a = ((color & 0xff000000) >>> 24) / 255f;
        c.r = 1;
        c.g = 1;
        c.b = 1;
        return c;
    }


    public void drawTexture(final int x, final int y, final int width, final int height, int handle, final ImageHolder<?> img, final int srcx, final int srcy, final int srcwidth,	final int srcheight, final int blend) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                Texture t = null;
                Object o = img.get();
                if (o instanceof Texture) {
                    t = (Texture) o;
                } else if (o instanceof Pixmap) {
                    t = new Texture((Pixmap) img.get());
                    img.force(t);
                } else {
                    t = ((FrameBuffer) o).getColorBufferTexture();
                }
                t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                Color c = batch.getColor();
                batch.end();

                batch.begin();
                batch.enableBlending();
                if (height < 0) {
                    batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ZERO);
                } else {
                    batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
                }
                if (width >= 0) {
                    batch.setColor(getColor(blend));
                } else {
                    batch.setColor(getColor(blend));
                }
                batch.draw(t, x, MiniClientMain.HEIGHT - y - Math.abs(height), Math.abs(width), Math.abs(height), srcx, srcy, srcwidth, srcheight, false, false);
//                batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
//                batch.setColor(1, 1, 1, 1);
                batch.disableBlending();
            }
        });
    }

    public ImageHolder<Object> loadImage(int width, int height) {
        Pixmap pm = new Pixmap(width,height, Pixmap.Format.RGBA8888);
        return new ImageHolder<Object>(pm);
    }

    private GL20 getGL2() {
        return Gdx.gl20;
    }

    public ImageHolder<Object> createSurface(int handle, final int width, final int height) {
        throw new UnsupportedOperationException("createSurface: Surfaces not supported");
    }

    public void setTargetSurface(final int handle, final ImageHolder<?> image) {
        throw new UnsupportedOperationException("setTargetSurface: Surfaces not supported");
    }

    public ImageHolder<Object> readImage(File cachedFile) throws Exception {
        return readImage(new FileInputStream(cachedFile));
    }

    public ImageHolder<Object> readImage(InputStream bais) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream(64*1024);
        IOUtil.fastCopy(bais, os);
        os.flush();
        Pixmap pm = new Pixmap(os.toByteArray(), 0, os.size());
        os.close();
        return new ImageHolder<Object>(pm);
    }

    public ImageHolder<Object> newImage(int width, int height) {
        return loadImage(width, height);
    }

    public void flipBuffer() {
        synchronized (renderQueue) {
            // move the frame operations to the render queue
            renderQueue.addAll(frameQueue);
            frameQueue.clear();
        }
        if (!MiniClientMain.CONTINUOUS_RENDERING) Gdx.graphics.requestRendering();
    }

    public void startFrame() {
        frameQueue.clear();
    }

    public void loadImageLine(int handle, ImageHolder<?> image, int line, int len2, byte[] cmddata) {
        Pixmap pm = (Pixmap) image.get();
        int dataPos = 12;
        int pixel = 0;
        for (int i = 0; i < len2 / 4; i++, dataPos += 4) {
            pixel = GFXCMD2.readInt(dataPos, cmddata);
            pixel = (pixel << 8) | ((pixel >> 24) & 0xFF);
            pm.drawPixel(i, line, pixel);
        }
    }

    public void xfmImage(int srcHandle, ImageHolder<?> srcImg, int destHandle, ImageHolder<?> destImg, int destWidth,
                         int destHeight, int maskCornerArc) {
        throw new UnsupportedOperationException("xfmImage not implemented");
    }

    public boolean hasGraphicsCanvas() {
        return true;
    }

    public Dimension getMaxScreenSize() {
        Dimension reportedScrSize = new Dimension(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return reportedScrSize;
    }

    public Dimension getScreenSize() {
        return new Dimension(MiniClientMain.WIDTH, MiniClientMain.HEIGHT);
    }

    public void setFullScreen(boolean b) {
        // TODO Auto-generated method stub
    }

    public void setSize(int w, int h) {
    }

    public void invokeLater(Runnable runnable) {
        frameQueue.add(runnable);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        synchronized (renderQueue) {
            if (renderQueue.size()>0) {
                //if (!MiniClientMain.CONTINUOUS_RENDERING) System.out.println("RENDER QUEUE");

                if (mainbuffer==null) {
                    mainbuffer = new FrameBuffer(Pixmap.Format.RGBA8888, MiniClientMain.WIDTH, MiniClientMain.HEIGHT, false);
                }

                if (shapeRenderer==null) {
                    shapeRenderer = new ShapeRenderer();
                }

                // render the queue to a frame texture to capure it so that we can replay it later instead of re-rendering the items
                mainbuffer.begin();
                // frame complete.. render it
                for (Runnable r : renderQueue) {
                    r.run();
                }
                mainbuffer.end();
                TextureRegion tr = new TextureRegion(mainbuffer.getColorBufferTexture());
                tr.flip(false, true);
                lastFrame = tr;

                batch.draw(lastFrame, 0, 0);
                renderQueue.clear();
            } else {
                //if (!MiniClientMain.CONTINUOUS_RENDERING) System.out.println("RENDER LAST FRAME");
                if (lastFrame!=null) {
                    // render the last frame
                    batch.draw(lastFrame, 0, 0);
                }
            }
        }
    }

    public static UIFactory getUIFactory() {
        return new UIFactory() {
            public UIManager<?> getUIManager(MiniClientConnection conn) {
                // return new LoggingUIManager(new OpenGLUIManager(conn));
                return new OpenGLFBUIManager(conn);
            }
        };
    }
}
