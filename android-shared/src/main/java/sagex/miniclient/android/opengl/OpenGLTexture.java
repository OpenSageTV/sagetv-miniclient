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

        ShaderUtils.logGLErrors("createTexture()");
    }

    public void set(Bitmap bitmap, String optName) {
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();

        if (texture == null) createTexture();

        log.debug("Setting Bitmap[{}]: {}", texture(), optName);

        try {
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            ShaderUtils.logGLErrors("bad texture");
        } catch (Throwable t) {
            log.error("[{}]: Unable to load texture from Bitmap: {}", texture(), optName, t);
            throw t;
        }

        // GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle();
    }

    public void draw(int x, int y, int width, int height, int sx, int sy, int sw, int sh, int blend, OpenGLSurface toSurface, int toSurfaceHandle) {
        log.debug("texture draw[{}] on surface {}", texture(), toSurfaceHandle);
        ShaderUtils.useProgram(ShaderUtils.textureShader);
        ShaderUtils.logGLErrors("Texture.draw() useProgram");
        GLES20.glUniformMatrix4fv(ShaderUtils.textureShader.u_myPMVMatrix, 1, false, toSurface.viewMatrix, 0);
        ShaderUtils.logGLErrors("Texture.draw() matrix");

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        ShaderUtils.logGLErrors("Texture.draw() bind texture");
        GLES20.glUniform1i(ShaderUtils.textureShader.u_sampler2d, 0);

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        if (height < 0) {
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);
            height *= -1;
        }

        if (width < 0) width *= -1;

        int pColor[] = {blend, blend, blend, blend, blend};
        IntBuffer bb = ByteBuffer.allocateDirect(pColor.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        bb.put(pColor);
        GLES20.glVertexAttribPointer(ShaderUtils.textureShader.u_myColor, 4, GLES20.GL_UNSIGNED_BYTE, true, 0, bb);
        ShaderUtils.logGLErrors("Texture.draw() colors1");
        GLES20.glEnableVertexAttribArray(ShaderUtils.textureShader.u_myColor);
        ShaderUtils.logGLErrors("Texture.draw() colors2");

        short pVertices2[] = {(short) x, (short) y, (short) (x + width), (short) y, (short) (x + width), (short) (y + height), (short) (x + width), (short) (y + height), (short) x, (short) (y + height), (short) x, (short) y};
        ShortBuffer b = ByteBuffer.allocateDirect(pVertices2.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        b.put(pVertices2);
        GLES20.glVertexAttribPointer(ShaderUtils.textureShader.a_myVertex, POSITION_SIZE, GLES20.GL_SHORT, false, 0, b);
        ShaderUtils.logGLErrors("Texture.draw() verticies1");
        GLES20.glEnableVertexAttribArray(ShaderUtils.textureShader.a_myVertex);
        ShaderUtils.logGLErrors("Texture.draw() verticies2");

        float[] data = {
                (float) sx / (float) width,
                (float) sy / (float) height,
                (float) (sx + sw) / (float) width,
                (float) sy / (float) height,
                (float) (sx + sw) / (float) width,
                (float) (sy + sh) / (float) height,
                (float) (sx + sw) / (float) width,
                (float) (sy + sh) / (float) height,
                (float) sx / (float) width,
                (float) (sy + sh) / (float) height,
                (float) sx / (float) width,
                (float) sy / (float) height
        };
        // Again, a FloatBuffer will be used to pass the values
        FloatBuffer fb = ByteBuffer.allocateDirect(data.length * FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
        fb.put(data);
        GLES20.glVertexAttribPointer(ShaderUtils.textureShader.a_myUV, TEXTURE_SIZE, GLES20.GL_FLOAT, false, 0, fb);
        ShaderUtils.logGLErrors("Texture.draw() myUV1");
        GLES20.glEnableVertexAttribArray(ShaderUtils.textureShader.a_myUV);
        ShaderUtils.logGLErrors("Texture.draw() myUV2");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        ShaderUtils.logGLErrors("Texture.draw() triangles");

//        GLES20.glDisable(GLES20.GL_BLEND);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        // Position of our image
//        b.position(POSITION_OFFSET);
//        GLES20.glVertexAttribPointer(ShaderUtils.TEXTURE_PROGRAM_aPOSITION, POSITION_SIZE, GLES20.GL_FLOAT, false, TOTAL_SIZE * FLOAT_SIZE, b);
//        GLES20.glEnableVertexAttribArray(ShaderUtils.TEXTURE_PROGRAM_aPOSITION);

        // Positions of the texture
//        b.position(TEXTURE_OFFSET);
//        GLES20.glVertexAttribPointer(ShaderUtils.TEXTURE_PROGRAM_aTEXPOS, TEXTURE_SIZE, GLES20.GL_FLOAT, false, TOTAL_SIZE * FLOAT_SIZE, b);
//        GLES20.glEnableVertexAttribArray(ShaderUtils.TEXTURE_PROGRAM_aTEXPOS);

        // Pass the projection and view transformation to the shader
        // GLES20.glUniformMatrix4fv(ShaderUtils.TEXTURE_PROGRAM_uSCREEN, 1, false, viewMatrix , 0);

        // Clear the screen and draw the rectangle
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

//        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
//        if(height<0)
//        {
//            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);
//            height*=-1;
//        }
//
//        if(width<0) width*=-1;
//        // Images
//        GLshort pVertices2[] = {x, y, x+width, y, x+width, y+height,
//                x+width, y+height, x, y+height, x, y};
//        GLuint pColors2[] = {blend, blend, blend, blend, blend, blend};
//        GLfloat pCoords2[] = {
//                1.0f*srcx/srcimage->uWidth, 1.0f*srcy/srcimage->uHeight,
//                1.0f*(srcx+srcwidth)/srcimage->uWidth, 1.0f*srcy/srcimage->uHeight,
//                1.0f*(srcx+srcwidth)/srcimage->uWidth, 1.0f*(srcy+srcheight)/srcimage->uHeight,
//                1.0f*(srcx+srcwidth)/srcimage->uWidth, 1.0f*(srcy+srcheight)/srcimage->uHeight,
//                1.0f*srcx/srcimage->uWidth, 1.0f*(srcy+srcheight)/srcimage->uHeight,
//                1.0f*srcx/srcimage->uWidth, 1.0f*srcy/srcimage->uHeight};
//        glEnableVertexAttribArray(VERTEX_ARRAY);
//        glEnableVertexAttribArray(COLOR_ARRAY);
//        glEnableVertexAttribArray(COORD_ARRAY);
//
//        // Sets the vertex data to this attribute index
//        glVertexAttribPointer(VERTEX_ARRAY, 2, GL_SHORT, GL_FALSE, 0, pVertices2);
//        glVertexAttribPointer(COLOR_ARRAY, 4, GL_UNSIGNED_BYTE, GL_TRUE, 0, pColors2);
//        glVertexAttribPointer(COORD_ARRAY, 2, GL_FLOAT, GL_FALSE, 0, pCoords2);
//        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

//    float[] getVertexData(int x, int y, int width, int height, int srcx, int srcy, int srcwidth, int srcheight) {
//        float[] data =
//                {
//                        x, y,                          //V1
//                        (float) srcx / (float) width, (float) srcy / (float) height,             //Texture coordinate for V1
//
//                        x, y + height,                   //V2
//                        (float) srcx / (float) width, ((float) srcy + (float) srcheight) / (float) height,
//
//                        x + width, y,                          //V3
//                        ((float) srcx + (float) srcwidth) / (float) width, (float) srcy / (float) height,
//
//                        x + width, y + height,                   //V4
//                        ((float) srcx + (float) srcwidth) / (float) width, ((float) srcy + (float) srcheight) / (float) height
//
//                };
//
//        return data;
//
//    }
//
//    public void draw(float viewMatrix[]) {
//        this.draw(0,0, width, height, 0, 0, width, height, 1, viewMatrix);
//    }
}
