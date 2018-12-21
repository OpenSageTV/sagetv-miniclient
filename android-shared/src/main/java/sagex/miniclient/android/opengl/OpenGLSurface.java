package sagex.miniclient.android.opengl;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenGLSurface extends OpenGLTexture {
    private static Logger log = LoggerFactory.getLogger(OpenGLSurface.class);

    public float[] viewMatrix = new float[16];
    public int[] buffer = null;
    public int[] renderBuffer = null;
    boolean bound=false;
    int id;

    public OpenGLSurface(int id, int w, int h) {
        super(w,h);
        this.id = id;
        Matrix.orthoM(viewMatrix, 0, 0, w, h, 0, 0, 1);
        flip = true;
    }


    public int buffer() {
        if (buffer==null) return -1;
        return buffer[0];
    }

    @Override
    public void delete() {
        if (buffer!=null) {
            GLES20.glDeleteFramebuffers(1, buffer, 0);
            buffer=null;
        }
        super.delete();
    }

    public OpenGLSurface createSurface()
    {
        delete();

        buffer = new int[1];
        GLES20.glGenFramebuffers(1, buffer, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, buffer[0]);

        //depth renderbuffer
        renderBuffer = new int[1];
        GLES20.glGenRenderbuffers(1, renderBuffer, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBuffer[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);


        //texture
        texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glFramebufferTexture2D( GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture(), 0 );

        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBuffer[0]);

        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if(status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.d("FBORenderer", "Framebuffer incomplete. Status: " + status);

            throw new RuntimeException("Error creating FBO");
        }

        GLES20.glClearColor(0,0,0,0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        OpenGLUtils.logGLErrors("Create Surface");

        bound=false;

        return this;
    }

    public void bind() {
        log.debug("Binding Framebuffer Surface: ({}), {}x{}", id, width, height);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, buffer());
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if(status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            log.debug("Framebuffer Did Not Bind. Status: {}", status);

            throw new RuntimeException("Error creating FBO");
        }

        GLES20.glViewport(0, 0, width, height);

        OpenGLUtils.logGLErrors("Surface.bind()");
        bound=true;
    }

    public void unbind() {
        log.debug("Unbinding Framebuffer Surface: ({})", id);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        bound=false;
    }

    public static OpenGLSurface get(OpenGLTexture openGLTexture) {
        assert openGLTexture instanceof OpenGLSurface;
        return (OpenGLSurface)openGLTexture;
    }
}
