package sagex.miniclient.uibridge;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;

import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.MiniPlayerPlugin;

public interface UIManager<Image> {
	void GFXCMD_INIT();
	void GFXCMD_DEINIT();

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
	void drawTexture(int x, int y, int width, int height, int handle, ImageHolder<Image> img, int srcx, int srcy, int srcwidth, int srcheight, int blend);
	void drawLine(int x1, int y1, int x2, int y2, int argb1, int argb2);
	ImageHolder<Image> loadImage(int width, int height);
    void unloadImage(int handle, ImageHolder<Image> bi);
    ImageHolder<Image> createSurface(int handle, int width, int height);
	ImageHolder<Image> readImage(File cachedFile) throws Exception;
	ImageHolder<Image> readImage(InputStream bais) throws Exception;
	ImageHolder<Image> newImage(int destWidth, int destHeight);
	void setTargetSurface(int handle, ImageHolder<Image> image);
	void flipBuffer();
	void startFrame();
	void loadImageLine(int handle, ImageHolder<Image> image, int line, int len2, byte[] cmddata);
	void xfmImage(int srcHandle, ImageHolder<Image> srcImg, int destHandle, ImageHolder<Image> destImg, int destWidth, int destHeight, int maskCornerArc);
	boolean hasGraphicsCanvas();
	Dimension getMaxScreenSize();
	Dimension getScreenSize();
	void setFullScreen(boolean b);
	void setSize(int w, int h);
	void invokeLater(Runnable runnable);
    Scale getScale();

	// video playback
	boolean createVideo(int width, int height, int format);

	boolean updateVideo(int frametype, ByteBuffer buf);

	MiniPlayerPlugin newPlayerPlugin(MiniClientConnection connection);

	void setVideoBounds(Object o, Object o1);
}
