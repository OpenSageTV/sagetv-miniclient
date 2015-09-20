package sagex.miniclient.android.canvas;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import sagex.miniclient.GFXCMD2;
import sagex.miniclient.ImageHolder;
import sagex.miniclient.UIManager;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Scale;

/**
 * Created by seans on 20/09/15.
 */
public class CanvasUIManager implements UIManager<Bitmap>, SurfaceHolder.Callback {
    private static final String TAG = "CanvasUI";
    private static final int WIDTH = 720;
    private static final int HEIGHT = 480;

    private SurfaceHolder holder;
    private final MiniClientActivity ctx;
    private Canvas canvas = null;

    private boolean firstFrame = true;

    Scale scale = new Scale(1, 1);

    boolean logFrameTime=true;
    long frameTime = 0;
    long frame=0;

    // this only ever accessed by a single thread
    Rect srcRect = new Rect();
    Rect dstRect = new Rect();
    Paint shapePaint = new Paint();
    Paint fontPaint = new Paint();
    Paint texturePaint = new Paint();

    public CanvasUIManager(MiniClientActivity ctx) {
        this.ctx = ctx;
    }

    @Override
    public void GFXCMD_INIT() {
        fontPaint.setAntiAlias(true);
        fontPaint.setDither(false);
        fontPaint.setFilterBitmap(true);

        texturePaint.setAntiAlias(false);
        texturePaint.setDither(false);
        texturePaint.setFilterBitmap(true);

        shapePaint.setAntiAlias(true);
        shapePaint.setDither(true);

        // tell android to show our view
        if (firstFrame) {
            ctx.setConnectingIsVisible(firstFrame);
        }
    }

    @Override
    public void GFXCMD_DEINIT() {
    }

    @Override
    public void close() {
        GFXCMD_DEINIT();
        ctx.finish();
    }

    @Override
    public void refresh() {
        // tell android to invalidate our window (not used, I don't think)
    }

    @Override
    public void hideCursor() {

    }

    @Override
    public void showBusyCursor() {

    }

    private void updateColor(Paint texturePaint, int blend) {
        texturePaint.setARGB(((blend & 0xff000000) >>> 24), ((blend & 0x00ff0000) >>> 16), ((blend & 0x0000ff00) >>> 8), ((blend & 0x000000ff)));
    }

    @Override
    public void drawRect(final int x, final int y, final int width, final int height, final int thickness, final int argbTL, int argbTR, int argbBR, int argbBL) {
        // TODO: Handle Gradient
        shapePaint.reset();
        shapePaint.setStrokeWidth(thickness);
        shapePaint.setStyle(Paint.Style.STROKE);
        updateColor(shapePaint, argbTL);
        canvas.drawRect(x, y, x + width, y + height, shapePaint);
        shapePaint.setStrokeWidth(1);
    }

    @Override
    public void fillRect(final int x, final int y, final int width, final int height, final int argbTL, int argbTR, int argbBR, int argbBL) {
        // TODO: Handle Gradient
        shapePaint.reset();
        shapePaint.setStyle(Paint.Style.FILL);
        updateColor(shapePaint, argbTL);
        canvas.drawRect(x, y, x + width, y + height, shapePaint);
        shapePaint.setStrokeWidth(1);
    }

    @Override
    public void clearRect(int x, int y, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL) {
        fillRect(x, y, width, height, 0, 0, 0, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawOval(final int x, final int y, final int width, final int height, final int thickness, final int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        shapePaint.reset();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shapePaint.setStyle(Paint.Style.STROKE);
            shapePaint.setStrokeWidth(thickness);
            updateColor(shapePaint, argbTL);
            canvas.drawOval(x, y, x + width, y + height, shapePaint);
        } else {
            Log.d(TAG, "drawOval() requires Lollipop");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void fillOval(final int x, final int y, final int width, final int height, final int argbTL, int argbTR, int argbBR, int argbBL, int clipX, int clipY, int clipW, int clipH) {
        shapePaint.reset();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shapePaint.setStyle(Paint.Style.FILL);
            updateColor(shapePaint, argbTL);
            canvas.drawOval(x, y, x + width, y + height, shapePaint);
        } else {
            Log.d(TAG, "fillOval() requires Lollipop");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawRoundRect(final int x, final int y, final int width, final int height, final int thickness, final int arcRadius, final int argbTL, final int argbTR, final int argbBR, final int argbBL, int clipX, int clipY, int clipW, int clipH) {
        shapePaint.reset();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shapePaint.setStyle(Paint.Style.STROKE);
            shapePaint.setStrokeWidth(thickness);
            updateColor(shapePaint, argbTL);
            canvas.drawRoundRect(x, y, x + width, y + height, arcRadius, arcRadius, shapePaint);
        } else {
            Log.d(TAG, "drawRoundRect() requires Lollipop");
            drawRect(x, y, width, height, thickness, argbTL, argbTR, argbBR, argbBL);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void fillRoundRect(final int x, final int y, final int width, final int height, final int arcRadius, final int argbTL, final int argbTR, final int argbBR, final int argbBL, int clipX, int clipY, int clipW, int clipH) {
        shapePaint.reset();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shapePaint.setStyle(Paint.Style.FILL);
            updateColor(shapePaint, argbTL);
            canvas.drawRoundRect(x, y, x + width, y + height, arcRadius, arcRadius, shapePaint);
        } else {
            Log.d(TAG, "fillRoundRect() requires Lollipop");
            fillRect(x, y, width, height, argbTL, argbTR, argbBR, argbBL);
        }
    }

    @Override
    public void drawLine(final int x1, final int y1, final int x2, final int y2, final int argb1, int argb2) {
        shapePaint.reset();
        shapePaint.setStyle(Paint.Style.STROKE);
        shapePaint.setStrokeWidth(1);
        updateColor(shapePaint, argb1);
        canvas.drawLine(x1, y1, x2, y2, shapePaint);
    }

    @Override
    public ImageHolder<Bitmap> readImage(File cachedFile) throws Exception {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(cachedFile.getAbsolutePath(), options);
        return new ImageHolder<>(bitmap);
    }

    @Override
    public ImageHolder<Bitmap> readImage(InputStream bais) throws Exception {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeStream(bais, null, options);
        return new ImageHolder<>(bitmap);
    }

    @Override
    public void drawTexture(final int x, final int y, final int width, final int height, int handle, final ImageHolder<?> img, final int srcx, final int srcy, final int srcwidth, final int srcheight, final int blend) {
        srcRect.set(srcx, srcy, srcx + srcwidth, srcy + srcheight);
        dstRect.set(x, y, x + Math.abs(width), y + Math.abs(height));

        if (width < 0) {
            fontPaint.setColorFilter(getColorFilter(blend));
            canvas.drawBitmap((Bitmap) img.get(), srcRect, dstRect, fontPaint);
        } else {
            canvas.drawBitmap((Bitmap) img.get(), srcRect, dstRect, texturePaint);
        }
    }

    HashMap<Integer, ColorFilter> colorFilters = new HashMap<>();
    private ColorFilter getColorFilter(int blend) {
        ColorFilter cf = colorFilters.get(blend);
        if (cf==null) {
            cf = new PorterDuffColorFilter(blend, PorterDuff.Mode.SRC_IN);
            colorFilters.put(blend, cf);
        }
        return cf;
    }

    @Override
    public void flipBuffer() {
        if (firstFrame) {
            firstFrame = false;
            ctx.setConnectingIsVisible(false);
        }
        holder.unlockCanvasAndPost(canvas);
        canvas = null;
        if (logFrameTime) {
            Log.d(TAG, "FRAME: " + (frame++) + "; Time: " + (System.currentTimeMillis()-frameTime) + "ms");
        }
    }

    @Override
    public void startFrame() {
        frameTime=System.currentTimeMillis();
        canvas = holder.lockCanvas();
        scale.setScale(((float) canvas.getWidth()) / ((float) WIDTH), ((float) canvas.getHeight()) / ((float) HEIGHT));
        canvas.scale(scale.getXScale(), scale.getYScale());
    }

    @Override
    public boolean hasGraphicsCanvas() {
        return canvas != null;
    }

    @Override
    public Dimension getMaxScreenSize() {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return new Dimension(size.x, size.y);
    }

    @Override
    public Dimension getScreenSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    @Override
    public void setFullScreen(boolean b) {

    }

    @Override
    public void setSize(int w, int h) {

    }

    @Override
    public void invokeLater(Runnable runnable) {
        throw new UnsupportedOperationException("invokeLater not implemented");
    }

    @Override
    public Scale getScale() {
        return scale;
    }

    // ----- LOCAL NETWORK OPTIMIZATION
    @Override
    public ImageHolder<Bitmap> loadImage(int width, int height) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(width, height, conf);
        return new ImageHolder<>(bmp);
    }

    @Override
    public ImageHolder<Bitmap> newImage(int destWidth, int destHeight) {
        return loadImage(destWidth, destHeight);
    }

    @Override
    public void loadImageLine(int handle, final ImageHolder<?> image, final int line, final int len2, final byte[] cmddata) {
        Bitmap b = (Bitmap) image.get();
        int dataPos = 12;
        int pixel = 0;
        for (int i = 0; i < len2 / 4; i++, dataPos += 4) {
            pixel = GFXCMD2.readInt(dataPos, cmddata);
            //pixel = (pixel << 8) | ((pixel >> 24) & 0xFF);
            b.setPixel(i, line, pixel);
        }
    }
    // ----- END LOCAL NETWORK OPTIMIZATION

    // ------- Used for surfaces

    @Override
    public ImageHolder<Bitmap> createSurface(int handle, int width, int height) {
        throw new UnsupportedOperationException("createSurface not implemented");
    }

    @Override
    public void setTargetSurface(int handle, ImageHolder<?> image) {
        throw new UnsupportedOperationException("setTargetSurface not implemented");
    }

    @Override
    public void xfmImage(int srcHandle, ImageHolder<?> srcImg, int destHandle, ImageHolder<?> destImg, int destWidth, int destHeight, int maskCornerArc) {
        throw new UnsupportedOperationException("xfmImage not implemented");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;
        Log.d(TAG, "Surface Created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "Surface Changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface Destroyed");
    }
}
