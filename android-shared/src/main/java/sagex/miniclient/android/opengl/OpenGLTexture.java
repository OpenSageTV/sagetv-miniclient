package sagex.miniclient.android.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import sagex.miniclient.uibridge.Texture;

public class OpenGLTexture implements Texture {
    private static Logger log = LoggerFactory.getLogger(OpenGLTexture.class);

    static final int FLOAT_SIZE = 4;
    static final int POSITION_SIZE = 2;
    static final int TEXTURE_SIZE = 2;
    static final int TOTAL_SIZE = POSITION_SIZE + TEXTURE_SIZE;
    static final int POSITION_OFFSET = 0;
    static final int TEXTURE_OFFSET = 2;

    public int width;
    public int height;

    int texture[] = null;

    boolean flip = false;

    public OpenGLTexture(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int texture() {
        if (texture != null)
            return texture[0];
        return -1;
    }

    public void delete() {
        if (texture != null) {
            log.debug("Deleting Texture: {}", texture());
            GLES20.glDeleteTextures(1, texture, 0);
            texture = null;
        }
    }

    public void createTexture() {
        if (texture == null) {
            texture = new int[1];
        } else {
            log.warn("createTexture() called for existing texture {}", texture(), new Exception("Recreating Texture " + texture()));
        }
        GLES20.glGenTextures(1, texture, 0);

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        log.debug("New Texture[{}]: {} x {}", texture(), this.width, this.height);

        OpenGLUtils.logGLErrors("createTexture()");
    }

    public void set(Bitmap bitmap, String optName) {
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();

        if (texture == null) createTexture();

        log.debug("Setting Bitmap[{}]: {}", texture(), optName);

        try {
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            OpenGLUtils.logGLErrors("bad texture");
        } catch (Throwable t) {
            log.error("[{}]: Unable to load texture from Bitmap: {}", texture(), optName, t);
            throw t;
        }

        bitmap.recycle();
    }

    public void draw(int x, int y, int w, int h, int sx, int sy, int sw, int sh, int blend, OpenGLSurface toSurface) {
        //log.debug("texture draw[{}] on surface {}", texture(), toSurfaceHandle);
        OpenGLUtils.useProgram(OpenGLUtils.textureShader);
        OpenGLUtils.logGLErrors("Texture.draw() useProgram");
        GLES20.glUniformMatrix4fv(OpenGLUtils.textureShader.u_myPMVMatrix, 1, false, toSurface.viewMatrix, 0);
        OpenGLUtils.logGLErrors("Texture.draw() matrix");

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        OpenGLUtils.logGLErrors("Texture.draw() bind texture");
        GLES20.glUniform1i(OpenGLUtils.textureShader.u_sampler2d, 0);

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        if (h < 0) {
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);
            h *= -1;
        }

        if (w < 0) w *= -1;

        GLES20.glUniform4fv(OpenGLUtils.defaultShader.u_myColor, 1, OpenGLUtils.argbToFloatArray(blend), 0);

        int pVertices2[] = {
                x, y,
                (x + w), y,
                (x + w), (y + h),
                (x + w), (y + h),
                x, (y + h),
                x, y};

        // framebuffers are flipped
//        if (flip) {
//            pVertices2[1] = -y;
//            pVertices2[3] = -y;
//            pVertices2[5] = (-y+h);
//            pVertices2[7] = (-y+h);
//            pVertices2[9] = (-y+h);
//            pVertices2[11] = -y;
//        }

        IntBuffer b = ByteBuffer.allocateDirect(pVertices2.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        b.put(pVertices2);
        b.position(0);
        GLES20.glVertexAttribPointer(OpenGLUtils.textureShader.a_myVertex,
                2 /* # of elements per vertex*/,
                GLES20.GL_INT,
                false,
                8 /* # bytes per vertex (2 * 4 bytes) */, b);
        OpenGLUtils.logGLErrors("Texture.draw() verticies1");
        GLES20.glEnableVertexAttribArray(OpenGLUtils.textureShader.a_myVertex);
        OpenGLUtils.logGLErrors("Texture.draw() verticies2");

        float[] data = {
                (float) sx / (float) width, (float) sy / (float) height,
                (float) (sx + sw) / (float) width, (float) sy / (float) height,
                (float) (sx + sw) / (float) width, (float) (sy + sh) / (float) height,
                (float) (sx + sw) / (float) width, (float) (sy + sh) / (float) height,
                (float) sx / (float) width, (float) (sy + sh) / (float) height,
                (float) sx / (float) width, (float) sy / (float) height
        };

        if (flip) {
            data[1] = -1f * data[1];
            data[3] = -1f * data[3];
            data[5] = -1f * data[5];
            data[7] = -1f * data[7];
            data[9] = -1f * data[9];
            data[11] = -1f * data[11];
        }

        //log.debug("Texture: Data: {}", Arrays.toString(data));

        // Again, a FloatBuffer will be used to pass the values
        FloatBuffer fb = ByteBuffer.allocateDirect(data.length * FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
        fb.put(data);
        fb.position(0);
        GLES20.glVertexAttribPointer(OpenGLUtils.textureShader.a_myUV, 2, GLES20.GL_FLOAT, false, 8, fb);
        OpenGLUtils.logGLErrors("Texture.draw() myUV1");
        GLES20.glEnableVertexAttribArray(OpenGLUtils.textureShader.a_myUV);
        OpenGLUtils.logGLErrors("Texture.draw() myUV2");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        OpenGLUtils.logGLErrors("Texture.draw() triangles");

        GLES20.glDisableVertexAttribArray(OpenGLUtils.textureShader.a_myUV);
        GLES20.glDisableVertexAttribArray(OpenGLUtils.textureShader.a_myVertex);

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_TEXTURE_2D);

    }
}
