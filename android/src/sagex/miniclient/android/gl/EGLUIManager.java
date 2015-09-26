package sagex.miniclient.android.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.android.gl.shapes.Line;
import sagex.miniclient.android.gl.shapes.Rect;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.ImageHolder;
import sagex.miniclient.uibridge.Scale;
import sagex.miniclient.uibridge.UIManager;

/**
 * Created by seans on 19/09/15.
 */
public class EGLUIManager implements UIManager<EGLTexture>, GLSurfaceView.Renderer {
    private static final String TAG = EGLUIManager.class.getSimpleName().toUpperCase();

    // logging stuff
    boolean logFrameTime=true;
    boolean logTextureTime=true;
    long longestTextureTime=0;
    long totalTextureTime=0;

    long frameTime = 0;
    long frameOps=0;
    long frame=0;

    Scale scale = new Scale(1,1);
    Dimension size = null;

    boolean firstFrame=true;

    private final MiniClientGLActivity activity;
    private final MiniClientSurfaceView surface;

    private MiniClientConnection connection;

    private List<Runnable> renderQueue = new ArrayList<>();
    private List<Runnable> frameQueue = new ArrayList<>();

    // Our matrices
    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];

    // Geometric variables
    public static float vertices[];
    public static short indices[];
    public static float uvs[];
    public static float colors[];
    public FloatBuffer vertexBuffer;
    public ShortBuffer drawListBuffer;
    public FloatBuffer uvBuffer;
    private FloatBuffer colorBuffer;

    private int mPositionHandle;
    private int mTexCoordLoc;
    private int mColorHandle;
    private int mtrxhandle;
    private int mSamplerLoc;


    // Our screenresolution
    float   mScreenWidth = 1280;
    float   mScreenHeight = 768;

    // Misc
    Context mContext;
    long mLastTime;
    int mProgram;
    // --------- END GL Stuff


    public EGLUIManager(MiniClientGLActivity miniClientGLActivity, MiniClientSurfaceView surface) {
        this.activity=miniClientGLActivity;
        this.mContext=miniClientGLActivity;
        this.surface=surface;
    }

    @Override
    public void GFXCMD_INIT() {
        connection.postResizeEvent(getScreenSize());
        if (firstFrame) {
            activity.setConnectingIsVisible(firstFrame);
        }
    }

    @Override
    public void GFXCMD_DEINIT() {

    }

    @Override
    public void close() {
        GFXCMD_DEINIT();
        activity.finish();
    }

    @Override
    public void refresh() {
        connection.postRepaintEvent(0,0,(int)mScreenWidth,(int)mScreenHeight);
    }

    @Override
    public void hideCursor() {

    }

    @Override
    public void showBusyCursor() {

    }

    @Override
    public void drawRect(final int x, final int y, final int width, final int height, int thickness, final int argbTL, int argbTR, int argbBR, int argbBL) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                Rect r = new Rect();
                r.SetRectXY(x, y, width, height, argbTL, mScreenWidth, mScreenHeight);
                r.draw(mtrxProjectionAndView);
            }
        });
    }

    @Override
    public void fillRect(int x, int y, int w, int h, int argbTL, int argbTR, int argbBR, int argbBL) {
        drawRect(x,y,w,h,1,argbTL,0,0,0);
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
        drawRect(x,y,width,height,1,argbTL,0,0,0);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        drawRect(x,y,width,height,1,argbTL,0,0,0);
    }

    private float Y(float y) {
        return mScreenHeight-y;
    }

    private float UX(float x, float w) {
        float v = x/w;
        return v;
    }
    private float UY(float y, float h) {
        float v = y/h;
        return v;
    }

    int lastColor = -1;
    @Override
    public void drawTexture(final int x, final int y, final int w, final int h, final int handle, final ImageHolder<EGLTexture> img, final int srcx, final int srcy, final int srcwidth, final int srcheight, final int blend) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                // Set our shader programm
                GLES20.glUseProgram(GLUtils.TextureShaderUtil.sp_Image);

                // set the image location on the screen
                // We have to create the vertices of our triangles (ie, 2 of them to make a rectangle)
                final int y1 = y;
                final int h1 = Math.abs(h);
                final int w1 = Math.abs(w);
                vertices = new float[]
                        {  x, Y(y1), 0.0f,
                                x, Y(y1+h1), 0.0f,
                                x+w1, Y(y1+h1), 0.0f,
                                x+w1, Y(y1), 0.0f,
                        };

                // The vertex buffer.
                vertexBuffer.put(vertices);
                vertexBuffer.position(0);

                // texture width and height
                float tw=img.get().width;
                float th=img.get().height;

                // setup the Image to render
                // Create our UV coordinates.
                uvs = new float[] {
                        UX(srcx,tw), UY(srcy,th),
                        UX(srcx,tw), UY(srcy + srcheight,th),
                        UX(srcx+ srcwidth,tw), UY(srcy + srcheight,th),
                        UX(srcx+ srcwidth,tw), UY(srcy,th)
                };

                // The texture buffer
                uvBuffer.put(uvs);
                uvBuffer.position(0);

                if (blend!=lastColor) {
//                // setup color
                    colors = new float[]{
                            1f * ((blend >> 16) & 0xFF) / 255f,
                            1f * ((blend >> 8) & 0xFF) / 255f,
                            1f * ((blend >> 0) & 0xFF) / 255f,
                            1f * ((blend >> 24) & 0xFF) / 255f,

                            1f * ((blend >> 16) & 0xFF) / 255f,
                            1f * ((blend >> 8) & 0xFF) / 255f,
                            1f * ((blend >> 0) & 0xFF) / 255f,
                            1f * ((blend >> 24) & 0xFF) / 255f,

                            1f * ((blend >> 16) & 0xFF) / 255f,
                            1f * ((blend >> 8) & 0xFF) / 255f,
                            1f * ((blend >> 0) & 0xFF) / 255f,
                            1f * ((blend >> 24) & 0xFF) / 255f,

                            1f * ((blend >> 16) & 0xFF) / 255f,
                            1f * ((blend >> 8) & 0xFF) / 255f,
                            1f * ((blend >> 0) & 0xFF) / 255f,
                            1f * ((blend >> 24) & 0xFF) / 255f
                    };

                    // color
                    // The vertex buffer.
                    colorBuffer.put(colors);
                    colorBuffer.position(0);
                }

                // set the color
                // Enable a handle to the triangle vertices
                GLES20.glEnableVertexAttribArray(mColorHandle);

                // Prepare the background coordinate data
                GLES20.glVertexAttribPointer(mColorHandle, 4,
                        GLES20.GL_FLOAT, false,
                        0, colorBuffer);

                GLES10.glEnable(GLES10.GL_BLEND);
                GLES10.glEnable(GLES10.GL_TEXTURE_2D);
                GLES10.glBlendFunc(GLES10.GL_ONE, GLES10.GL_ONE_MINUS_SRC_ALPHA);

                // set up blending
                if (h<0) {
                    GLES10.glBlendFunc(GLES10.GL_ONE, GLES10.GL_ZERO);
                }

                // bind the image
                EGLTexture texture = img.get();
                if (!texture.loaded) {
                    texture.load();
                } else {
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.get()[0]);
                }


                // ---- Render the Image
                // Enable generic vertex attribute array
                GLES20.glEnableVertexAttribArray(mPositionHandle);

                // Prepare the triangle coordinate data
                GLES20.glVertexAttribPointer(mPositionHandle, 3,
                        GLES20.GL_FLOAT, false,
                        0, vertexBuffer);

                // Enable generic vertex attribute array
                GLES20.glEnableVertexAttribArray(mTexCoordLoc);

                // Prepare the texturecoordinates
                GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT,
                        false,
                        0, uvBuffer);


                // Apply the projection and view transformation
                GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, mtrxProjectionAndView, 0);

                // Set the sampler texture unit to 0, where we have saved the texture.
                GLES20.glUniform1i(mSamplerLoc, 0);

                // Draw the triangle
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                        GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

                // Disable vertex array
                GLES20.glDisableVertexAttribArray(mPositionHandle);
                GLES20.glDisableVertexAttribArray(mTexCoordLoc);
                GLES20.glDisableVertexAttribArray(mColorHandle);

                GLES10.glDisable(GLES10.GL_BLEND);
                GLES10.glDisable(GLES10.GL_TEXTURE_2D);

                lastColor=blend;
            }
        });

        frameOps++;
    }

    @Override
    public void drawLine(final int x1, final int y1, final int x2, final int y2, final int argb1, int argb2) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                Line l = new Line();
                l.SetLineXY(x1,y1,x2,y2,argb1,mScreenWidth,mScreenHeight);
                l.draw(mtrxProjectionAndView);
            }
        });
    }

    @Override
    public ImageHolder<EGLTexture> loadImage(int width, int height) {
        return null;
    }

    @Override
    public void unloadImage(int handle, final ImageHolder<EGLTexture> bi) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                GLES10.glDeleteTextures(1, bi.get().get(), 0);
            }
        });
    }

    @Override
    public void loadImageLine(int handle, ImageHolder<EGLTexture> image, int line, int len2, byte[] cmddata) {
    }

    @Override
    public ImageHolder<EGLTexture> readImage(File file) throws Exception {
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
    public ImageHolder<EGLTexture> readImage(InputStream fis) throws Exception {
        long st = System.currentTimeMillis();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);

        long time = System.currentTimeMillis()-st;
        totalTextureTime+=time;
        longestTextureTime=Math.max(time,longestTextureTime);

        return new ImageHolder<>(new EGLTexture(bitmap));
    }

    @Override
    public ImageHolder<EGLTexture> newImage(int destWidth, int destHeight) {
        return loadImage(destWidth, destHeight);
    }

    @Override
    public ImageHolder<EGLTexture> createSurface(int handle, int width, int height) {
        final EGLTexture t = new EGLTexture(true, width, height);
        invokeLater(new Runnable() {
            @Override
            public void run() {
                // setup the texture and bind the framebuffer
                t.load();
                t.bindFramebuffer();
            }
        });
        return new ImageHolder<>(t);
    }

    @Override
    public void setTargetSurface(int handle, ImageHolder<EGLTexture> image) {
        if (handle==0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            //image.get().unbindFramebuffer();
        } else {
            image.get().bindFramebuffer();
        }
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
        surface.requestRender();
        if (logFrameTime) {
            Log.d(TAG, "FRAME: " + (frame) + "; Time: " + (System.currentTimeMillis()-frameTime) + "ms; Ops: " + frameOps);
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
    public void xfmImage(int srcHandle, ImageHolder<EGLTexture> srcImg, int destHandle, ImageHolder<EGLTexture> destImg, int destWidth, int destHeight, int maskCornerArc) {
    }

    @Override
    public boolean hasGraphicsCanvas() {
        return true;
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
    public MiniClientConnection getConnection() {
        return connection;
    }

    @Override
    public void onSurfaceCreated(GL10 notused, EGLConfig config) {
        // Set the clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);

        // Create the shaders, solid color
        // Create the shaders, images
        int vertexShader = GLUtils.loadShader(GLES20.GL_VERTEX_SHADER,
                GLUtils.TextureShaderUtil.vs_Image);
        int fragmentShader = GLUtils.loadShader(GLES20.GL_FRAGMENT_SHADER,
                GLUtils.TextureShaderUtil.fs_Image);

        GLUtils.TextureShaderUtil.sp_Image = GLES20.glCreateProgram();
        GLES20.glAttachShader(GLUtils.TextureShaderUtil.sp_Image, vertexShader);
        GLES20.glAttachShader(GLUtils.TextureShaderUtil.sp_Image, fragmentShader);
        GLES20.glLinkProgram(GLUtils.TextureShaderUtil.sp_Image);

        // get handle to vertex shader's vPosition member
        mPositionHandle =
                GLES20.glGetAttribLocation(GLUtils.TextureShaderUtil.sp_Image, "vPosition");

        // Get handle to texture coordinates location
        mTexCoordLoc = GLES20.glGetAttribLocation(GLUtils.TextureShaderUtil.sp_Image,
                "a_texCoord" );

        mColorHandle = GLES20.glGetAttribLocation(GLUtils.TextureShaderUtil.sp_Image,
                "a_Color");


        // Get handle to shape's transformation matrix
        mtrxhandle = GLES20.glGetUniformLocation(GLUtils.TextureShaderUtil.sp_Image,
                "uMVPMatrix");

        // Get handle to textures locations
        mSamplerLoc = GLES20.glGetUniformLocation (GLUtils.TextureShaderUtil.sp_Image,
                "s_texture" );


        // draw vertex order
        indices = new short[] {0, 1, 2, 0, 2, 3}; // The order of vertexrendering.

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);

        // create vertex buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(3 * 4 * 4); // 3 coords * 4 vertices * 4
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();

        // UV buffer
        bb = ByteBuffer.allocateDirect(2 * 4 * 4); // 2 coords * 4 vertices * 4
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();

        // color buffer
        bb = ByteBuffer.allocateDirect(4 * 4 * 4); // rgba * 4 vertices * 4
        bb.order(ByteOrder.nativeOrder());
        colorBuffer = bb.asFloatBuffer();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // We need to know the current width and height.
        mScreenWidth = width;
        mScreenHeight = height;

        // Redo the Viewport, making it fullscreen.
        GLES20.glViewport(0, 0, (int) mScreenWidth, (int) mScreenHeight);

        // Clear our matrices
        for(int i=0;i<16;i++)
        {
            mtrxProjection[i] = 0.0f;
            mtrxView[i] = 0.0f;
            mtrxProjectionAndView[i] = 0.0f;
        }

        // Setup our screen width and height for normal sprite translation.
        Matrix.orthoM(mtrxProjection, 0, 0f, mScreenWidth, 0.0f, mScreenHeight, 0, 50);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);
    }

    @Override
    public void onDrawFrame(GL10 notused) {
        // clear Screen and Depth Buffer,
        // we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        long st=System.currentTimeMillis();
        synchronized (renderQueue) {
            for (Runnable r : renderQueue) {
                r.run();
            }
            renderQueue.clear();
        }
        long et=System.currentTimeMillis();
        if (logFrameTime) {
            Log.d(TAG, "RENDER: Time: " + (et-st) + "ms");
        }
    }
}
