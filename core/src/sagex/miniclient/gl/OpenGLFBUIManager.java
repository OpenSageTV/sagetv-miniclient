package sagex.miniclient.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sagex.miniclient.FontHolder;
import sagex.miniclient.GFXCMD2;
import sagex.miniclient.ImageHolder;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.MiniClientConnectionGateway;
import sagex.miniclient.MiniClientMain;
import sagex.miniclient.UIManager;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Keys;
import sagex.miniclient.uibridge.MouseEvent;
import sagex.miniclient.uibridge.UIFactory;
import sagex.miniclient.util.IOUtil;

public class OpenGLFBUIManager extends Actor implements UIManager<Object, Void> {
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

    static final HashMap<Integer, Integer> KEYMAP = new HashMap<Integer, Integer>();
    static {
        KEYMAP.put(Input.Keys.UP, Keys.VK_UP);
        KEYMAP.put(Input.Keys.DPAD_UP, Keys.VK_UP);
        KEYMAP.put(Input.Keys.DOWN, Keys.VK_DOWN);
        KEYMAP.put(Input.Keys.DPAD_DOWN, Keys.VK_DOWN);
        KEYMAP.put(Input.Keys.LEFT, Keys.VK_LEFT);
        KEYMAP.put(Input.Keys.DPAD_LEFT, Keys.VK_LEFT);
        KEYMAP.put(Input.Keys.RIGHT, Keys.VK_RIGHT);
        KEYMAP.put(Input.Keys.DPAD_RIGHT, Keys.VK_RIGHT);
    }

    public void init() {
        batch=MiniClientMain.INSTANCE.getStage().getBatch();

        MiniClientMain.INSTANCE.getStage().addActor(this);
        MiniClientMain.INSTANCE.getStage().addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                System.out.println("Post Key Press: " + keycode + "; char: " + event.getCharacter());
                if (KEYMAP.containsKey(keycode)) {
                    keycode = KEYMAP.get(keycode);
                }
                myConn.postKeyEvent(keycode, 0, event.getCharacter());
                return true;
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                myConn.postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 16, (int)x, MiniClientMain.HEIGHT - (int)y, 1, 1, 0));
                return true;
            }
        });

        MiniClientMain.INSTANCE.getStage().addListener(new ActorGestureListener() {
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                System.out.println("TAP: " + x + "," + y);
                myConn.postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 16, (int)x, MiniClientMain.HEIGHT - (int)y, 1, 1, 0));
            }

            int flingThreshold = 200;

            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                myConn.postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 16, (int)x, MiniClientMain.HEIGHT - (int)y, 1, 1, 0));
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                myConn.postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 16, (int)x, MiniClientMain.HEIGHT - (int)y, 1, 1, 0));
            }

            @Override
            public boolean longPress(Actor actor, float x, float y) {
                myConn.postKeyEvent(Keys.VK_ENTER, 0, (char)13);
                return true;
            }

            @Override
            public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                myConn.postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 16, (int)x, MiniClientMain.HEIGHT - (int)y, 1, 1, 0));
            }

            @Override
            public void fling(InputEvent event, float velocityX, float velocityY, int button) {
                System.out.println("FLING: " + velocityX + "," + velocityY);
                if (velocityX > flingThreshold) {
                    System.out.println("Flight Right");
                    myConn.postKeyEvent(Keys.VK_RIGHT, 0, (char) 0);
                } else if (velocityX < -flingThreshold) {
                    myConn.postKeyEvent(Keys.VK_LEFT, 0, (char) 0);
                } else if (velocityY > flingThreshold) {
                    myConn.postKeyEvent(Keys.VK_UP, 0, (char) 0);
                } else if (velocityY < -flingThreshold) {
                    myConn.postKeyEvent(Keys.VK_DOWN, 0, (char) 0);
                }
            }

            @Override
            public void pinch(InputEvent event, Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
                System.out.println("Pinch == Escape");
                myConn.postKeyEvent(Keys.VK_ESCAPE, 0, (char)27);
            }

            @Override
            public void zoom(InputEvent event, float initialDistance, float distance) {
                System.out.println("Zoom == Enter");
                myConn.postKeyEvent(Keys.VK_ENTER, 0, (char)13);
            }
        });
        if (!MiniClientMain.CONTINUOUS_RENDERING) Gdx.graphics.requestRendering();
    }

    public void dispose() {
        remove();
    }

    public void close() {
        remove();
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

    public void drawRect(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL) {
        // TODO Auto-generated method stub
    }

    public void fillRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL) {
        // TODO Auto-generated method stub

    }

    public void clearRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL) {
        // TODO Auto-generated method stub

    }

    public void drawOval(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL,
                         int clipX, int clipY, int clipW, int clipH) {
        // TODO Auto-generated method stub

    }

    public void fillOval(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY,
                         int clipW, int clipH) {
        // TODO Auto-generated method stub

    }

    public void drawRoundRect(int x, int y, int width, int height, int thickness, int arcRadius, int argbTL, int argbTR, int argbBR,
                              int argbBL, int clipX, int clipY, int clipW, int clipH) {
        // TODO Auto-generated method stub

    }

    public void fillRoundRect(int x, int y, int width, int height, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL,
                              int clipX, int clipY, int clipW, int clipH) {
        // TODO Auto-generated method stub

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
                gl.glEnable(gl.GL_BLEND);
                gl.glBlendFunc(gl.GL_ONE, gl.GL_ONE_MINUS_SRC_ALPHA);
                if (height < 0) {
                    gl.glBlendFunc(gl.GL_ONE, gl.GL_ZERO);
                }
                Texture t = null;
                Object o = img.get();
                if (o instanceof Texture) {
                    t = (Texture) o;
                } else if (o instanceof Pixmap) {
                    t = new Texture((Pixmap) img.get());
                    img.force(t);
                } else {
                    t = ((FrameBuffer)o).getColorBufferTexture();
                }
                Color c = batch.getColor();
                if (width<0) {
                    // font
                    batch.setColor(getColor(blend));
                } else {
                    batch.setColor(getAlpha(blend));
                }
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                batch.draw(t, x, MiniClientMain.HEIGHT-y-Math.abs(height), Math.abs(width), Math.abs(height), srcx, srcy, srcwidth, srcheight, false, false);
                batch.setColor(c);
            }
        });
    }

    public void drawLine(int x1, int y1, int x2, int y2, int argb1, int argb2) {
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

    public FontHolder<Void> createFont(InputStream fis) {
        return null;
    }

    public FontHolder<Void> loadFont(String string, int style, int size) {
        return null;
    }

    public FontHolder<Void> deriveFont(FontHolder<?> cachedFont, float size) {
        // TODO Auto-generated method stub
        return null;
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
    }

    public boolean hasGraphicsCanvas() {
        return true;
    }

    public void drawText(int x, int y, int textlen, String text, int fontHandle, FontHolder<?> fontHolder, int argb, int clipX,
                         int clipY, int clipW, int clipH) {
    }

    public Dimension getMaxScreenSize() {
        Dimension reportedScrSize = new Dimension(MiniClientMain.WIDTH, MiniClientMain.HEIGHT);
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
                if (!MiniClientMain.CONTINUOUS_RENDERING) System.out.println("RENDER QUEUE");

                if (mainbuffer==null) {
                    mainbuffer = new FrameBuffer(Pixmap.Format.RGBA8888, MiniClientMain.WIDTH, MiniClientMain.HEIGHT, false);
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
                if (!MiniClientMain.CONTINUOUS_RENDERING) System.out.println("RENDER LAST FRAME");
                if (lastFrame!=null) {
                    // render the last frame
                    batch.draw(lastFrame, 0, 0);
                }
            }
        }
    }

    public static UIFactory getUIFactory() {
        return new UIFactory() {
            public UIManager<?, ?> getUIManager(MiniClientConnection conn) {
                // return new LoggingUIManager(new OpenGLUIManager(conn));
                return new OpenGLFBUIManagerNEW(conn);
            }
        };
    }
}
