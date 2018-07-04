package sagex.miniclient.android.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;

import sagex.miniclient.uibridge.Texture;

public class OpenGLTexture implements Texture {
    public OpenGLTexture(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width;
    public int height;
    public int texture[] = null;
    public int texture() {
        if (texture!=null)
            return texture[0];
        return -1;
    }
    public void texture(int t) {
        if (texture==null) {
            texture = new int[1];
        }
        texture[0]=t;
    }

    public void delete() {
        if (texture!=null) {
            GLES20.glDeleteTextures(1, texture, 0);
            texture = null;
        }
    }

    public void createTexture() {
        if (texture==null) texture=new int[1];
        GLES20.glGenTextures(1, texture, 0);
    }

    public void loadBitmap(Bitmap bitmap, String optName) {
        if (texture==null) createTexture();

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        try {
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        } catch (Throwable t) {
            Log.w("SAGEOPENGL", "Unable to load: " + optName);
            Log.w("SAGEOPENGL", "DENSITY: " + bitmap.getDensity());
            throw t;
        }

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle();
    }
}
