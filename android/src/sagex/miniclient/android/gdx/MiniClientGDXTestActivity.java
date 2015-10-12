package sagex.miniclient.android.gdx;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import butterknife.Bind;
import butterknife.ButterKnife;
import sagex.miniclient.android.R;

import static sagex.miniclient.android.AppUtil.hideSystemUI;

/**
 * Created by seans on 20/09/15.
 */
public class MiniClientGDXTestActivity extends AndroidApplication implements ApplicationListener {
    @Bind(R.id.surface)
    FrameLayout uiFrameHolder;

    Stage stage;
    Batch batch;
    Camera camera;
    Viewport viewport;
    ShapeRenderer shapeRenderer;


    private View miniClientView;

    public MiniClientGDXTestActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideSystemUI(this);

        setContentView(R.layout.miniclientgltest_layout);
        ButterKnife.bind(this);

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        //cfg.useGL20 = false;
        // we need to change the default pixel format - since it does not include an alpha channel
        // we need the alpha channel so the camera preview will be seen behind the GL scene
        cfg.r = 8;
        cfg.g = 8;
        cfg.b = 8;
        cfg.a = 8;

        miniClientView = initializeForView(this, cfg);

        if (graphics.getView() instanceof SurfaceView) {
            GLSurfaceView glView = (GLSurfaceView) graphics.getView();
            glView.setZOrderOnTop(true);
            //glView.setZOrderMediaOverlay(true);
            // force alpha channel - I'm not sure we need this as the GL surface is already using alpha channel
            glView.getHolder().setFormat(PixelFormat.RGBA_8888);
        }

        uiFrameHolder.addView(miniClientView);
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(1920, 1080, camera);
        stage = new Stage(viewport);
        batch = stage.getBatch();
        shapeRenderer = new ShapeRenderer();

        Gdx.graphics.setContinuousRendering(false);
        Gdx.graphics.requestRendering();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().setWorldSize(1920, 1080);
        stage.getViewport().update(width, height, true);
        Gdx.graphics.requestRendering();
    }

    @Override
    public void render() {
        Gdx.gl20.glClearColor(0, 0, 0, 0);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // draw whatever we have on the stage
        stage.draw();

        // draw red rectangle
        drawRect(10, 10, 1000, 1000);

        // draw a blue box
        fillRect(50, 50, 800, 800);

        // punch a hole in the surface (ie, clear an area) so that we can see the view that is
        // behind this view
        clearRect(200, 200, 1600, 600);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    public void drawRect(final int x, final int y, final int width, final int height) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(x, y, width, height, Color.RED, Color.RED, Color.RED, Color.RED);
        shapeRenderer.end();
    }

    public void fillRect(final int x, final int y, final int width, final int height) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(x, y, width, height, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE);
        shapeRenderer.end();
    }

    public void clearRect(final int x, final int y, final int width, final int height) {
//                Gdx.gl.glEnable(GL20.GL_BLEND);
//                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);


        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.CLEAR);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

//                Gdx.gl.glDisable(GL20.GL_BLEND);
    }

}
