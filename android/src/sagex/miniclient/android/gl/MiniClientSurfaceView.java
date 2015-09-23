package sagex.miniclient.android.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by seans on 21/09/15.
 */
public class MiniClientSurfaceView extends GLSurfaceView {
    public MiniClientSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
    }

    public MiniClientSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
    }
}
