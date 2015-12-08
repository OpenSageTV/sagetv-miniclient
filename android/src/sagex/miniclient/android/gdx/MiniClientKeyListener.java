package sagex.miniclient.android.gdx;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import sagex.miniclient.MiniClient;
import sagex.miniclient.UserEvent;
import sagex.miniclient.android.AndroidKeyEventMapper;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.events.BackPressedEvent;
import sagex.miniclient.android.events.ShowNavigationEvent;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.EventRouter;
import sagex.miniclient.uibridge.Keys;

/**
 * Created by seans on 26/09/15.
 */
public class MiniClientKeyListener implements View.OnKeyListener {
    private static final Logger log = LoggerFactory.getLogger(MiniClientKeyListener.class);
    private static String PUNCTUATION = "`~!@#$%^&*()_+{}|:\"<>?-=[];'./\\,";

    static AndroidKeyEventMapper keyEventMapper = new AndroidKeyEventMapper();

    /**
     * HOME cannot be easily mapped to another Event.  Used to be able to do that, not any more.
     */

    static {
        // navivation native keymap
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, EventRouter.UP);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, EventRouter.DOWN);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, EventRouter.LEFT);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, EventRouter.RIGHT);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, EventRouter.SELECT);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_BUTTON_B, EventRouter.BACK);

        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_PAGE_UP, EventRouter.PAGE_UP);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_PAGE_DOWN, EventRouter.PAGE_DOWN);

        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PAUSE, EventRouter.MEDIA_PAUSE);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PLAY, EventRouter.MEDIA_PLAY);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, EventRouter.MEDIA_PLAY_PAUSE);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_MEDIA_STOP, EventRouter.MEDIA_STOP);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_BUTTON_Y, EventRouter.MEDIA_PLAY_PAUSE);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_BUTTON_X, EventRouter.MEDIA_STOP);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, EventRouter.MEDIA_FF);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_MEDIA_REWIND, EventRouter.MEDIA_REW);

        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_ENTER, EventRouter.ENTER);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_MENU, EventRouter.OPTIONS);
        EventRouter.NATIVE_UI_KEYMAP.put(KeyEvent.KEYCODE_HOME, EventRouter.HOME);

        // UI Long Presses
        EventRouter.NATIVE_UI_LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, EventRouter.OPTIONS);
        EventRouter.NATIVE_UI_LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_ENTER, EventRouter.OPTIONS);
        EventRouter.NATIVE_UI_LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, EventRouter.OPTIONS);
        EventRouter.NATIVE_UI_LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, EventRouter.MEDIA_PAUSE);
        EventRouter.NATIVE_UI_LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, EventRouter.MEDIA_PLAY);
        EventRouter.NATIVE_UI_LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, EventRouter.MEDIA_STOP);
    }

    private final MiniClient client;
    int skipKey = -1;
    boolean skipUp = false;

    Map<Object, UserEvent> LONG_KEYMAP;
    Map<Object, UserEvent> KEYMAP;

    public MiniClientKeyListener(MiniClient client) {
        this.client = client;

        LONG_KEYMAP = EventRouter.NATIVE_UI_LONGPRESS_KEYMAP;
        KEYMAP = EventRouter.NATIVE_UI_KEYMAP;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // log.debug("KEYS: DOWN KEYCODE: " + keyCode + "; " + event + "; Video Playing: " + client.isVideoPlaying());
            if (LONG_KEYMAP.containsKey(keyCode)) {
                if (event.isLongPress()) {
                    log.debug("KEYS: LONG PRESS KEYCODE: {}; {}", keyCode, event);
                    if (LONG_KEYMAP.containsKey(keyCode)) {
                        skipKey = keyCode;
                        UserEvent key = LONG_KEYMAP.get(keyCode);
                        if (key == null) {
                            log.debug("KEYS: Invalid Key Code: {}", keyCode);
                            return false;
                        }
                        if (client.properties().getBoolean(PrefStore.Keys.long_press_select_for_osd, true) &&
                                (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                            client.eventbus().post(ShowNavigationEvent.INSTANCE);
                        } else {
                            EventRouter.post(client, key);
                        }
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

            // check if we are handling a keypress completion
            // send a-z 0-9 and PUNCTUATION as is
            if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z
                    || keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9
                    || PUNCTUATION.indexOf(event.getUnicodeChar()) != -1) {
                //log.debug("KEYPRESS: {}; {}; {}", (char) event.getUnicodeChar(), (char) event.getUnicodeChar(KeyEvent.META_SHIFT_LEFT_ON), event.getUnicodeChar());
                char toSend = (char) event.getUnicodeChar();
                if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
                    toSend = (char) event.getUnicodeChar(KeyEvent.META_SHIFT_LEFT_ON);
                }
                client.getCurrentConnection().postKeyEvent(toSend, androidToSageKeyModifier(event), (char) event.getUnicodeChar());
                return true;
            }

            // bit of hack to handle hiding system UI when keyboard is visible
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                client.eventbus().post(BackPressedEvent.INSTANCE);
                return true;
            }

            if (KEYMAP.containsKey(keyCode)) {
                UserEvent key = KEYMAP.get(keyCode);
                log.debug("KEYS: POST KEYCODE: {}; {}; longpress?: {}", keyCode, event, event.isLongPress());
                EventRouter.post(client, key);
                return true;
            } else {
                if (client.properties().getBoolean(PrefStore.Keys.debug_log_unmapped_keypresses, false)) {
                    log.debug("KEYS: Unmapped Key Code: {}", event);
                    try {
                        Toast.makeText(MiniclientApplication.get(), "UNMAPPED KEY: " + keyEventMapper.getFieldName(event.getKeyCode()), Toast.LENGTH_LONG).show();
                    } catch (Throwable t) {
                    }
                }
            }
        }

        return false;
    }

    private int androidToSageKeyModifier(KeyEvent event) {
        int modifiers = 0;
        if (event.isShiftPressed()) {
            modifiers = modifiers | Keys.SHIFT_MASK;
        }
        if (event.isCtrlPressed()) {
            modifiers = modifiers | Keys.CTRL_MASK;
        }
        if (event.isAltPressed()) {
            modifiers = modifiers | Keys.ALT_MASK;
        }
        return 0;
    }
}
