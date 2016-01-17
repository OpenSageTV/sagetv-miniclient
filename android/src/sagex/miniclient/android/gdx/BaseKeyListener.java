package sagex.miniclient.android.gdx;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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
public class BaseKeyListener implements View.OnKeyListener {
    /**
     * NOTE:
     * HOME cannot be easily mapped to another Event.  Used to be able to do that, not any more.
     */

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected static String PUNCTUATION = "`~!@#$%^&*()_+{}|:\"<>?-=[];'./\\,";
    protected static AndroidKeyEventMapper keyEventMapper = new AndroidKeyEventMapper();

    protected final MiniClient client;
    protected int skipKey = -1;
    protected boolean skipUp = false;

    Map<Object, UserEvent> LONGPRESS_KEYMAP;
    Map<Object, UserEvent> KEYMAP;

    public BaseKeyListener(MiniClient client) {
        this.client = client;
        LONGPRESS_KEYMAP = new HashMap<>();
        KEYMAP = new HashMap<>();
        initializeKeyMaps();
    }

    /**
     * Setup the Short and Long key press Mappings
     */
    protected void initializeKeyMaps() {
        // navivation native keymap
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, EventRouter.UP);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, EventRouter.DOWN);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, EventRouter.LEFT);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, EventRouter.RIGHT);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, EventRouter.SELECT);
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_B, EventRouter.BACK);

        KEYMAP.put(KeyEvent.KEYCODE_PAGE_UP, EventRouter.PAGE_UP);
        KEYMAP.put(KeyEvent.KEYCODE_PAGE_DOWN, EventRouter.PAGE_DOWN);

        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PAUSE, EventRouter.MEDIA_PAUSE);
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PLAY, EventRouter.MEDIA_PLAY);
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, EventRouter.MEDIA_PLAY_PAUSE);
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_STOP, EventRouter.MEDIA_STOP);
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_Y, EventRouter.MEDIA_PLAY_PAUSE);
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_X, EventRouter.MEDIA_STOP);
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, EventRouter.MEDIA_FF);
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_REWIND, EventRouter.MEDIA_REW);

        KEYMAP.put(KeyEvent.KEYCODE_ENTER, EventRouter.ENTER);
        KEYMAP.put(KeyEvent.KEYCODE_MENU, EventRouter.OPTIONS);
        KEYMAP.put(KeyEvent.KEYCODE_HOME, EventRouter.HOME);

        KEYMAP.put(KeyEvent.KEYCODE_DEL, EventRouter.BACKSPACE);

        // map search to OPTIONs, since we don't do SEARCH, yet
        KEYMAP.put(KeyEvent.KEYCODE_SEARCH, EventRouter.OPTIONS);

        KEYMAP.put(KeyEvent.KEYCODE_VOLUME_UP, EventRouter.VOLUME_UP);
        KEYMAP.put(KeyEvent.KEYCODE_VOLUME_DOWN, EventRouter.VOLUME_DOWN);
        KEYMAP.put(KeyEvent.KEYCODE_VOLUME_MUTE, EventRouter.VOLUME_MUTE);

        // UI Long Presses
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, EventRouter.OPTIONS);
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_ENTER, EventRouter.OPTIONS);
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, EventRouter.PAGE_UP);
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, EventRouter.PAGE_DOWN);
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, EventRouter.FORWORAD);
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, EventRouter.BACK);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // log.debug("KEYS: DOWN KEYCODE: " + keyCode + "; " + event + "; Video Playing: " + client.isVideoPlaying());
            if (LONGPRESS_KEYMAP.containsKey(keyCode)) {
                if (event.isLongPress()) {
                    log.debug("KEYS: LONG PRESS KEYCODE: {}; {}", keyCode, event);
                    if (LONGPRESS_KEYMAP.containsKey(keyCode)) {
                        skipKey = keyCode;
                        UserEvent key = LONGPRESS_KEYMAP.get(keyCode);
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

    protected int androidToSageKeyModifier(KeyEvent event) {
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
