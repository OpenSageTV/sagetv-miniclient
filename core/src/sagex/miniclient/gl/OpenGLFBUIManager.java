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
import sagex.miniclient.uibridge.UIFactory;
import sagex.miniclient.util.IOUtil;

public class OpenGLFBUIManager extends Actor implements UIManager<Object, Void> {
	private MiniClientConnectionGateway myConn;

    private boolean buffers=true;

    private FrameBuffer mainbuffer0;
    private FrameBuffer backbuffer;
    private TextureRegion renderBuffer;

    private List<Runnable> renderQueue = new ArrayList<Runnable>(64);
    private boolean renderQueueItems =true;

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
                    keycode=KEYMAP.get(keycode);
                }
                myConn.postKeyEvent(keycode, 0, event.getCharacter());
                return true;
            }
        });

        MiniClientMain.INSTANCE.getStage().addListener(new ActorGestureListener() {
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                System.out.println("TAP: " + x + "," + y);
                //myConn.postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 16, (int) x, MiniClientMain.HEIGHT - (int) y, 1, 1, 0));
                myConn.postKeyEvent(Keys.VK_ENTER, 0, (char) 0);
            }

            @Override
            public void fling(InputEvent event, float velocityX, float velocityY, int button) {
                System.out.println("FLING: " + velocityX + "," + velocityY);
                if (velocityX > 100) {
                    System.out.println("Flight Right");
                    myConn.postKeyEvent(Keys.VK_RIGHT, 0, (char) 0);
                } else if (velocityX < -100) {
                    myConn.postKeyEvent(Keys.VK_LEFT, 0, (char) 0);
                } else if (velocityY > 100) {
                    myConn.postKeyEvent(Keys.VK_UP, 0, (char) 0);
                } else if (velocityY < -100) {
                    myConn.postKeyEvent(Keys.VK_DOWN, 0, (char) 0);
                }
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
        // System.out.println(Integer.toHexString(color));
        //Color c = new Color((byte) ((color >> 16) & 0xFF), (byte) ((color >> 8) & 0xFF), (byte) ((color >> 0) & 0xFF), (byte) ((color >> 24) & 0xFF));
        Color c= new Color(color);
//        c.r = ((color >> 16) & 0xFF);
//        c.g = ((color >> 8) & 0xFF);
//        c.b = ((color >> 0) & 0xFF);
//        c.a = ((color >> 24) & 0xFF);
        return c;
        //gl.glColor4ub((byte) ((color >> 16) & 0xFF), (byte) ((color >> 8) & 0xFF), (byte) ((color >> 0) & 0xFF), (byte) ((color >> 24) & 0xFF));

    }

    private int getRGBAColor(int color) {
        // System.out.println(Integer.toHexString(color));
        return Color.rgba8888((float) ((color >> 16) & 0xFF), (float) ((color >> 8) & 0xFF), (float) ((color >> 0) & 0xFF), (float) ((color >> 24) & 0xFF));
    }

	public void drawTexture(final int x, final int y, final int width, final int height, int handle, final ImageHolder<?> img, final int srcx, final int srcy, final int srcwidth,	final int srcheight, final int blend) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                if (buffers) backbuffer.begin();
                //if (target==0) return;
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
                //batch.begin();
                Color c = batch.getColor();
                if (width<0) {
                    // font
                    batch.setColor(getColor(blend));
                }
                batch.draw(t, x, MiniClientMain.HEIGHT-y-Math.abs(height), Math.abs(width), Math.abs(height), srcx, srcy, srcwidth, srcheight, false, false);
                batch.setColor(c);
                //batch.end();
                if (buffers) backbuffer.end();
                //t.dispose();
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
        final FrameBufferHolder fbh = new FrameBufferHolder();
        invokeLater(new Runnable() {
            @Override
            public void run() {
                FrameBuffer fb = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
                fbh.set(fb);
                // be nice if SageTV called SetTargetSurface
                if (buffers) backbuffer = fb;
                if (buffers) backbuffer.begin();
//                Gdx.gl.glClearColor(0, 0, 0, 0);
//                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                if (buffers) backbuffer.end();
            }
        });
        return fbh;
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

	public void setTargetSurface(final int handle, final ImageHolder<?> image) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                if (handle==0) {
                    backbuffer=mainbuffer0;
                } else {
                    backbuffer= (FrameBuffer) image.get();
                }
            }
        });
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
        invokeLater(new Runnable() {
            @Override
            public void run() {
                if (buffers) {
                    renderBuffer = new TextureRegion(backbuffer.getColorBufferTexture());
                    renderBuffer.flip(false, true);
                }
            }
        });
        renderQueueItems =true;
        if (!MiniClientMain.CONTINUOUS_RENDERING) Gdx.graphics.requestRendering();
	}

	public void startFrame() {
        synchronized (renderQueue) {
            renderQueueItems = false;
            invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (buffers) {
                        if (mainbuffer0 == null) {
                            mainbuffer0 = new FrameBuffer(Pixmap.Format.RGBA8888, MiniClientMain.WIDTH, MiniClientMain.HEIGHT, false);
                            if (backbuffer == null) {
                                backbuffer = mainbuffer0;
                            } else {
                                backbuffer.dispose();
                                backbuffer = mainbuffer0;
                            }
                        }
                        backbuffer.begin();
                    }
                    //Gdx.gl.glClearColor(0, 0, 0, 0);
                    //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                    if (buffers) backbuffer.end();
                }
            });
        }
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
        //Gdx.app.postRunnable(runnable);
        renderQueue.add(runnable);
	}

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!renderQueueItems) {
            if (!MiniClientMain.CONTINUOUS_RENDERING) System.out.println("RENDER OLD BUFFER");
            // frame not finished, render last buffer
            if (renderBuffer != null) {
                try {
                    batch.draw(renderBuffer, 0, 0);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } else {
            synchronized (renderQueue) {
                if (!MiniClientMain.CONTINUOUS_RENDERING) System.out.println("RENDER QUEUE");
                // frame complete.. render it
                for (Runnable r : renderQueue) {
                    r.run();
                }
                renderQueue.clear();
                renderQueueItems=false;
                if (renderBuffer!=null)
                    batch.draw(renderBuffer, 0, 0);
            }
        }
    }

    public static UIFactory getUIFactory() {
		return new UIFactory() {
			public UIManager<?, ?> getUIManager(MiniClientConnection conn) {
				// return new LoggingUIManager(new OpenGLUIManager(conn));
                return new OpenGLFBUIManager(conn);
			}
		};
	}
}
