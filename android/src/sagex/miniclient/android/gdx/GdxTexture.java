package sagex.miniclient.android.gdx;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Created by seans on 26/09/15.
 */
public class GdxTexture {
    private Bitmap bitmap;
    private Texture texture;

    public GdxTexture(Bitmap bitmap) {
        this.bitmap=bitmap;
    }

    public void load() {
        if (bitmap==null) return;
        if (texture!=null) return;

        Texture tex = new Texture(bitmap.getWidth(), bitmap.getHeight(), Pixmap.Format.RGBA8888);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex.getTextureObjectHandle());
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        bitmap.recycle();
        bitmap=null;

        texture=tex;
    }

    public Texture texture() {
        if (texture==null) {
            load();
        }
        return texture;
    }
}
