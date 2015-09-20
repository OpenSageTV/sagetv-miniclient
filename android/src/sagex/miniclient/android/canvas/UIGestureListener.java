package sagex.miniclient.android.canvas;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import sagex.miniclient.MiniClientConnectionGateway;
import sagex.miniclient.uibridge.Keys;
import sagex.miniclient.uibridge.MouseEvent;

/**
 * Created by seans on 20/09/15.
 */
public class UIGestureListener extends GestureDetector.SimpleOnGestureListener {
    boolean logTouch = true;
    private static final String TAG = "SAGETVINPUT";
    private final MiniClientConnectionGateway myConn;

    public UIGestureListener(MiniClientConnectionGateway myConn) {
        super();
        this.myConn=myConn;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return super.onSingleTapUp(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (logTouch) Log.d(TAG, "onLongPress: Sending ENTER");
        myConn.postKeyEvent(Keys.VK_ENTER, 0, (char)0);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float flingThreshold=2000;
        if (logTouch) {
            Log.d(TAG, "FLING: " + velocityX + "," + velocityY + "; e1Pointers: " + e1.getPointerCount() + "; e2Pointers: " + e2.getPointerCount());
        }
        if (velocityX > flingThreshold) {
            if (logTouch) Log.d(TAG, "Fling Right");
            myConn.postKeyEvent(Keys.VK_RIGHT, 0, (char) 0);
        } else if (velocityX < -flingThreshold) {
            if (logTouch) Log.d(TAG, "Fling Left");
            myConn.postKeyEvent(Keys.VK_LEFT, 0, (char) 0);
        } else if (velocityY > flingThreshold) {
            if (multi(e1, e2)) {
                if (logTouch) Log.d(TAG, "Fling Page Down");
                myConn.postKeyEvent(Keys.VK_PAGE_DOWN, 0, (char) 0);
            } else {
                if (logTouch) Log.d(TAG, "Fling Down");
                myConn.postKeyEvent(Keys.VK_DOWN, 0, (char) 0);
            }
        } else if (velocityY < -flingThreshold) {
            if (multi(e1,e2)) {
                if (logTouch) Log.d(TAG, "Fling Page Up");
                myConn.postKeyEvent(Keys.VK_PAGE_UP, 0, (char) 0);
            } else {
                if (logTouch) Log.d(TAG, "Fling Up");
                myConn.postKeyEvent(Keys.VK_UP, 0, (char) 0);
            }
        }

        return true;
    }

    private boolean multi(MotionEvent e1, MotionEvent e2) {
        return e1.getPointerCount()>1 || e2.getPointerCount()>1;
    }

    private boolean multi(MotionEvent e1) {
        return e1.getPointerCount()>1;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        super.onShowPress(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (logTouch) Log.d(TAG, "Mouse Down" + e);
        myConn.postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 16, X(e), Y(e), 1, 1, 0));
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (logTouch) Log.d(TAG, "onDoubleTap: Sending ENTER");
        myConn.postKeyEvent(Keys.VK_ENTER, 0, (char)0);
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return super.onDoubleTapEvent(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (logTouch) Log.d(TAG, "Mouse Tap: (screen x,y)[" + e.getX() +"," +e.getY() + "]; (canvas x,y)["+X(e) + "," +Y(e) +"]");
        if (multi(e)) {
            myConn.postKeyEvent(Keys.VK_ENTER, 0, (char)0);
        } else {
            myConn.postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 16, X(e), Y(e), 1, 1, 0));
        }
        return true;
    }

    public int X(MotionEvent e) {
        return (int)myConn.getUiManager().getScale().xScreenToCanvas(e.getX());
    }

    public int Y(MotionEvent e) {
        return (int)myConn.getUiManager().getScale().yScreenToCanvas(e.getY());
    }
}
