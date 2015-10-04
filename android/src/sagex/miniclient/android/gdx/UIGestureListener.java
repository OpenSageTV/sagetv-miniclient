package sagex.miniclient.android.gdx;

import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.MiniClient;
import sagex.miniclient.uibridge.Keys;
import sagex.miniclient.uibridge.MouseEvent;

/**
 * Created by seans on 20/09/15.
 */
public class UIGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final Logger log = LoggerFactory.getLogger(UIGestureListener.class);

    private final MiniClient client;
    boolean logTouch = true;
    private int pointers;

    public UIGestureListener(MiniClient client) {
        super();
        this.client = client;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return super.onSingleTapUp(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (logTouch) log.debug("onLongPress: Sending ENTER");
        client.getCurrentConnection().postKeyEvent(Keys.VK_ENTER, 0, (char) 0);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float flingThreshold = 2000;
        if (logTouch) {
            log.debug("FLING: " + velocityX + "," + velocityY + "; e1Pointers: " + e1.getPointerCount() + "; e2Pointers: " + e2.getPointerCount());
        }
        if (velocityX > flingThreshold) {
            if (logTouch) log.debug("Fling Right");
            client.getCurrentConnection().postKeyEvent(Keys.VK_RIGHT, 0, (char) 0);
        } else if (velocityX < -flingThreshold) {
            if (logTouch) log.debug("Fling Left");
            client.getCurrentConnection().postKeyEvent(Keys.VK_LEFT, 0, (char) 0);
        } else if (velocityY > flingThreshold) {
            if (multi() > 2) {
                if (logTouch) log.debug("Fling Show Options");
                // send ctrl + o
                client.getCurrentConnection().postKeyEvent(Keys.VK_O, 2, 'o');
            } else if (multi() == 2) {
                if (logTouch) log.debug("Fling Page Down");
                client.getCurrentConnection().postKeyEvent(Keys.VK_PAGE_DOWN, 0, (char) 0);
            } else {
                if (logTouch) log.debug("Fling Down");
                client.getCurrentConnection().postKeyEvent(Keys.VK_DOWN, 0, (char) 0);
            }
        } else if (velocityY < -flingThreshold) {
            if (multi() > 1) {
                if (logTouch) log.debug("Fling Page Up");
                client.getCurrentConnection().postKeyEvent(Keys.VK_PAGE_UP, 0, (char) 0);
            } else {
                if (logTouch) log.debug("Fling Up");
                client.getCurrentConnection().postKeyEvent(Keys.VK_UP, 0, (char) 0);
            }
        }

        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        super.onShowPress(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        pointers = 1;
        if (logTouch) log.debug("Mouse Down: " + e);
        client.getCurrentConnection().postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 16, X(e), Y(e), 1, 1, 0));
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (logTouch) log.debug("onDoubleTap: Sending ENTER");
        client.getCurrentConnection().postKeyEvent(Keys.VK_ENTER, 0, (char) 0);
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return super.onDoubleTapEvent(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (logTouch)
            log.debug("Mouse Tap: (screen x,y)[" + e.getX() + "," + e.getY() + "]; (canvas x,y)[" + X(e) + "," + Y(e) + "]");
        client.getCurrentConnection().postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 16, X(e), Y(e), 1, 1, 0));
        return true;
    }

    public int X(MotionEvent e) {
        return (int) client.getUIRenderer().getScale().xScreenToCanvas(e.getX());
    }

    public int Y(MotionEvent e) {
        return (int) client.getUIRenderer().getScale().yScreenToCanvas(e.getY());
    }

    public void processRawEvent(GestureDetectorCompat mDetector, MotionEvent e) {
        // record the touches
        setTouches(e);

        mDetector.onTouchEvent(e);

        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            client.getCurrentConnection().postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 16, X(e), Y(e), 1, 1, 0));
        }
    }

    void setTouches(MotionEvent event) {
        this.pointers = Math.max(this.pointers, event.getPointerCount());
    }

    private int multi() {
        return pointers;
    }
}
