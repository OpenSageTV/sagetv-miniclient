package sagex.miniclient.android.opengl;

import android.opengl.GLES20;

import sagex.miniclient.uibridge.ImageHolder;
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
}
