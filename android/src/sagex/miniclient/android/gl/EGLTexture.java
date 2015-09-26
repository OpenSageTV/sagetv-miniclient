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

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import sagex.miniclient.uibridge.ImageHolder;

/**
 * Created by seans on 19/09/15.
 */
public class EGLTexture {
    private static final String TAG = "EGLTexture";
    private int[] texture;
    private Bitmap bitmap;
    public boolean loaded=false;
    public int width;
    public int height;

    boolean createFrameBuffer=false;

    public EGLTexture(boolean createFrameBuffer, int width, int height) {
        this.createFrameBuffer=createFrameBuffer;
        this.width=width;
        this.height=height;
    }

    public EGLTexture(Bitmap bitmap) {
        this.bitmap=bitmap;
        this.width=bitmap.getWidth();
        this.height=bitmap.getHeight();
    }

    public void load() {
        if (loaded) return;

        if (createFrameBuffer) {
            texture = new int[2];
            texture[0] = createTargetTexture(width,height)[0];
            texture[1] = createFrameBuffer(width,height,texture[0])[0];
        } else {
            // http://gamedev.stackexchange.com/questions/10829/loading-png-textures-for-use-in-android-opengl-es1
            //
            texture = new int[1];
            GLES20.glGenTextures(1, texture, 0);
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

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }
        loaded=true;
    }

    private int[] createTargetTexture(int width, int height) {
        int texture;
        int[] textures = new int[1];

        GLES20.glGenTextures(1, textures, 0);
        texture = textures[0];
        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, texture);
        GLES20.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, width, height,
                0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_NEAREST);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR);
        GLES20.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_CLAMP_TO_EDGE);

        return textures;
    }


    private int[] createFrameBuffer(int width, int height, int targetTextureId) {
        int framebuffer;
        int[] framebuffers = new int[1];
        GLES20.glGenFramebuffers(1, framebuffers, 0);
        framebuffer = framebuffers[0];

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer);

        GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GL10.GL_TEXTURE_2D, targetTextureId, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return framebuffers;
    }

    public void bindFramebuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, texture[1]);
    }

    public void unbindFramebuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public int[] get() {
        return texture;
    }
}
