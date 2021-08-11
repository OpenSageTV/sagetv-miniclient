package sagex.miniclient.android.ui;


import android.app.Activity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.MiniClient;
import sagex.miniclient.SageCommand;
import sagex.miniclient.android.preferences.TouchPreferences;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.EventRouter;
import sagex.miniclient.uibridge.MouseEvent;

/**
 * Created by seans on 20/09/15.
 */
public class UIGestureListener extends GestureDetector.SimpleOnGestureListener
{
    private static final Logger log = LoggerFactory.getLogger(UIGestureListener.class);
    private static int EDGE_SIZE = 50; // dp
    private static int HOTSPOT_SIZE = 50; // dp

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

    public static final int FLING_NONE = 0;
    public static final int FLING_LEFT = 1;
    public static final int FLING_RIGHT = 2;
    public static final int FLING_UP = 3;
    public static final int FLING_DOWN = 4;
    public static final int FLING_THRESHHOLD = 50;

    private final MiniClient client;
    private final Activity activity;
    private final int edgeSize;
    private int pointers;
    private int edgesTouched;
    private TouchPreferences prefs;

    //private  SharedPreferences preferences;
    //SharedPreferences.Editor editor;

    boolean logTouch = true;
    boolean touchFocusEnabled =true;
    boolean edgesEnabled=true;

    public UIGestureListener(Activity act, MiniClient client)
    {
        super();
        this.client = client;
        this.activity = act;
        final float density = activity.getResources().getDisplayMetrics().density;

        prefs = new TouchPreferences(client.properties());

        EDGE_SIZE = prefs.getEdgeSizePixels();
        HOTSPOT_SIZE = prefs.getHotspotSizePixels();

        edgeSize = (int) (EDGE_SIZE * density + 0.5f);
    }



    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        return super.onSingleTapUp(e);
    }

    @Override
    public void onLongPress(MotionEvent e)
    {
        if (logTouch)
            log.debug("On Long Press");

        if (multi() > 2) {
            EventRouter.postCommand(client, prefs.getTripleLongPress());
        } else if (multi() > 1) {
            EventRouter.postCommand(client, prefs.getDoubleLongPress());
        } else {
            EventRouter.postCommand(client, prefs.getLongPress());
        }
    }


    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        return super.onScroll(e1, e2, distanceX, distanceY);
    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        int fling = getFling(e1, e2, velocityX, velocityY);

        //TODO: Think about how I can make the show nav configurable.  And possibly show keyboard

        if (logTouch)
        {
            log.debug("FLING: {}", fling);
        }

        if (fling == FLING_NONE)
        {
            return true;
        }

        //Three finger swipe
        if (multi()>2)
        {
            if (logTouch)
            {
                log.debug("FLING 3 Finger");
            }

            // track 3 finger flings
            if (FLING_RIGHT==fling)
            {
                if (logTouch) log.debug("3 Finger Fling Right - Invoke RIGHT action");
                EventRouter.postCommand(client, prefs.getTripleSwipeRight());

                return true;
            }
            if (FLING_LEFT==fling)
            {
                if (logTouch) log.debug("3 Finger Fling LEFT - Invoke LEFT action");
                EventRouter.postCommand(client, prefs.getTripleSwipeLeft());

                return true;
            }
            if (FLING_UP==fling)
            {
                if (logTouch) log.debug("3 Finger Fling Right - Invoke UP action");
                EventRouter.postCommand(client, prefs.getTrippleSwipeUp());

                return true;
            }
            if (FLING_DOWN==fling)
            {
                if (logTouch) log.debug("3 Finger Fling LEFT - Invoke DOWN action");
                EventRouter.postCommand(client, prefs.getTripleSwipeDown());

                return true;
            }
        }

        //Two finger swipe
        if (multi()>1)
        {
            if (logTouch) log.debug("FLING 2 Finger");
            // track 2 finger flings

            if (FLING_LEFT==fling)
            {
                if (logTouch) log.debug("2 Finger Fling left");
                EventRouter.postCommand(client, prefs.getDoubleSwipeLeft());

                return true;
            }
            if (FLING_RIGHT==fling)
            {

                EventRouter.postCommand(client, prefs.getDoubleSwipeRight());
                return true;
            }
            if (FLING_UP==fling)
            {
                if (logTouch) log.debug("2 Finger Fling up");
                EventRouter.postCommand(client, prefs.getDoubleSwipeUp());

                return true;
            }
            if (FLING_DOWN==fling)
            {
                if(logTouch) log.debug("2 Finger Fling down");
                EventRouter.postCommand(client, prefs.getDoubleSwipeDown());

                return true;
            }
        }

        //Process edge swipes
        if (edgesEnabled)
        {
            // do the edge detections
            if (FLING_RIGHT == fling && isEdgeTouched(EDGE_LEFT))
            {
                if (logTouch) log.debug("Left Edge Trigger");
                EventRouter.postCommand(client, prefs.getEdgeSwipeLeft());

                return true;
            }

            if (FLING_LEFT == fling && isEdgeTouched(EDGE_RIGHT)) {
                if (logTouch) log.debug("Right Edge Trigger");
                EventRouter.postCommand(client, prefs.getEdgeSwipeRight());

                return true;
            }

            if (FLING_UP == fling && isEdgeTouched(EDGE_BOTTOM))
            {
                if (logTouch) log.debug("Bottom Edge Trigger");
                EventRouter.postCommand(client, prefs.getEdgeSwipeBottom());

                return true;
            }

            if (FLING_DOWN == fling && isEdgeTouched(EDGE_TOP)) {
                if (logTouch) log.debug("Top Edge Trigger");
                EventRouter.postCommand(client, prefs.getEdgeSwipeTop());

                return true;
            }
        }

        //Single finger swipe
        if (FLING_UP==fling)
        {
            EventRouter.postCommand(client, prefs.getSingleSwipeUp());
            return true;
        }

        if (FLING_DOWN==fling)
        {
            EventRouter.postCommand(client, prefs.getSingleSwipeDown());
            return true;
        }

        if (FLING_RIGHT==fling)
        {
            EventRouter.postCommand(client, prefs.getSingleSwipeRight());
            return true;
        }

        if (FLING_LEFT==fling)
        {
            EventRouter.postCommand(client, prefs.getSingleSwipeLeft());
            return true;
        }

        return true;
    }

    private int getFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        if (e1==null||e2==null)
        {
            return FLING_NONE;
        }

        float dx = Math.abs(e1.getX()-e2.getX());
        float dy = Math.abs(e1.getY()-e2.getY());

        if (dx>dy)
        {
            // fling right/left
            if (velocityX > FLING_THRESHHOLD)
            {
                return FLING_RIGHT;
            }
            else if (velocityX < -FLING_THRESHHOLD)
            {
                return FLING_LEFT;
            }
        }
        else
        {
            // fling up/down
            if (velocityY > FLING_THRESHHOLD)
            {
                return FLING_DOWN;
            }
            else if (velocityY < -FLING_THRESHHOLD)
            {
                return FLING_UP;
            }
        }

        return FLING_NONE;
    }

    @Override
    public void onShowPress(MotionEvent e)
    {
        if (logTouch) log.debug("onShowPress");
        super.onShowPress(e);
    }

    @Override
    public boolean onDown(MotionEvent e)
    {
        pointers = 1;
        if (logTouch) log.debug("Mouse Down: " + e);

        if (touchFocusEnabled)
        {
            client.getCurrentConnection().postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 16, X(e), Y(e), 1, 1, 0));
        }

        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e)
    {
        if (logTouch) log.debug("onDoubleTap");

        if (multi() > 2) {
            EventRouter.postCommand(client, prefs.getOnDoubleTap3());
        } else if (multi() > 1) {
            EventRouter.postCommand(client, prefs.getOnDoubleTap2());

        } else {
            EventRouter.postCommand(client, prefs.getOnDoubleTap());
        }

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e)
    {
        if (logTouch) log.debug("onDoubleTapEvent");
        return super.onDoubleTapEvent(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e)
    {
        if (logTouch)
        {
            log.debug("Mouse Tap: (screen x,y)[" + e.getX() + "," + e.getY() + "]; (canvas x,y)[" + X(e) + "," + Y(e) + "]");
        }
        if (!touchFocusEnabled)
        {
            client.getCurrentConnection().postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 16, X(e), Y(e), 1, 1, 0));
        }

        client.getCurrentConnection().postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 16, X(e), Y(e), 1, 1, 0));

        if (isBottomLeftHotSpot(e) && prefs.getHotspotBottomLeft() != SageCommand.NONE) {
            EventRouter.postCommand(client, prefs.getHotspotBottomLeft());
        } else if (isBottomRightHotSpot(e) && prefs.getHotspotBottomRight() != SageCommand.NONE) {
            EventRouter.postCommand(client, prefs.getHotspotBottomRight());
        } else if (isTopRightHotSpot(e) && prefs.getHotspotTopRight() != SageCommand.NONE) {
            EventRouter.postCommand(client, prefs.getHotspotTopRight());
        } else if (isTopLeftHotSpot(e) && prefs.getHotspotTopLeft() != SageCommand.NONE) {
            EventRouter.postCommand(client, prefs.getHotspotTopLeft());
        } else {
            client.getCurrentConnection().postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 16, X(e), Y(e), 1, 1, 0));
        }

        return true;
    }

    private boolean isBottomLeftHotSpot(MotionEvent e) {
        return e.getY() > client.getUIRenderer().getScreenSize().getHeight() - HOTSPOT_SIZE && e.getX() < HOTSPOT_SIZE;
    }

    private boolean isTopLeftHotSpot(MotionEvent e) {
        return e.getY() < HOTSPOT_SIZE && e.getX() < HOTSPOT_SIZE;
    }

    private boolean isBottomRightHotSpot(MotionEvent e) {
        Dimension d = client.getUIRenderer().getScreenSize();
        return e.getY() > d.getHeight() - HOTSPOT_SIZE && e.getX() > d.getWidth() - HOTSPOT_SIZE;
    }

    private boolean isTopRightHotSpot(MotionEvent e) {
        Dimension d = client.getUIRenderer().getScreenSize();
        return e.getY() < HOTSPOT_SIZE && e.getX() > d.getWidth() - HOTSPOT_SIZE;
    }

    public int X(MotionEvent e)
    {
        return (int) client.getUIRenderer().getScale().xScreenToCanvas(e.getX());
    }

    public int Y(MotionEvent e)
    {
        return (int) client.getUIRenderer().getScale().yScreenToCanvas(e.getY());
    }

    public void processRawEvent(GestureDetectorCompat mDetector, MotionEvent e) {

        // record the touches
        setTouches(e);

        if (edgesTouched == 0 && e.getAction() == MotionEvent.ACTION_DOWN)
        {
            edgesTouched = getEdgesTouched((int) e.getX(), (int) e.getY());

            if (edgesTouched != 0)
            {
                log.debug("Edges Touched: {}", edgesTouched);
            }
        }
        mDetector.onTouchEvent(e);

        if (touchFocusEnabled)
        {
            if (e.getAction() == MotionEvent.ACTION_MOVE)
            {
                if (logTouch) log.debug("onMouseMove: {},{}", X(e), Y(e));

                client.getCurrentConnection().postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, X(e), Y(e), 0, 0, 0));
            }
        }

        if (edgesTouched != 0 && (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_POINTER_UP))
        {
            log.debug("Clearing Touched Edges");
            edgesTouched = 0;
        }
    }

    void setTouches(MotionEvent event)
    {
        this.pointers = Math.max(this.pointers, event.getPointerCount());
    }

    private int getEdgesTouched(int x, int y)
    {
        int result = 0;
        if (x < getRootView().getLeft() + edgeSize) result |= EDGE_LEFT;
        if (y < getRootView().getTop() + edgeSize) result |= EDGE_TOP;
        if (x > getRootView().getRight() - edgeSize) result |= EDGE_RIGHT;
        if (y > getRootView().getBottom() - edgeSize) result |= EDGE_BOTTOM;
        return result;
    }

    View rootView = null;

    View getRootView() {
        if (rootView!=null) return rootView;
        activity.getWindow().getDecorView().getRootView();
        if (rootView==null) {
            rootView = activity.findViewById(android.R.id.content);
        }
        return rootView;
    }

    public boolean isEdgeTouched(int edges)
    {
        return (edgesTouched & edges) != 0;
    }

    private int multi()
    {
        return pointers;
    }

}