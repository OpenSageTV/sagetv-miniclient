package sagex.miniclient.android.gdx;

import android.app.Activity;
import android.support.v4.view.GestureDetectorCompat;
import android.view.MotionEvent;
import android.view.View;

import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.android.gl.UIGestureListener;

/**
 * Created by seans on 26/09/15.
 */
public class MiniclientTouchListener implements View.OnTouchListener {
    private MiniClientConnection clientConnection;

    private UIGestureListener mGestureListener;
    GestureDetectorCompat mDetector = null;

    public MiniclientTouchListener(Activity act, MiniClientConnection client) {
        this.clientConnection=client;
        mGestureListener = new UIGestureListener(client);
        mDetector = new GestureDetectorCompat(act, mGestureListener);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (clientConnection==null) {
            return false;
        }
        if (mGestureListener!=null) {
            mGestureListener.processRawEvent(mDetector, event);
        }
        return true;
    }
}
