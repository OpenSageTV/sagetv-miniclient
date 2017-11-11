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
    protected static String PUNCTUATION = "`~!@#$%^&*()_+{}|:\" <>?-=[];'./\\,";
    protected static AndroidKeyEventMapper keyEventMapper = new AndroidKeyEventMapper();

    protected final MiniClient client;
    protected int skipKey = -1;
    protected boolean skipUp = false;
    protected int flircMeta = 0;

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

        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_SELECT, EventRouter.SELECT);
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_START, EventRouter.MEDIA_PLAY_PAUSE);

        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_R1, EventRouter.DELETE);
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_R2, EventRouter.HOME);

        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_0, EventRouter.NUM_0);
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_1, EventRouter.NUM_1);
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_2, EventRouter.NUM_2);
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_3, EventRouter.NUM_3);
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_4, EventRouter.NUM_4);
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_5, EventRouter.NUM_5);
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_6, EventRouter.NUM_6);
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_7, EventRouter.NUM_7);
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_8, EventRouter.NUM_8);
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_9, EventRouter.NUM_9);

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

        // for harmony remote
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_ENTER, EventRouter.ENTER);

        // flirc
        KEYMAP.put(KeyEvent.KEYCODE_ESCAPE, EventRouter.OPTIONS);
        KEYMAP.put(KeyEvent.KEYCODE_MOVE_HOME, EventRouter.HOME);

        // standard remotes
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
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_ENTER, EventRouter.OPTIONS);
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_ENTER, EventRouter.OPTIONS);
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, EventRouter.PAGE_UP);
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, EventRouter.PAGE_DOWN);
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, EventRouter.FORWORAD);
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, EventRouter.BACK);

        // guide and info
        KEYMAP.put(KeyEvent.KEYCODE_GUIDE, EventRouter.GUIDE);
        KEYMAP.put(KeyEvent.KEYCODE_INFO, EventRouter.INFO);

        // Fkeys
        KEYMAP.put(KeyEvent.KEYCODE_F1, EventRouter.F1);
        KEYMAP.put(KeyEvent.KEYCODE_F2, EventRouter.F2);
        KEYMAP.put(KeyEvent.KEYCODE_F3, EventRouter.F3);
        KEYMAP.put(KeyEvent.KEYCODE_F4, EventRouter.F4);
        KEYMAP.put(KeyEvent.KEYCODE_F5, EventRouter.F5);
        KEYMAP.put(KeyEvent.KEYCODE_F6, EventRouter.F6);
        KEYMAP.put(KeyEvent.KEYCODE_F7, EventRouter.F7);
        KEYMAP.put(KeyEvent.KEYCODE_F8, EventRouter.F8);
        KEYMAP.put(KeyEvent.KEYCODE_F9, EventRouter.F9);
        KEYMAP.put(KeyEvent.KEYCODE_F10, EventRouter.F10);
        KEYMAP.put(KeyEvent.KEYCODE_F11, EventRouter.F11);
        KEYMAP.put(KeyEvent.KEYCODE_F12, EventRouter.F12);
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
                                (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER)) {
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

            if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT) {
                flircMeta+=Keys.CTRL_MASK;
                log.debug("FLIRC Meta Ctrl");
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
                flircMeta+=Keys.SHIFT_MASK;
                log.debug("FLIRC Meta Shift");
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
                flircMeta+=Keys.ALT_MASK;
                log.debug("FLIRC Meta Alt");
                return true;
            }

            // if we have a multi-press FLIRC key, then process it
            if (flircMeta>0) {
                try {
                    processFlircKeyMetaKey(keyCode, event);
                } finally {
                    log.debug("Resetting Flirc Meta");
                    flircMeta=0;
                }
            }

            // check if we are handling a keypress completion
            // send a-z 0-9 and PUNCTUATION as is
            if ((keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z)
                    || (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9)
                    || keyCode == KeyEvent.KEYCODE_SPACE
                    || keyCode == KeyEvent.KEYCODE_TAB
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

    private void processFlircKeyMetaKey(int keyCode, KeyEvent event) {
        if ((keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z)
                || (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9)
                || keyCode == KeyEvent.KEYCODE_SPACE
                || keyCode == KeyEvent.KEYCODE_TAB
                || PUNCTUATION.indexOf(event.getUnicodeChar()) != -1) {
            //log.debug("KEYPRESS: {}; {}; {}", (char) event.getUnicodeChar(), (char) event.getUnicodeChar(KeyEvent.META_SHIFT_LEFT_ON), event.getUnicodeChar());
            char toSend = (char) event.getUnicodeChar();
            if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
                toSend = (char) event.getUnicodeChar(KeyEvent.META_SHIFT_LEFT_ON);
            }
            log.debug("FLIRC: Sending {} with meta: {}", String.valueOf(toSend), flircMeta);
            client.getCurrentConnection().postKeyEvent(toSend, flircMeta, (char) event.getUnicodeChar());
        }
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
        return modifiers;
    }
}
