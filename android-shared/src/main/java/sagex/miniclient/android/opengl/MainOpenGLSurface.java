package sagex.miniclient.android.opengl;

import android.opengl.GLES20;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.uibridge.Dimension;

public class MainOpenGLSurface extends OpenGLSurface {
    private static Logger log = LoggerFactory.getLogger(MainOpenGLSurface.class);
    private Dimension fullScreen;

    public MainOpenGLSurface(Dimension uiSize, Dimension fullScreenSize) {
        super(0, uiSize.width, uiSize.height);
        this.fullScreen = fullScreenSize;
    }

    public int buffer() {
        return 0;
    }

    @Override
    public int texture() {
        return 0;
    }

    @Override
    public void delete() {
        throw new RuntimeException("Cannot Delete the Main Surface");
    }

    public MainOpenGLSurface createSurface() {
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        return this;
    }

    public void bind() {
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glViewport(0, 0, fullScreen.width, fullScreen.height);
        this.bound = true;
    }

    public void unbind() {
        log.warn("Unbinding Main Surface is not supported");
        bound = false;
    }
}
