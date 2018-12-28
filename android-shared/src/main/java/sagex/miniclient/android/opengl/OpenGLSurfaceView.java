package sagex.miniclient.android.opengl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

public class OpenGLSurfaceView extends GLSurfaceView {
    public OpenGLSurfaceView(Context context, OpenGLRenderer renderer) {
        super(context);

        renderer.setView(this);
        //setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        setRenderer(renderer);
        setZOrderOnTop(true);
        setZOrderMediaOverlay(true);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
