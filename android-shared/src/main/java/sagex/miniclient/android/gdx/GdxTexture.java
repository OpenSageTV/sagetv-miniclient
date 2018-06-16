package sagex.miniclient.android.gdx;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.uibridge.Disposable;

/**
 * Created by seans on 26/09/15.
 */
public class GdxTexture implements Disposable, sagex.miniclient.uibridge.Texture {
    private static final Logger log = LoggerFactory.getLogger(GdxTexture.class);

    int width;
    int height;
    boolean isFrameBuffer = false;
    FrameBuffer frameBuffer;
    boolean isFrameBufferInUse = false;

    Bitmap bitmap;
    Texture texture;

    public GdxTexture(Bitmap bitmap) {
        this.bitmap = bitmap;
        if (bitmap != null) {
            width = bitmap.getWidth();
            height = bitmap.getHeight();
        }
    }

    public GdxTexture(int width, int height) {
        this.width = width;
        this.height = height;
        this.isFrameBuffer = true;
    }

    public void load() {
        if (isFrameBuffer) {
            createFrameBuffer();
            return;
        }
        if (bitmap == null) return;
        if (texture != null) return;

        Texture tex = new Texture(bitmap.getWidth(), bitmap.getHeight(), Pixmap.Format.RGBA8888);
        Gdx.gl20.glBindTexture(GL20.GL_TEXTURE_2D, tex.getTextureObjectHandle());
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        Gdx.gl20.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        bitmap.recycle();
        bitmap = null;

        texture = tex;
    }

    private void createFrameBuffer() {
        if (frameBuffer == null) {
            frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false, false);
            frameBuffer.begin();
            Gdx.gl20.glClearColor(0, 0, 0, 0);
            Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
            frameBuffer.end();
        }
    }

    public void bindFrameBuffer() {
        if (frameBuffer == null) {
            createFrameBuffer();
        }
        if (isFrameBufferInUse) {
            log.error("Attempting to Bind Framebuffer when it is already in use");
        }
        // this ensures that the we get the updated texture from the framebuffer when texture() is called.
        texture = null;
        isFrameBufferInUse = true;
        frameBuffer.begin();
    }

    public void unbindFrameBuffer() {
        if (frameBuffer != null && isFrameBufferInUse) {
            frameBuffer.end();
        }
        isFrameBufferInUse = false;
    }

    public Texture texture() {
        if (texture != null) {
            return texture;
        }

        load();

        // load the texture from the buffer
        if (isFrameBuffer) {
            if (isFrameBufferInUse) {
                unbindFrameBuffer();
            }
            texture = frameBuffer.getColorBufferTexture();
        }
        return texture;
    }

    /**
     * Release everything about this Texture.
     */
    public void dispose() {
        if (texture != null) {
            try {
                texture.dispose();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (frameBuffer != null) {
            try {
                frameBuffer.dispose();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (bitmap != null) {
            try {
                bitmap.recycle();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        texture = null;
        frameBuffer = null;
        isFrameBufferInUse = false;
        bitmap = null;
    }
}
