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
import java.nio.ShortBuffer;

import sagex.miniclient.uibridge.Texture;

public class OpenGLTexture implements Texture {
    public static int TEXTURE_FILTER = GLES20.GL_LINEAR;

    private static Logger log = LoggerFactory.getLogger(OpenGLTexture.class);

    static final int FLOAT_SIZE = 4;

    public int width;
    public int height;

    int texture[] = null;

    int pVertices2[] = new int[8];
    IntBuffer pVerticiesByteBuff = ByteBuffer.allocateDirect(pVertices2.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();

    float[] uvData = new float[8];
    FloatBuffer uvDataBuff = ByteBuffer.allocateDirect(uvData.length * FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

    private static ShortBuffer drawListBuffer;
    protected static short drawOrder[] = {0, 1, 2, 0, 3, 2}; // order to draw vertices

    static {
        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
    }

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
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, TEXTURE_FILTER);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, TEXTURE_FILTER);

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
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        GLES20.glUniform1i(OpenGLUtils.textureShader.u_sampler2d, 0);

        // the Quartz renderer does use -w and -h values, so this is here for reference.
//        boolean doBlend = true;
//        if(h < 0) {
//            doBlend = false;
//            h *= -1;
//        }
//
//        if(w < 0) {
//            w *= -1;
//        } else {
//            if(doBlend)
//                blend |= 0x00ffffff; // only use alpha
//        }
//
//
//        if (doBlend) {
//            GLES20.glEnable(GLES20.GL_BLEND);
//            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
//        } else {
//            GLES20.glDisable(GLES20.GL_BLEND);
//        }

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        if (h < 0) {
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);
            h *= -1;
        }

        if (w < 0) w *= -1;

        GLES20.glUniform4fv(OpenGLUtils.textureShader.u_myColor, 1, OpenGLUtils.argbToFloatArray(blend), 0);


//  // top left
        pVertices2[0] = x;
        pVertices2[1] = y;
//  // bottom left
        pVertices2[2] = x;
        pVertices2[3] = y + h;
//  // bottom right
        pVertices2[4] = x + w;
        pVertices2[5] = y + h;
//  // top right
        pVertices2[6] = x + w;
        pVertices2[7] = y;

        pVerticiesByteBuff.put(pVertices2);
        pVerticiesByteBuff.position(0);
        GLES20.glVertexAttribPointer(OpenGLUtils.textureShader.a_myVertex,
                2 /* # of elements per vertex*/,
                GLES20.GL_INT,
                false,
                0, pVerticiesByteBuff);
        GLES20.glEnableVertexAttribArray(OpenGLUtils.textureShader.a_myVertex);

//  // top left
        uvData[0] = (float) sx / (float) width;
        uvData[1] = (float) sy / (float) height;
//  // bottom left
        uvData[2] = (float) sx / (float) width;
        uvData[3] = (float) (sy + sh) / (float) height;
//  // bottom right
        uvData[4] = (float) (sx + sw) / (float) width;
        uvData[5] = (float) (sy + sh) / (float) height;
//  // top right
        uvData[6] = (float) (sx + sw) / (float) width;
        uvData[7] = (float) sy / (float) height;

        // Again, a FloatBuffer will be used to pass the values
        uvDataBuff.put(uvData);
        uvDataBuff.position(0);
        GLES20.glVertexAttribPointer(OpenGLUtils.textureShader.a_myUV, 2,
                GLES20.GL_FLOAT, false, 0, uvDataBuff);
        GLES20.glEnableVertexAttribArray(OpenGLUtils.textureShader.a_myUV);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(OpenGLUtils.textureShader.a_myUV);
        GLES20.glDisableVertexAttribArray(OpenGLUtils.textureShader.a_myVertex);

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_TEXTURE_2D);
    }
}
