package sagex.miniclient;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class MiniClientMain extends ApplicationAdapter {
    public static int WIDTH=720;
    public static int HEIGHT=480;

    public static MiniClientMain INSTANCE;
    public static boolean CONTINUOUS_RENDERING=false;

    ShapeRenderer background;
    Stage stage = null;

    public Stage getStage() {
        return stage;
    }

	@Override
	public void create () {
        INSTANCE=this;
        Gdx.graphics.setContinuousRendering(CONTINUOUS_RENDERING);

        background=new ShapeRenderer();
        Camera camera = new OrthographicCamera(WIDTH, HEIGHT);
        stage = new Stage(new StretchViewport(WIDTH, HEIGHT, camera));
        Gdx.input.setInputProcessor(getStage());
        Gdx.input.setCatchBackKey(true);

        Assets.get().queueLoading();
        Assets.get().assetManager.update(2000);
        Assets.get().setSkin();

        mainMenu();
	}

    public static void requestRendering() {
        if (!CONTINUOUS_RENDERING) Gdx.graphics.requestRendering();
    }

    public void resize (int width, int height) {
        getStage().getViewport().update(width, height, true);
        System.out.println("SIZE: width: " + width + "; height: " + height);
        System.out.println("VIEWPORT: width: " + getStage().getViewport().getScreenWidth() + "; height: " + getStage().getViewport().getScreenHeight());
        System.out.println("VIEWPORT: x: " + getStage().getViewport().getScreenX() + "; y: " + getStage().getViewport().getScreenY());
        System.out.println("WORLD: width: " + getStage().getViewport().getWorldWidth() + "; height: " + getStage().getViewport().getWorldHeight());
        Gdx.graphics.requestRendering();
    }

    public void dispose() {
        getStage().dispose();
    }

    @Override
    public void render() {
        //if (!CONTINUOUS_RENDERING) System.out.println("BEGIN RENDER");

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        getStage().act(Gdx.graphics.getDeltaTime());
        getStage().draw();
        //if (!CONTINUOUS_RENDERING) System.out.println("END RENDER");
    }

    public void mainMenu() {
        stage.addActor(new sagex.miniclient.ui.ServerSelectorView());
        requestRendering();
    }
}
