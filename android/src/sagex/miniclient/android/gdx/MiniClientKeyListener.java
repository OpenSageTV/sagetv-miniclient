package sagex.miniclient.android.gdx;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.uibridge.Keys;

/**
 * Created by seans on 26/09/15.
 */
public class MiniClientKeyListener implements View.OnKeyListener {
    private static final Map<Integer, Integer> LONG_KEYMAP = new HashMap<>();
    private static final Map<Integer, Integer> KEYMAP = new HashMap<>();
    private static final String TAG = "GDXMINICLIENTKEY";

    static {
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, Keys.VK_UP);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, Keys.VK_DOWN);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, Keys.VK_LEFT);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, Keys.VK_RIGHT);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, Keys.VK_ENTER);

        //KEYMAP.put(KeyEvent.KEYCODE_BUTTON_SELECT, Keys.VK_ENTER);
        //KEYMAP.put(KeyEvent.KEYCODE_BUTTON_START, Keys.VK_ENTER);

        //KEYMAP.put(KeyEvent.KEYCODE_BUTTON_A, Keys.VK_ENTER); (DPAD Center will catch this)
        //KEYMAP.put(KeyEvent.KEYCODE_BACK, Keys.VK_ESCAPE);

        // don't like that behaviour
        //KEYMAP.put(KeyEvent.KEYCODE_BACK, Keys.VK_ESCAPE);

        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_B, Keys.VK_ESCAPE);
    }

    static {
        LONG_KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, Keys.VK_ESCAPE);
    }

    private final MiniClientConnection connection;
    int skipKey = -1;
    boolean skipUp = false;
    public MiniClientKeyListener(MiniClientConnection connection) {
        this.connection=connection;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.d(TAG, "DOWN KEYCODE: " + keyCode + "; " + event);
            if (LONG_KEYMAP.containsKey(keyCode)) {
                if (event.isLongPress()) {
                    Log.d(TAG, "LONG PRESS KEYCODE: " + keyCode + "; " + event);
                    if (LONG_KEYMAP.containsKey(keyCode)) {
                        skipKey = keyCode;
                        keyCode = LONG_KEYMAP.get(keyCode);
                        connection.postKeyEvent(keyCode, 0, (char) 0);
                        skipUp = true;
                        return true;
                    }
                }
                if (event.isTracking()) return true;
                event.startTracking();
                return true;
            }
            return false;
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            if (skipUp && skipKey == keyCode) {
                // After a Long Press
                skipUp = false;
                skipKey = -1;
                return true;
            }
            Log.d(TAG, "POST KEYCODE: " + keyCode + "; " + event + "; longpress: " + event.isLongPress());

            if (KEYMAP.containsKey(keyCode)) {
                keyCode = KEYMAP.get(keyCode);
                connection.postKeyEvent(keyCode, 0, (char) 0);
                return true;
            }
        }

        return false;
    }
}
