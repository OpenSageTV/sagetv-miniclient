package sagex.miniclient.android.ui.keymaps;

import android.content.Context;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.MiniClient;
import sagex.miniclient.SageCommand;
import sagex.miniclient.android.AndroidKeyEventMapper;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.preferences.MediaMappingPreferences;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.EventRouter;
import sagex.miniclient.uibridge.Keys;
import sagex.miniclient.util.VerboseLogging;


/**
 * Modified by jvl711 on 7/8/2018 - Add customizable mappings.  Allow repeat on the key presses.
 *
 * Created by seans on 26/09/15.
 */
public class KeyMapProcessor {
    // hack from the onBackPressed so that we don't process it twice
    public static boolean skipBackOneTime = false;
    private final AudioManager am;
    private final boolean soundEffects;

    /**
     * NOTE:
     * HOME cannot be easily mapped to another Event.  Used to be able to do that, not any more.
     *
     * jvl711
     * NOTE: I thik there are some other keys that might also be difficult to remap.  For instance Volume Up/Volume Down
     */

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected static String PUNCTUATION = "`~!@#$%^&*()_+{}|:\" <>?-=[];'./\\,";
    protected static AndroidKeyEventMapper keyEventMapper = new AndroidKeyEventMapper();

    protected final MiniClient client;
    protected int flircMeta = 0;
    boolean skipUp = false;
    boolean longPress = false;
    boolean longPressCancel = false;
    long longPressTime = 0;

    protected Context context;
    private MediaMappingPreferences prefs;

    public KeyMapProcessor(MiniClient client, MediaMappingPreferences prefs, AudioManager am)
    {
        this.client = client;
        this.prefs = prefs;
        this.am = am;
        this.soundEffects = prefs.isSoundEffectsEnabled();
    }

    public boolean onKey(KeyMap keyMap, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (longPressCancel) return true;

            if (VerboseLogging.LOG_KEYS)
                log.debug("LONG DOWN: {} - {} -- {}", event.getEventTime(), event.getDownTime(), event);

            // check for longpress
            if (!longPress && event.getEventTime() - event.getDownTime() > keyMap.getKeyRepeatDelayMS(keyCode)) {
                longPress = true;
                longPressTime = event.getEventTime();

                if (VerboseLogging.LOG_KEYS)
                    log.debug("FIRE: LongPress {} {}", longPressTime, event);

                handleKeyPress(keyMap, keyCode, event, longPress);

                // some long press actions might only want to be processed once, like, back, or select
                if (keyMap.shouldCancelLongPress(keyCode)) {
                    if (VerboseLogging.LOG_KEYS)
                        log.debug("Cancel LongPress Repeats {}", event);
                    longPressCancel = true;
                }

                return true;
            }

            // if longpress has started, check for repeats
            if (longPress && event.getEventTime() - longPressTime > keyMap.getKeyRepeatRateMS(keyCode)) {
                if (VerboseLogging.LOG_KEYS)
                    log.debug("FIRE: LongPress {} Repeat {} - {}", longPressTime, event.getEventTime(), event);
                longPressTime = event.getEventTime();
                handleKeyPress(keyMap, keyCode, event, longPress);
                return true;
            } else {
                // for navigation keys we fire them once, and then start the delay/repeat loops
                // ie, down will move down, but when held it will start repeating after the delay
                if (!skipUp && keyMap.isNavigationKey(keyCode)) {
                    skipUp = true;
                    handleKeyPress(keyMap, keyCode, event, false);
                    return true;
                } else {
                    // waiting for longpress/repeat
                    return true;
                }
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            if (longPress || skipUp) {
                if (VerboseLogging.LOG_KEYS)
                    log.debug("Long Press UP: Do Nothing.");
                longPressTime = 0;
                longPress = false;
                longPressCancel = false;
                skipUp = false;
            } else {
                if (VerboseLogging.LOG_KEYS)
                    log.debug("UP: {}", event);

                // pretty much all normal keyboard keys will get handled here
                handleKeyPress(keyMap, keyCode, event, false);
            }
        }

        return true;
    }

    private void handleKeyPress(KeyMap keyMap, int keyCode, KeyEvent event, boolean longPress) {
        // this is really a hack because of the on screen controls
        // when we close them, we don't want to process the "back"
        if (skipBackOneTime) {
            skipBackOneTime = false;
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                log.debug("Skipping Back Event one time.");
                return;
            }
        }

        if (prefs.debugKeyPresses()) {
            client.eventbus().post(new DebugKeyEvent(keyCode, event, longPress, keyEventMapper.getFieldName(event.getKeyCode())));
        }

        playClickSound();

        SageCommand command = null;

        if (keyMap.hasSageCommandOverride(keyCode, longPress)) {
            keyMap.performSageCommandOverride(keyCode, client, longPress);
            return;
        }

        if (longPress && keyMap.hasLongPress(keyCode)) {
            command = keyMap.getLongPressCommand(keyCode);
        }

        if (!longPress || command == null) {
            command = keyMap.getNormalPressCommand(keyCode);
        }

        if (command != null) {
            if (VerboseLogging.LOG_KEYS)
                log.debug("Sending Sage Command {} for Event {}", command, event);
            EventRouter.postCommand(client, command);
            return;
        }

        // this is normal keys like a,b,c, etc.
        handleDefaultEvent(keyCode, event);
    }

    private void handleDefaultEvent(int keyCode, KeyEvent event) {
        if (VerboseLogging.LOG_KEYS)
            log.debug("Handle Default Key Event: {} {}", keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT) {
            flircMeta += Keys.CTRL_MASK;
            if (VerboseLogging.LOG_KEYS)
                log.debug("FLIRC Meta Ctrl");
            return;
        }

        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            flircMeta += Keys.SHIFT_MASK;
            if (VerboseLogging.LOG_KEYS)
                log.debug("FLIRC Meta Shift");
            return;
        }

        if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
            flircMeta += Keys.ALT_MASK;
            if (VerboseLogging.LOG_KEYS)
                log.debug("FLIRC Meta Alt");
            return;
        }

        if (flircMeta > 0) {
            try {
                processFlircKeyMetaKey(keyCode, event);
                return;
            } finally {
                if (VerboseLogging.LOG_KEYS)
                    log.debug("Resetting Flirc Meta");
                flircMeta = 0;
            }
        }

        // Check to see if this is a keyboard command
        if ((keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) || (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9)
                || keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_TAB || PUNCTUATION.indexOf(event.getUnicodeChar()) != -1) {
            char toSend = (char) event.getUnicodeChar();

            if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
                toSend = (char) event.getUnicodeChar(KeyEvent.META_SHIFT_LEFT_ON);
            }

            client.getCurrentConnection().postKeyEvent(toSend, androidToSageKeyModifier(event), (char) event.getUnicodeChar());

            return;
        }

        if (keyCode >= KeyEvent.KEYCODE_F1 && keyCode <= KeyEvent.KEYCODE_F12) {
            //F1 Virtual Code = 112
            //F1 KeyCode = 131

            //KeyEvent.KEYCODE_PAGE_DOWN = 93
            //Keys.VK_PAGE_DOWN = 34

            client.getCurrentConnection().postKeyEvent((keyCode - 19), 0, (char) (keyCode - 19));
            return;
        }

        if (client.properties().getBoolean(PrefStore.Keys.debug_log_unmapped_keypresses, false)) {
            if (VerboseLogging.LOG_KEYS)
                log.debug("KEYS: Unmapped Key Code: {}", event);

            try {
                Toast.makeText(MiniclientApplication.get(), "UNMAPPED KEY: " + keyEventMapper.getFieldName(event.getKeyCode()), Toast.LENGTH_LONG).show();
            } catch (Throwable t) {
            }
        }
    }

    private void processFlircKeyMetaKey(int keyCode, KeyEvent event)
    {
        if ((keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) || (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9)
                || keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_TAB || PUNCTUATION.indexOf(event.getUnicodeChar()) != -1)
        {
            char toSend = (char) event.getUnicodeChar();

            if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z)
            {
                toSend = (char) event.getUnicodeChar(KeyEvent.META_SHIFT_LEFT_ON);
            }

            if (VerboseLogging.LOG_KEYS)
                log.debug("FLIRC: Sending {} with meta: {}", String.valueOf(toSend), flircMeta);
            client.getCurrentConnection().postKeyEvent(toSend, flircMeta, (char) event.getUnicodeChar());
        }
    }

    protected int androidToSageKeyModifier(KeyEvent event)
    {
        int modifiers = 0;

        if (event.isShiftPressed())
        {
            modifiers = modifiers | Keys.SHIFT_MASK;
        }
        if (event.isCtrlPressed())
        {
            modifiers = modifiers | Keys.CTRL_MASK;
        }
        if (event.isAltPressed())
        {
            modifiers = modifiers | Keys.ALT_MASK;
        }

        return modifiers;
    }

    void playClickSound() {
        if (soundEffects) {
            float vol = 0.3f; //This will be half of the default system sound
            am.playSoundEffect(AudioManager.FX_KEY_CLICK, vol);
        }
    }
}
