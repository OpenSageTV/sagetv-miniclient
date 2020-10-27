package sagex.miniclient.android.opengl;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import sagex.miniclient.android.R;
import sagex.miniclient.android.UIActivityLifeCycleHandler;

/**
 * Created by seans on 20/09/15.
 */
public class MiniClientOpenGLActivity extends Activity implements UIActivityLifeCycleHandler.IActivityCallback<OpenGLRenderer> {
    UIActivityLifeCycleHandler<OpenGLRenderer> uiActivityLifeCycleHandler;

    public MiniClientOpenGLActivity() {
        uiActivityLifeCycleHandler = new UIActivityLifeCycleHandler<>(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiActivityLifeCycleHandler.onCreate(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        uiActivityLifeCycleHandler.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiActivityLifeCycleHandler.onResume(this);
    }

    @Override
    protected void onPause() {
        uiActivityLifeCycleHandler.onPause(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        uiActivityLifeCycleHandler.onDestroy();
        super.onDestroy();
    }

    @Override
    public View createAndConfigureUIView(UIActivityLifeCycleHandler<OpenGLRenderer> handler) {
        return new OpenGLSurfaceView(this, handler.getUIRenderer());
    }

    @Override
    public OpenGLRenderer createUIRenderer(UIActivityLifeCycleHandler<OpenGLRenderer> handler) {
        return new OpenGLRenderer(handler, handler.getClient());
    }

    @Override
    public int getLayoutViewId(UIActivityLifeCycleHandler<OpenGLRenderer> handler) {
        return R.layout.miniclientopengl_layout;
    }
}
