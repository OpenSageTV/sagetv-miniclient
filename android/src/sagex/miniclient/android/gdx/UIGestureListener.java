package sagex.miniclient.android.gdx;

import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.MiniClient;
import sagex.miniclient.android.events.ShowKeyboardEvent;
import sagex.miniclient.android.events.ShowNavigationEvent;
import sagex.miniclient.uibridge.Keys;
import sagex.miniclient.uibridge.MouseEvent;

/**
 * Created by seans on 20/09/15.
 */
public class UIGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final Logger log = LoggerFactory.getLogger(UIGestureListener.class);

    private static final int EDGE_SIZE = 20; // dp

    /**
     * Edge flag indicating that the left edge should be affected.
     */
    public static final int EDGE_LEFT = 1 << 0;
    /**
     * Edge flag indicating that the right edge should be affected.
     */
    public static final int EDGE_RIGHT = 1 << 1;
    /**
     * Edge flag indicating that the top edge should be affected.
     */
    public static final int EDGE_TOP = 1 << 2;
    /**
     * Edge flag indicating that the bottom edge should be affected.
     */
    public static final int EDGE_BOTTOM = 1 << 3;
    /**
     * Edge flag set indicating all edges should be affected.
     */
    public static final int EDGE_ALL = EDGE_LEFT | EDGE_TOP | EDGE_RIGHT | EDGE_BOTTOM;

    private final MiniClient client;
    private final MiniClientGDXActivity context;
    private final int edgeSize;
    boolean logTouch = true;
    private int pointers;
    private int edgesTouched;

    public UIGestureListener(MiniClientGDXActivity act, MiniClient client) {
        super();
        this.client = client;
        this.context = act;
        final float density = context.getResources().getDisplayMetrics().density;
        edgeSize = (int) (EDGE_SIZE * density + 0.5f);
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
            //log.debug("E1:{}, E2: {}", e1, e2);
        }

        if (velocityX > flingThreshold) {
            if (multi() > 2) {
                if (logTouch) log.debug("Fling Right: Skip Forward");
                client.getCurrentConnection().postKeyEvent(Keys.VK_F8, Keys.CTRL_MASK, (char) 0);
            } else {
                if (isEdgeTouched(EDGE_LEFT)) {
                    if (logTouch) log.debug("Left Edge Trigger for KeyBoard");
                    client.eventbus().post(ShowNavigationEvent.INSTANCE);
                } else {
                    if (logTouch) log.debug("Fling Right");
                    client.getCurrentConnection().postKeyEvent(Keys.VK_RIGHT, 0, (char) 0);
                }
            }
        } else if (velocityX < -flingThreshold) {
            if (multi() > 2) {
                if (logTouch) log.debug("Fling Left: Skip Back");
                client.getCurrentConnection().postKeyEvent(Keys.VK_F7, Keys.CTRL_MASK, (char) 0);
            } else {
                if (logTouch) log.debug("Fling Left");
                client.getCurrentConnection().postKeyEvent(Keys.VK_LEFT, 0, (char) 0);
            }
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
                if (isEdgeTouched(EDGE_BOTTOM)) {
                    client.eventbus().post(ShowKeyboardEvent.INSTANCE);
                } else {
                    if (logTouch) log.debug("Fling Up");
                    client.getCurrentConnection().postKeyEvent(Keys.VK_UP, 0, (char) 0);
                }
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
        if (edgesTouched == 0 && e.getAction() == MotionEvent.ACTION_DOWN) {
            edgesTouched = getEdgesTouched((int) e.getX(), (int) e.getY());
            if (edgesTouched != 0) {
                log.debug("Edges Touched: {}", edgesTouched);
            }
        }
        mDetector.onTouchEvent(e);

        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            client.getCurrentConnection().postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 16, X(e), Y(e), 1, 1, 0));
        }

        if (edgesTouched != 0 && (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_POINTER_UP)) {
            log.debug("Clearing Touched Edges");
            edgesTouched = 0;
        }
    }

    void setTouches(MotionEvent event) {
        this.pointers = Math.max(this.pointers, event.getPointerCount());
    }

    private int getEdgesTouched(int x, int y) {
        int result = 0;
        if (x < context.getRootView().getLeft() + edgeSize) result |= EDGE_LEFT;
        if (y < context.getRootView().getTop() + edgeSize) result |= EDGE_TOP;
        if (x > context.getRootView().getRight() - edgeSize) result |= EDGE_RIGHT;
        if (y > context.getRootView().getBottom() - edgeSize) result |= EDGE_BOTTOM;
        return result;
    }

    public boolean isEdgeTouched(int edges) {
        return (edgesTouched & edges) != 0;
    }

    private int multi() {
        return pointers;
    }
}
