package sagex.miniclient.android.opengl;

import android.opengl.GLES20;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainOpenGLSurface extends OpenGLSurface {
    private static Logger log = LoggerFactory.getLogger(MainOpenGLSurface.class);

    public MainOpenGLSurface(int w, int h) {
        super(w, h);
    }

    public int buffer() {
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
        log.debug("Binding Framebuffer Surface: {}, {}x{}", buffer(), width, height);

        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //GLES20.glFramebufferTexture2D( GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture(), 0 );
        GLES20.glViewport(0, 0, width, height);
        //GLES20.glClearColor(0,0,1,1);
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        this.bound = true;
    }

    public void unbind() {
        log.warn("Unbinding Main Surface is not supported");
        bound = false;
    }

    public void draw() {
        throw new RuntimeException("MainSurface.draw() is unsupported");
    }
}
