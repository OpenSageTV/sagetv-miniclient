package sagex.miniclient.android.gdx;

import android.app.Activity;
import android.support.v4.view.GestureDetectorCompat;
import android.view.MotionEvent;
import android.view.View;

import sagex.miniclient.MiniClient;

/**
 * Created by seans on 26/09/15.
 */
public class MiniclientTouchListener implements View.OnTouchListener {
    GestureDetectorCompat mDetector = null;
    private MiniClient client;
    private UIGestureListener mGestureListener;

    public MiniclientTouchListener(Activity act, MiniClient client) {
        this.client = client;
        mGestureListener = new UIGestureListener(act, client);
        mDetector = new GestureDetectorCompat(act, mGestureListener);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!client.isConnected()) {
            return false;
        }
        if (mGestureListener != null) {
            mGestureListener.processRawEvent(mDetector, event);
        }
        return true;
    }
}
