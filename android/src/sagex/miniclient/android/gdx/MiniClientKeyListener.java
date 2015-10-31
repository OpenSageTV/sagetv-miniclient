package sagex.miniclient.android.gdx;

import android.view.KeyEvent;
import android.view.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import sagex.miniclient.MiniClient;
import sagex.miniclient.uibridge.EventRouter;
import sagex.miniclient.uibridge.SageTVKey;

/**
 * Created by seans on 26/09/15.
 */
public class MiniClientKeyListener implements View.OnKeyListener {
    private static final Logger log = LoggerFactory.getLogger(MiniClientKeyListener.class);

    static {
        // navivation native keymap
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, EventRouter.UP);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, EventRouter.DOWN);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, EventRouter.LEFT);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, EventRouter.RIGHT);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, EventRouter.SELECT);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_BUTTON_B, EventRouter.ESCAPE);

        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PAUSE, EventRouter.MEDIA_PAUSE);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PLAY, EventRouter.MEDIA_PLAY);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, EventRouter.MEDIA_PLAY_PAUSE);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_BUTTON_Y, EventRouter.MEDIA_PLAY_PAUSE);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_BUTTON_X, EventRouter.MEDIA_STOP);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, EventRouter.MEDIA_FF);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_MEDIA_REWIND, EventRouter.MEDIA_REW);

        // UI Long Presses
        EventRouter.NATIVE_UI_LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, EventRouter.OPTIONS);
    }

    private final MiniClient client;

    int skipKey = -1;
    boolean skipUp = false;

    public MiniClientKeyListener(MiniClient client) {
        this.client = client;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Map<Object, SageTVKey> LONG_KEYMAP;
        Map<Object, SageTVKey> KEYMAP;

        LONG_KEYMAP = EventRouter.NATIVE_UI_LONGPRESS_KEYMAP;
        KEYMAP = EventRouter.NATIVE_UI_KEYMAP;

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            log.debug("KEYS: DOWN KEYCODE: " + keyCode + "; " + event + "; Video Playing: " + client.isVideoPlaying());
            if (LONG_KEYMAP.containsKey(keyCode)) {
                if (event.isLongPress()) {
                    log.debug("KEYS: LONG PRESS KEYCODE: {}; {}", keyCode, event);
                    if (LONG_KEYMAP.containsKey(keyCode)) {
                        skipKey = keyCode;
                        SageTVKey key = LONG_KEYMAP.get(keyCode);
                        if (key == null) {
                            log.debug("KEYS: Invalid Key Code: {}", keyCode);
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
                log.debug("KEYS: Skipping Key {}", keyCode);
                return true;
            }
            if (KEYMAP.containsKey(keyCode)) {
                SageTVKey key = KEYMAP.get(keyCode);
                log.debug("KEYS: POST KEYCODE: {}; {}; longpress?: {}", keyCode, event, event.isLongPress());
                client.getCurrentConnection().postKeyEvent(key.keyCode, key.modifiers, key.keyChar);
                return true;
            } else {
                log.debug("KEYS: Unmapped Key Code: {}", event);
            }
        }

        return false;
    }
}
