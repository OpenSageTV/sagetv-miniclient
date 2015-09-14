package sagex.miniclient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import sagex.miniclient.uibridge.Dimension;

public interface UIManager<Image,Font> {
	void init();
	void dispose();
	void close();
	void refresh();
	
	void hideCursor();
	void showBusyCursor();
	
	void drawRect(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL);
	void fillRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL);
	void clearRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL);
	void drawOval(int x, int y, int width, int height, int thickness, int argbTL, int argbTR, int argbBR, int argbBL, int clipX,
				  int clipY, int clipW, int clipH);
	void fillOval(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY,
				  int clipW, int clipH);
	void drawRoundRect(int x, int y, int width, int height, int thickness, int arcRadius, int argbTL, int argbTR, int argbBR,
					   int argbBL, int clipX, int clipY, int clipW, int clipH);
	void fillRoundRect(int x, int y, int width, int height, int arcRadius, int argbTL, int argbTR, int argbBR, int argbBL,
					   int clipX, int clipY, int clipW, int clipH);
	void drawTexture(int x, int y, int width, int height, int handle, ImageHolder<?> img, int srcx, int srcy, int srcwidth, int srcheight, int blend);
	void drawLine(int x1, int y1, int x2, int y2, int argb1, int argb2);
	ImageHolder<Image> loadImage(int width, int height);
	ImageHolder<Image> createSurface(int width, int height);
	ImageHolder<Image> readImage(File cachedFile) throws Exception;
	ImageHolder<Image> readImage(ByteArrayInputStream bais) throws Exception;
	ImageHolder<Image> newImage(int destWidth, int destHeight);
	void setTargetSurface(int handle, ImageHolder<?> image);
	FontHolder<Font> createFont(InputStream fis);
	FontHolder<Font> loadFont(String string, int style, int size);
	FontHolder<Font> deriveFont(FontHolder<?> cachedFont, float size);
	void flipBuffer();
	void startFrame();
	void loadImageLine(int handle, ImageHolder<?> image, int line, int len2, byte[] cmddata);
	void xfmImage(int srcHandle, ImageHolder<?> srcImg, int destHandle, ImageHolder<?> destImg, int destWidth, int destHeight, int maskCornerArc);
	boolean hasGraphicsCanvas();
	void drawText(int x, int y, int textlen, String text, int fontHandle, FontHolder<?> fontHolder, int argb, int clipX, int clipY, int clipW, int clipH);
	Dimension getMaxScreenSize();
	Dimension getScreenSize();
	void setFullScreen(boolean b);
	void setSize(int w, int h);
	void invokeLater(Runnable runnable);

}
