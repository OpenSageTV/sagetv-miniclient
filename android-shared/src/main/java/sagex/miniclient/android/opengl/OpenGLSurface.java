package sagex.miniclient.android.opengl;

import android.opengl.GLES20;

public class OpenGLSurface extends OpenGLTexture {
    public float[] viewMatrix = new float[16];
    public int[] buffer = null;

    public OpenGLSurface(int w, int h) {
        super(w,h);
    }


    public int buffer() {
        if (buffer==null) return -1;
        return buffer[0];
    }

    @Override
    public void delete() {
        super.delete();
        if (buffer!=null) {
            GLES20.glDeleteFramebuffers(1, buffer, 0);
            buffer=null;
        }
    }

    public OpenGLSurface createSurface()
    {
        delete();
        GLES20.glGenFramebuffers(1, buffer, 0);

        createTexture();

        // Binds this texture handle so we can load the data into it
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture());
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, buffer());
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture(), 0);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        return this;
    }

    public static OpenGLSurface get(OpenGLTexture openGLTexture) {
        assert openGLTexture instanceof OpenGLSurface;
        return (OpenGLSurface)openGLTexture;
    }
}
