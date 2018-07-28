package sagex.miniclient.android.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import sagex.miniclient.uibridge.Texture;

public class OpenGLTexture implements Texture {
    static final int debug = 1;
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

    void debug(String msg, Object... args) {
        if (debug>0) {
            System.out.printf(msg + "\n", args);
        }
    }

    public int texture() {
        if (texture!=null)
            return texture[0];
        return -1;
    }

    public void delete() {
        if (texture!=null) {
            debug("Deleting Texture");
            new Exception("DELETING TEXTURE").printStackTrace();
            GLES20.glDeleteTextures(1, texture, 0);
            texture = null;
        }
    }

    public void createTexture() {
        if (texture==null) {
            debug("New Texture: %d x %d", this.width, this.height);
            texture=new int[1];
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

    }

    public void set(Bitmap bitmap, String optName) {
        this.width=bitmap.getWidth();
        this.height=bitmap.getHeight();

        if (texture==null) createTexture();

        debug("Setting Bitmap: %s", optName);

        try {
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        } catch (Throwable t) {
            Log.w("SAGEOPENGL", "Unable to load: " + optName);
            throw t;
        }

        // GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle();
    }

    public void draw(int x, int y, int width, int height, int srcx, int srcy, int srcwidth, int srcheight, int blend, float[] viewMatrix) {
        ShaderUtils.useProgram(ShaderUtils.Shader.Texture);

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        if(height<0)
        {
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);
            height*=-1;
        }

        if(width<0) width*=-1;

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        GLES20.glUniform1i(ShaderUtils.TEXTURE_PROGRAM_uTEXTURE, 0);

        float[] data = getVertexData(x, y, width, height, srcx, srcy, srcwidth, srcheight);

        // Again, a FloatBuffer will be used to pass the values
        FloatBuffer b = ByteBuffer.allocateDirect(data.length * FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
        b.put(data);

        // Position of our image
        b.position(POSITION_OFFSET);
        GLES20.glVertexAttribPointer(ShaderUtils.TEXTURE_PROGRAM_aPOSITION, POSITION_SIZE, GLES20.GL_FLOAT, false, TOTAL_SIZE * FLOAT_SIZE, b);
        GLES20.glEnableVertexAttribArray(ShaderUtils.TEXTURE_PROGRAM_aPOSITION);

        // Positions of the texture
        b.position(TEXTURE_OFFSET);
        GLES20.glVertexAttribPointer(ShaderUtils.TEXTURE_PROGRAM_aTEXPOS, TEXTURE_SIZE, GLES20.GL_FLOAT, false, TOTAL_SIZE * FLOAT_SIZE, b);
        GLES20.glEnableVertexAttribArray(ShaderUtils.TEXTURE_PROGRAM_aTEXPOS);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(ShaderUtils.TEXTURE_PROGRAM_uSCREEN, 1, false, viewMatrix , 0);

        // Clear the screen and draw the rectangle
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

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

    float[] getVertexData(int x, int y, int width, int height, int srcx, int srcy, int srcwidth, int srcheight) {
        float[] data =
                {
                        x, y,                          //V1
                        (float) srcx / (float) width, (float) srcy / (float) height,             //Texture coordinate for V1

                        x, y + height,                   //V2
                        (float) srcx / (float) width, ((float) srcy + (float) srcheight) / (float) height,

                        x + width, y,                          //V3
                        ((float) srcx + (float) srcwidth) / (float) width, (float) srcy / (float) height,

                        x + width, y + height,                   //V4
                        ((float) srcx + (float) srcwidth) / (float) width, ((float) srcy + (float) srcheight) / (float) height

                };

        return data;

    }

    public void draw(float viewMatrix[]) {
        this.draw(0,0, width, height, 0, 0, width, height, 1, viewMatrix);
    }
}
