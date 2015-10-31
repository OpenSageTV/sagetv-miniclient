package sagex.miniclient.android.gdx;

import android.view.KeyEvent;
import android.view.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import sagex.miniclient.MiniClient;
import sagex.miniclient.uibridge.Keys;
import sagex.miniclient.uibridge.SageTVKey;

/**
 * Created by seans on 26/09/15.
 */
public class MiniClientKeyListener implements View.OnKeyListener {
    private static final Logger log = LoggerFactory.getLogger(MiniClientKeyListener.class);

    private static final Map<Integer, SageTVKey> LONG_KEYMAP = new HashMap<>();
    private static final Map<Integer, SageTVKey> KEYMAP = new HashMap<>();

    static {
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, new SageTVKey(Keys.VK_UP));
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, new SageTVKey(Keys.VK_DOWN));
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, new SageTVKey(Keys.VK_LEFT));
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, new SageTVKey(Keys.VK_RIGHT));
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, new SageTVKey(Keys.VK_ENTER));

        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PAUSE, new SageTVKey(Keys.VK_S, Keys.CTRL_MASK | Keys.SHIFT_MASK));
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PLAY, new SageTVKey(Keys.VK_S, Keys.CTRL_MASK | Keys.SHIFT_MASK));
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, new SageTVKey(Keys.VK_S, Keys.CTRL_MASK | Keys.SHIFT_MASK));
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_Y, new SageTVKey(Keys.VK_S, Keys.CTRL_MASK | Keys.SHIFT_MASK));

        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, new SageTVKey(Keys.VK_F, Keys.CTRL_MASK));
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_REWIND, new SageTVKey(Keys.VK_A, Keys.CTRL_MASK));

        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_R1, new SageTVKey(Keys.VK_F, Keys.CTRL_MASK));
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_L1, new SageTVKey(Keys.VK_A, Keys.CTRL_MASK));


        //KEYMAP.put(KeyEvent.KEYCODE_BUTTON_SELECT, Keys.VK_ENTER);
        //KEYMAP.put(KeyEvent.KEYCODE_BUTTON_START, Keys.VK_ENTER);

        //KEYMAP.put(KeyEvent.KEYCODE_BUTTON_A, Keys.VK_ENTER); (DPAD Center will catch this)
        //KEYMAP.put(KeyEvent.KEYCODE_BACK, Keys.VK_ESCAPE);

        // don't like that behaviour
        //KEYMAP.put(KeyEvent.KEYCODE_BACK, Keys.VK_ESCAPE);

        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_B, new SageTVKey(Keys.VK_ESCAPE));
    }

    static {
        LONG_KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, new SageTVKey(Keys.VK_ESCAPE));
    }

    private final MiniClient client;

    int skipKey = -1;
    boolean skipUp = false;

    public MiniClientKeyListener(MiniClient client) {
        this.client = client;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            log.debug("DOWN KEYCODE: " + keyCode + "; " + event);
            if (LONG_KEYMAP.containsKey(keyCode)) {
                if (event.isLongPress()) {
                    log.debug("LONG PRESS KEYCODE: {}; {}", keyCode, event);
                    if (LONG_KEYMAP.containsKey(keyCode)) {
                        skipKey = keyCode;
                        SageTVKey key = LONG_KEYMAP.get(keyCode);
                        if (key == null) {
                            log.debug("Invalid Key Code: {}", keyCode);
                            return false;
                        }

                        client.getCurrentConnection().postKeyEvent(key.keyCode, key.modifiers, key.keyChar);
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
            log.debug("POST KEYCODE: {}; {}; longpress?: {}", keyCode, event, event.isLongPress());

            if (KEYMAP.containsKey(keyCode)) {
                SageTVKey key = KEYMAP.get(keyCode);
                if (key == null) {
                    log.debug("Invalid Key Code: {}", keyCode);
                    return false;
                }
                client.getCurrentConnection().postKeyEvent(key.keyCode, key.modifiers, key.keyChar);
                return true;
            }
        }

        return false;
    }
}
