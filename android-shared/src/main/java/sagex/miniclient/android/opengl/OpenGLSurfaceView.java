package sagex.miniclient.android.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class OpenGLSurfaceView extends GLSurfaceView {
    public OpenGLSurfaceView(Context context, OpenGLRenderer renderer) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
