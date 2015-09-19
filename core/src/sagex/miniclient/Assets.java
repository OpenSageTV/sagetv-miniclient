package sagex.miniclient;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Created by seans on 17/02/15.
 */
public class Assets {
    private static final Assets instance = new Assets();

    //public static final String FONT_OSTRICH = "fonts/ostrich-rounded.ttf";
    public static final String FONT_OSTRICH = "fonts/Ubuntu-R.ttf";
    public static final String FONT_FILE_DEFAULT = FONT_OSTRICH;
    public static final String FONT_DEFAULT = "default-font.ttf";
    public static final String FONT_LARGE = "large-font.ttf";
    public static final String FONT_LEVEL_ITEM = "large-numbers.ttf";

    public static final String SKIN_ATLAS = "skin/uiskin.atlas";
    public static final String SKIN_JSON = "skin/uiskin.json";

    public Color red = Color.valueOf("EB8672");
    public Color green = Color.valueOf("B5D290");
    public Color blue = Color.valueOf("58BEEC");
    public Color purple = Color.valueOf("A186BE");
    public Color orange = Color.valueOf("F59533");
    public Color transparentDark = Color.valueOf("00000060");
    public Color obstacle1 = purple;
    public Color editable = transparentDark;
    public Color white = Color.WHITE;
    public Color charcaol = Color.valueOf("36454f");

    public static Assets get() {
        return instance;
    }

    public AssetManager assetManager = new AssetManager();
    public Skin skin = null;
    public boolean loaded=false;

    public void queueLoading() {
        if (loaded) return;
        // load the fonts
        FileHandleResolver resolver = new InternalFileHandleResolver();
        assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        FreetypeFontLoader.FreeTypeFontLoaderParameter size1Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size1Params.fontFileName = FONT_FILE_DEFAULT;
        size1Params.fontParameters.size = 18;
        //size1Params.fontParameters.characters="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+-?.=";
        size1Params.fontParameters.kerning = true;
        size1Params.fontParameters.magFilter = Texture.TextureFilter.Linear;
        size1Params.fontParameters.minFilter = Texture.TextureFilter.Linear;

        assetManager.load(FONT_DEFAULT, BitmapFont.class, size1Params);

        FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size2Params.fontFileName = FONT_FILE_DEFAULT;
        size2Params.fontParameters.size = 36;
        //size2Params.fontParameters.characters="ABCDEFGHIJKLMNOPQRSTUVWXYZ?+-.";
        size2Params.fontParameters.kerning = true;
        size2Params.fontParameters.magFilter = Texture.TextureFilter.Linear;
        size2Params.fontParameters.minFilter = Texture.TextureFilter.Linear;

        assetManager.load(FONT_LARGE, BitmapFont.class, size2Params);

        // load the skin
        assetManager.load(SKIN_ATLAS, TextureAtlas.class);
    }

    public void setSkin() {
        if (skin==null) {
            skin = new Skin();
            skin.addRegions(assetManager.get(SKIN_ATLAS, TextureAtlas.class));
            skin.add("default-font", assetManager.get(FONT_DEFAULT, BitmapFont.class), BitmapFont.class);
            skin.load(Gdx.files.internal(SKIN_JSON));
        }
    }

    /**
     * needs to be called during the render phase
     */
    public boolean update() {
        if (loaded) return true;
        return assetManager.update();
    }

    public void dispose() {
        assetManager.dispose();
        assetManager = new AssetManager();
        skin=null;
    }

    public BitmapFont getButtonFont() {
        return assetManager.get(FONT_LARGE, BitmapFont.class);
    }
    public BitmapFont getFont(String id) {
        return assetManager.get(id, BitmapFont.class);
    }
}
