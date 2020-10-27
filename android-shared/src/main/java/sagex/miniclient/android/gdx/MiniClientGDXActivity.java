package sagex.miniclient.android.gdx;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import sagex.miniclient.android.R;
import sagex.miniclient.android.UIActivityLifeCycleHandler;

/**
 * Created by seans on 20/09/15.
 */
public class MiniClientGDXActivity extends AndroidApplication implements UIActivityLifeCycleHandler.IActivityCallback<MiniClientGDXRenderer> {
    UIActivityLifeCycleHandler<MiniClientGDXRenderer> uiActivityLifeCycleHandler;
    public MiniClientGDXActivity() {
        uiActivityLifeCycleHandler = new UIActivityLifeCycleHandler<>(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiActivityLifeCycleHandler.onCreate(this);
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
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        uiActivityLifeCycleHandler.onWindowFocusChanged(hasFocus);
    }

    @Override
    public View createAndConfigureUIView(UIActivityLifeCycleHandler<MiniClientGDXRenderer> handler) {
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        //cfg.useGL20 = false;
        // we need to change the default pixel format - since it does not include an alpha channel
        // we need the alpha channel so the camera preview will be seen behind the GL scene
        cfg.r = 8;
        cfg.g = 8;
        cfg.b = 8;
        cfg.a = 8;

        View miniClientView = initializeForView(handler.getUIRenderer(), cfg);

        if (graphics.getView() instanceof SurfaceView) {
            GLSurfaceView glView = (GLSurfaceView) graphics.getView();
            // This is needed or else we won't see OSD over video
            glView.setZOrderOnTop(true);
            // This is needed or else we will not see the video playing behind the OSD
            glView.getHolder().setFormat(PixelFormat.RGBA_8888);
        }
        return miniClientView;
    }

    @Override
    public MiniClientGDXRenderer createUIRenderer(UIActivityLifeCycleHandler<MiniClientGDXRenderer> handler) {
        return new MiniClientGDXRenderer(handler, handler.getClient());
    }

    @Override
    public int getLayoutViewId(UIActivityLifeCycleHandler<MiniClientGDXRenderer> handler) {
        return R.layout.miniclientgdx_layout;
    }
}
