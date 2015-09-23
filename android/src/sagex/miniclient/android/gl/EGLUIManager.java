package sagex.miniclient.android.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
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

    private final MiniClientGLActivity activity;
    private final MiniClientSurfaceView surface;

    private MiniClientConnection connection;

    private List<Runnable> renderQueue = new ArrayList<>();
    private List<Runnable> frameQueue = new ArrayList<>();

    // --------- BEGIN GL Stuff
    // http://androidblog.reindustries.com/a-real-opengl-es-2-0-2d-tutorial-part-2/
    public static class riGraphicTools {

        // Program variables
        public static int sp_SolidColor;
        public static int sp_Image;

        /* SHADER Solid
         *
         * This shader is for rendering a colored primitive.
         *
         */
        public static final String vs_SolidColor =
                "uniform    mat4        uMVPMatrix;" +
                        "attribute  vec4        vPosition;" +
                        "void main() {" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}";

        public static final String fs_SolidColor =
                "precision mediump float;" +
                        "void main() {" +
                        "  gl_FragColor = vec4(0.5,0,0,1);" +
                        "}";

        /* SHADER Image
 *
 * This shader is for rendering 2D images straight from a texture
 * No additional effects.
 *
 */
        public static final String vs_Image =
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "attribute vec2 a_texCoord;" +
                        "varying vec2 v_texCoord;" +
                        "void main() {" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "  v_texCoord = a_texCoord;" +
                        "}";
        public static final String fs_Image =
                "precision mediump float;" +
                        "varying vec2 v_texCoord;" +
                        "uniform sampler2D s_texture;" +
                        "void main() {" +
                        "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
                        "}";
        public static int loadShader(int type, String shaderCode){

            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            int shader = GLES20.glCreateShader(type);

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);

            // return the shader
            return shader;
        }
    }

    // Our matrices
    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];

    // Geometric variables
    public static float vertices[];
    public static short indices[];
    public static float uvs[];
    public FloatBuffer vertexBuffer;
    public ShortBuffer drawListBuffer;
    public FloatBuffer uvBuffer;

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
    }

    @Override
    public void GFXCMD_DEINIT() {

    }

    @Override
    public void close() {

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
    public void fillRect(int x, int y, int w, int h, int argbTL, int argbTR, int argbBR, int argbBL) {
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

    private float X(float x) {
        //return -1f+(x/size.getWidth())*2;
        return x;
    }

    private float Y(float y) {
        //return -1f+((size.getHeight()-y)/size.getHeight())*2;
        return mScreenHeight-y;
    }

    private float UX(float x, float w) {
        return x/w;
    }
    private float UY(float y, float h) {
        return y/h;
    }

    private void setGLColor(int color)
    {
        //System.out.println(Integer.toHexString(color));
        GLES10.glColor4x((byte) ((color >> 16) & 0xFF), (byte) ((color >> 8) & 0xFF),
                (byte) ((color >> 0) & 0xFF), (byte) ((color >> 24) & 0xFF));

    }


    @Override
    public void drawTexture(final int x, final int y, final int w, final int h, final int handle, final ImageHolder<EGLTexture> img, final int srcx, final int srcy, final int srcwidth, final int srcheight, final int blend) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                // set the image location on the screen
                // We have to create the vertices of our triangles (ie, 2 of them to make a rectangle)
                final int y1 = Math.abs(y);
                vertices = new float[]
                        {  x, Y(y1), 0.0f,
                                x, Y(y1+h), 0.0f,
                                x+w, Y(y1+h), 0.0f,
                                x+w, Y(y1), 0.0f,
                        };

                indices = new short[] {0, 1, 2, 0, 2, 3}; // The order of vertexrendering.

                // The vertex buffer.
                ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
                bb.order(ByteOrder.nativeOrder());
                vertexBuffer = bb.asFloatBuffer();
                vertexBuffer.put(vertices);
                vertexBuffer.position(0);

                // initialize byte buffer for the draw list
                ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
                dlb.order(ByteOrder.nativeOrder());
                drawListBuffer = dlb.asShortBuffer();
                drawListBuffer.put(indices);
                drawListBuffer.position(0);

                // setup the Image to render
                // Create our UV coordinates.
                uvs = new float[] {
//                        0.0f, 0.0f,
//                        0.0f, 1.0f,
//                        1.0f, 1.0f,
//                        1.0f, 0.0f

                        UX(srcx,srcwidth), UY(srcy,srcheight),
                        UX(srcx,srcwidth), UY(srcy+srcheight,srcheight),
                        UX(srcx+srcwidth,srcwidth), UY(srcy+srcheight,srcheight),
                        UX(srcx+srcwidth,srcwidth), UY(srcy,srcheight)
                };

                // The texture buffer
                bb = ByteBuffer.allocateDirect(uvs.length * 4);
                bb.order(ByteOrder.nativeOrder());
                uvBuffer = bb.asFloatBuffer();
                uvBuffer.put(uvs);
                uvBuffer.position(0);


                GLES10.glEnable(GLES10.GL_BLEND);
                GLES10.glEnable(GLES10.GL_TEXTURE_2D);
                GLES10.glBlendFunc(GLES10.GL_ONE, GLES10.GL_ONE_MINUS_SRC_ALPHA);

                // set up blending
                if (h<0) {
                    GLES10.glBlendFunc(GLES10.GL_ONE, GLES10.GL_ZERO);
                }

                setGLColor(blend);

                // bind the image
                EGLTexture texture = img.get();
                if (!texture.loaded) {
                    texture.load();
                } else {
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.get()[0]);
                }





                // ---- Render the Image
                // get handle to vertex shader's vPosition member
                int mPositionHandle =
                        GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "vPosition");

                // Enable generic vertex attribute array
                GLES20.glEnableVertexAttribArray(mPositionHandle);

                // Prepare the triangle coordinate data
                GLES20.glVertexAttribPointer(mPositionHandle, 3,
                        GLES20.GL_FLOAT, false,
                        0, vertexBuffer);

                // Get handle to texture coordinates location
                int mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image,
                        "a_texCoord" );

                // Enable generic vertex attribute array
                GLES20.glEnableVertexAttribArray ( mTexCoordLoc );

                // Prepare the texturecoordinates
                GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                        false,
                        0, uvBuffer);

                // Get handle to shape's transformation matrix
                int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image,
                        "uMVPMatrix");

                // Apply the projection and view transformation
                GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, mtrxProjectionAndView, 0);

                // Get handle to textures locations
                int mSamplerLoc = GLES20.glGetUniformLocation (riGraphicTools.sp_Image,
                        "s_texture" );

                // Set the sampler texture unit to 0, where we have saved the texture.
                GLES20.glUniform1i ( mSamplerLoc, 0);

                // Draw the triangle
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                        GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

                // Disable vertex array
                GLES20.glDisableVertexAttribArray(mPositionHandle);
                GLES20.glDisableVertexAttribArray(mTexCoordLoc);

                GLES10.glDisable(GLES10.GL_BLEND);
                GLES10.glDisable(GLES10.GL_TEXTURE_2D);
            }
        });

        frameOps++;
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int argb1, int argb2) {

    }

    @Override
    public ImageHolder<EGLTexture> loadImage(int width, int height) {
        return null;
    }

    @Override
    public void loadImageLine(int handle, ImageHolder<EGLTexture> image, int line, int len2, byte[] cmddata) {
    }

    @Override
    public ImageHolder<EGLTexture> createSurface(int handle, int width, int height) {
        return null;
    }

    @Override
    public ImageHolder<EGLTexture> readImage(File cachedFile) throws Exception {
        return new ImageHolder<>(new EGLTexture(cachedFile));
    }

    @Override
    public ImageHolder<EGLTexture> readImage(InputStream bais) throws Exception {
        return null;
    }

    @Override
    public ImageHolder<EGLTexture> newImage(int destWidth, int destHeight) {
        return loadImage(destWidth, destHeight);
    }

    @Override
    public void setTargetSurface(int handle, ImageHolder<EGLTexture> image) {

    }

    boolean dropFrame=false;
    @Override
    public void flipBuffer() {
        dropFrame=true;
        synchronized (renderQueue) {
            renderQueue.addAll(frameQueue);
            frameQueue.clear();
        }
        dropFrame=false;
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

    @Override
    public void onSurfaceCreated(GL10 notused, EGLConfig config) {
        // Set the clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);

        // Create the shaders, solid color
        int vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER,
                riGraphicTools.vs_SolidColor);
        int fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER,
                riGraphicTools.fs_SolidColor);

        riGraphicTools.sp_SolidColor = GLES20.glCreateProgram();
        GLES20.glAttachShader(riGraphicTools.sp_SolidColor, vertexShader);
        GLES20.glAttachShader(riGraphicTools.sp_SolidColor, fragmentShader);
        GLES20.glLinkProgram(riGraphicTools.sp_SolidColor);

        // Create the shaders, images
        vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER,
                riGraphicTools.vs_Image);
        fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER,
                riGraphicTools.fs_Image);

        riGraphicTools.sp_Image = GLES20.glCreateProgram();
        GLES20.glAttachShader(riGraphicTools.sp_Image, vertexShader);
        GLES20.glAttachShader(riGraphicTools.sp_Image, fragmentShader);
        GLES20.glLinkProgram(riGraphicTools.sp_Image);

        // Set our shader programm
        GLES20.glUseProgram(riGraphicTools.sp_Image);
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

        // Create the triangles
//        SetupImageLocation(0, 0, mScreenWidth, mScreenHeight);
//        // Create the image information
//        SetupImage();
    }

    @Override
    public void onDrawFrame(GL10 notused) {
        Log.d(TAG, "GL draw frame");
        // clear Screen and Depth Buffer,
        // we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        synchronized (renderQueue) {
            for (Runnable r : renderQueue) {
                if (dropFrame) {
                    dropFrame=false;
                    break;
                }
                r.run();
            }
            renderQueue.clear();
        }
    }
}
