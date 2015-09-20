package sagex.miniclient.android.gl;

import android.opengl.GLES20;

import java.io.File;
import java.io.InputStream;

import sagex.miniclient.ImageHolder;
import sagex.miniclient.UIManager;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Scale;

/**
 * Created by seans on 19/09/15.
 */
public class EGLUIManager implements UIManager<EGLTexture> {
    GLES20 gl;

    @Override
    public void GFXCMD_INIT() {

    }

    @Override
    public void GFXCMD_DEINIT() {

    }

    @Override
    public void close() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void hideCursor() {

    }

    @Override
    public void showBusyCursor() {

    }

    @Override
    public void drawRect(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL) {

    }

    @Override
    public void fillRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL) {

    }

    @Override
    public void clearRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL) {

    }

    @Override
    public void drawOval(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {

    }

    @Override
    public void fillOval(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {

    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int thickness, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {

    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {

    }

    @Override
    public void drawTexture(int x, int y, int width, int height, int handle, ImageHolder<?> img, int srcx, int srcy, int srcwidth, int srcheight, int blend) {
//        int texturet[] = ((EGLTexture) img.get()).get();
//        if (texturet != null) {
//            gl.glEnable(gl.GL_BLEND);
//            gl.glEnable(gl.GL_TEXTURE_2D);
//            if (texturet.length == 4) // This is a rendering buffer
//            {
//                // System.out.println("Render with fb texture of : " +
//                // texturet[1]);
//                gl.glBindTexture(gl.GL_TEXTURE_2D, texturet[1]);
//            } else {
//                gl.glBindTexture(gl.GL_TEXTURE_2D, texturet[0]);
//            }
//            gl.glBlendFunc(gl.GL_ONE, gl.GL_ONE_MINUS_SRC_ALPHA);
//            if (height < 0) {
//                gl.glBlendFunc(gl.GL_ONE, gl.GL_ZERO);
//                height *= -1;
//            }
//            if (width >= 0) // not font
//            {
//                setGLColor(gl, blend);
//                // gl.glColor4f(1.0f,1.0f,1.0f,1.0f);
//
//                GLES30.glBegin(gl.GL_QUADS);
//                GLES30.glTexCoord2f(srcx, srcy);
//                gl.glVertex2f(x, y);
//                gl.glTexCoord2f(srcx + srcwidth, srcy);
//                gl.glVertex2f(x + width, y);
//                gl.glTexCoord2f(srcx + srcwidth, srcy + srcheight);
//                gl.glVertex2f(x + width, y + height);
//                gl.glTexCoord2f(srcx, srcy + srcheight);
//                gl.glVertex2f(x, y + height);
//                gl.glEnd();
//                gl.glBlendFunc(gl.GL_ONE, gl.GL_ONE_MINUS_SRC_ALPHA);
//                gl.glColor4f(1, 1, 1, 1);
//            } else // font
//            {
//                setGLColor(gl, blend);
//                width *= -1;
//                gl.glBegin(gl.GL_QUADS);
//                gl.glTexCoord2f(srcx, srcy);
//                gl.glVertex2f(x, y);
//                gl.glTexCoord2f(srcx + srcwidth, srcy);
//                gl.glVertex2f(x + width, y);
//                gl.glTexCoord2f(srcx + srcwidth, srcy + srcheight);
//                gl.glVertex2f(x + width, y + height);
//                gl.glTexCoord2f(srcx, srcy + srcheight);
//                gl.glVertex2f(x, y + height);
//                gl.glEnd();
//                gl.glBlendFunc(gl.GL_ONE, gl.GL_ONE_MINUS_SRC_ALPHA);
//                GLES10.glColor4f(1, 1, 1, 1);
//            }
//            gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
//            gl.glDisable(gl.GL_TEXTURE_2D);
//        } else {
//            System.out.println("ERROR invalid handle passed for texture rendering of: " + handle);
//        }
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int argb1, int argb2) {

    }

    @Override
    public ImageHolder<EGLTexture> loadImage(int width, int height) {
//        int texturet[] = new int[1];
//        byte img[] = new byte[width*height*4];
//        gl.glGenTextures(1, texturet, 0);
//        gl.glEnable(gl.GL_TEXTURE_2D);
//        gl.glBindTexture(gl.GL_TEXTURE_2D, texturet[0]);
//        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR);
//        gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, 4, width, height, 0,
//                gl.GL_BGRA, bigendian ? gl.GL_UNSIGNED_INT_8_8_8_8_REV : gl.GL_UNSIGNED_BYTE, ByteBuffer.wrap(img));
//        gl.glDisable(gl.GL_TEXTURE_2D);
//
//        return new ImageHolder<EGLTexture>(new EGLTexture(texturet));
        return null;
    }

    @Override
    public void loadImageLine(int handle, ImageHolder<?> image, int line, int len2, byte[] cmddata) {
//        int texturet[] = ((EGLTexture)image.get()).get();
//        int texturedata[] = new int[len2];
//        int dataPos = 12;
//        for (int i = 0; i < len2 / 4; i++, dataPos += 4) {
//            texturedata[i] = GFXCMD2.readInt(dataPos, cmddata);
//        }
//        gl.glEnable(gl.GL_TEXTURE_2D);
//        gl.glBindTexture(gl.GL_TEXTURE_2D, texturet[0]);
//        gl.glTexSubImage2D(gl.GL_TEXTURE_2D, 0, 0, line, len2 / 4, 1, gl.GL_BGRA,
//                bigendian ? gl.GL_UNSIGNED_INT_8_8_8_8_REV : gl.GL_UNSIGNED_BYTE, IntBuffer.wrap(texturedata));
//        gl.glDisable(gl.GL_TEXTURE_2D);
    }

    @Override
    public ImageHolder<EGLTexture> createSurface(int handle, int width, int height) {
        return null;
    }

    @Override
    public ImageHolder<EGLTexture> readImage(File cachedFile) throws Exception {
        return null;
    }

    @Override
    public ImageHolder<EGLTexture> readImage(InputStream bais) throws Exception {
        return null;
    }

    @Override
    public ImageHolder<EGLTexture> newImage(int destWidth, int destHeight) {
        return loadImage(destWidth, destHeight);
    }

    @Override
    public void setTargetSurface(int handle, ImageHolder<?> image) {

    }

    @Override
    public void flipBuffer() {

    }

    @Override
    public void startFrame() {

    }

    @Override
    public void xfmImage(int srcHandle, ImageHolder<?> srcImg, int destHandle, ImageHolder<?> destImg, int destWidth, int destHeight, int maskCornerArc) {

    }

    @Override
    public boolean hasGraphicsCanvas() {
        return false;
    }

    @Override
    public Dimension getMaxScreenSize() {
        return null;
    }

    @Override
    public Dimension getScreenSize() {
        return null;
    }

    @Override
    public void setFullScreen(boolean b) {

    }

    @Override
    public void setSize(int w, int h) {

    }

    @Override
    public void invokeLater(Runnable runnable) {

    }

    @Override
    public Scale getScale() {
        return null;
    }
}
