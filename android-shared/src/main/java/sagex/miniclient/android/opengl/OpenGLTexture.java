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

    public int width;
    public int height;

    int texture[] = null;

    int pVertices2[] = new int[12];
    IntBuffer pVerticiesByteBuff = ByteBuffer.allocateDirect(pVertices2.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
    float[] uvData = new float[12];
    FloatBuffer uvDataBuff = ByteBuffer.allocateDirect(uvData.length * FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

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
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        //log.debug("New Texture[{}]: {} x {}", texture(), this.width, this.height);

        OpenGLUtils.logGLErrors("createTexture()");
    }

    public void set(Bitmap bitmap, String optName) {
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();

        if (texture == null) createTexture();

        //log.debug("Setting Bitmap[{}]: {}", texture(), optName);

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
        GLES20.glUniformMatrix4fv(OpenGLUtils.textureShader.u_myPMVMatrix, 1, false, toSurface.viewMatrix, 0);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        GLES20.glUniform1i(OpenGLUtils.textureShader.u_sampler2d, 0);

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        if (h < 0) {
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);
            h *= -1;
        }

        if (w < 0) w *= -1;

        GLES20.glUniform4fv(OpenGLUtils.textureShader.u_myColor, 1, OpenGLUtils.argbToFloatArray(blend), 0);

        pVertices2[0] = x;
        pVertices2[1] = y;
        pVertices2[2] = x + w;
        pVertices2[3] = y;
        pVertices2[4] = x + w;
        pVertices2[5] = y + h;
        pVertices2[6] = x + w;
        pVertices2[7] = y + h;
        pVertices2[8] = x;
        pVertices2[9] = y + h;
        pVertices2[10] = x;
        pVertices2[11] = y;

        pVerticiesByteBuff.put(pVertices2);
        pVerticiesByteBuff.position(0);
        GLES20.glVertexAttribPointer(OpenGLUtils.textureShader.a_myVertex,
                2 /* # of elements per vertex*/,
                GLES20.GL_INT,
                false,
                0 /* # bytes per vertex (2 * 4 bytes) */, pVerticiesByteBuff);
        GLES20.glEnableVertexAttribArray(OpenGLUtils.textureShader.a_myVertex);

        uvData[0] = (float) sx / (float) width;
        uvData[1] = (float) sy / (float) height;
        uvData[2] = (float) (sx + sw) / (float) width;
        uvData[3] = (float) sy / (float) height;
        uvData[4] = (float) (sx + sw) / (float) width;
        uvData[5] = (float) (sy + sh) / (float) height;
        uvData[6] = (float) (sx + sw) / (float) width;
        uvData[7] = (float) (sy + sh) / (float) height;
        uvData[8] = (float) sx / (float) width;
        uvData[9] = (float) (sy + sh) / (float) height;
        uvData[10] = (float) sx / (float) width;
        uvData[11] = (float) sy / (float) height;

        // framebuffers need to be flipped
        if (flip) {
            uvData[1] = -1f * uvData[1];
            uvData[3] = -1f * uvData[3];
            uvData[5] = -1f * uvData[5];
            uvData[7] = -1f * uvData[7];
            uvData[9] = -1f * uvData[9];
            uvData[11] = -1f * uvData[11];
        }

        // Again, a FloatBuffer will be used to pass the values
        uvDataBuff.put(uvData);
        uvDataBuff.position(0);
        GLES20.glVertexAttribPointer(OpenGLUtils.textureShader.a_myUV, 2, GLES20.GL_FLOAT, false, 8, uvDataBuff);
        GLES20.glEnableVertexAttribArray(OpenGLUtils.textureShader.a_myUV);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES20.glDisableVertexAttribArray(OpenGLUtils.textureShader.a_myUV);
        GLES20.glDisableVertexAttribArray(OpenGLUtils.textureShader.a_myVertex);

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_TEXTURE_2D);
    }
}
