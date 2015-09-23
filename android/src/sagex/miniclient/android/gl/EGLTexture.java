package sagex.miniclient.android.gl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import sagex.miniclient.uibridge.ImageHolder;

/**
 * Created by seans on 19/09/15.
 */
public class EGLTexture {
    private static final String TAG = "EGLTexture";
    private int[] texture;
    private File file=null;
    public boolean loaded=false;

    public EGLTexture(File file) {
        this.file=file;
    }

    public void load() {
        // http://gamedev.stackexchange.com/questions/10829/loading-png-textures-for-use-in-android-opengl-es1
        //
        try {
            FileInputStream fis = new FileInputStream(file);
            texture = new int[1];
            GLES20.glGenTextures(1, texture, 0);
            Log.d(TAG, "Loading Texture: " + file);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);

            // Set wrapping mode
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            loaded=true;
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int[] get() {
        return texture;
    }
}
