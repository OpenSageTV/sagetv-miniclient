package sagex.miniclient;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.io.IOException;

import sagex.miniclient.gl.OpenGLUIManager;

public class MiniClientMain extends ApplicationAdapter {
    public static int WIDTH=720;
    public static int HEIGHT=480;

    public static MiniClientMain INSTANCE;

    ShapeRenderer background;
    Stage stage = null;

    Pixmap img;
    Pixmap larger;

    public Stage getStage() {
        return stage;
    }

	@Override
	public void create () {
        INSTANCE=this;
        Gdx.graphics.setContinuousRendering(false);

        background=new ShapeRenderer();
        Camera camera = new OrthographicCamera(WIDTH, HEIGHT);
        stage = new Stage(new StretchViewport(WIDTH, HEIGHT, camera));
        Gdx.input.setInputProcessor(getStage());
        Gdx.input.setCatchBackKey(true);

        //Gdx.graphics.requestRendering();

//		batch = new SpriteBatch();
//        Texture t  =new Texture("");
		img = new Pixmap(Gdx.files.internal("badlogic.jpg"));
        larger = new Pixmap(HEIGHT,HEIGHT, Pixmap.Format.RGBA8888);
        //larger.drawPixmap(img, 0, 0, 256, 256, 0, 0,256, 256);
        larger.drawPixmap(img, 0,0);
        larger.setColor(Color.CYAN);
        larger.drawLine(0,0,511,0);
	}

    boolean started=false;
    void startMiniClient() {
        if (!started) {
            started=true;
            MiniClient.startup(new String[]{});
            MgrServerInfo info = new MgrServerInfo("192.168.1.176", 31099, null);
            MiniClientConnection client = new MiniClientConnection("192.168.1.176", null, true, info, OpenGLUIManager.getUIFactory());
            try {
                client.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void resize (int width, int height) {
        getStage().getViewport().update(width, height, true);
        System.out.println("SIZE: width: " + width + "; height: " + height);
        System.out.println("VIEWPORT: width: " + getStage().getViewport().getScreenWidth() + "; height: " + getStage().getViewport().getScreenHeight());
        System.out.println("VIEWPORT: x: " + getStage().getViewport().getScreenX() + "; y: " + getStage().getViewport().getScreenY());
        System.out.println("WORLD: width: " + getStage().getViewport().getWorldWidth() + "; height: " + getStage().getViewport().getWorldHeight());
        Gdx.graphics.requestRendering();
        startMiniClient();
    }

    public void dispose() {
        getStage().dispose();
    }

    @Override
    public void render() {
        // 0xF5 F6 CE 00 (yellowish)
        // BD BD BD
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        getStage().act(Gdx.graphics.getDeltaTime());
        getStage().draw();

//        drawBackground(getStage().getBatch(), 1);
//        getStage().getBatch().begin();
//        Texture t =new Texture(larger);
//        getStage().getBatch().draw(t, 0, 0);
//        getStage().getBatch().end();
//        t.dispose();
//        System.out.println("RENDER");
    }

    protected void drawBackground(Batch batch, float parentAlpha) {
        batch.begin();
        getStage().getCamera().update();
        background.setProjectionMatrix(getStage().getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        background.begin(ShapeRenderer.ShapeType.Line);
        background.setColor(Color.RED.r, Color.RED.g, Color.RED.b, Color.RED.a * parentAlpha);
        background.rect(1, 1, WIDTH - 1, HEIGHT - 1);
        background.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.end();
    }

}
