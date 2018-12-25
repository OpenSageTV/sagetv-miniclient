package sagex.miniclient.android.ui.keymaps;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.MiniClient;
import sagex.miniclient.SageCommand;
import sagex.miniclient.android.AndroidKeyEventMapper;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.events.ShowNavigationEvent;
import sagex.miniclient.android.preferences.MediaMappingPreferences;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.EventRouter;
import sagex.miniclient.uibridge.Keys;


/**
 * Modified by jvl711 on 7/8/2018 - Add customizable mappings.  Allow repeat on the key presses.
 *
 * Created by seans on 26/09/15.
 */
public class KeyMapProcessor {
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
    protected int skipKey = -1;
    protected boolean skipUp = false;
    protected int flircMeta = 0;

    protected Context context;
    private MediaMappingPreferences prefs;

    protected KeyEvent lastEvent;

    public KeyMapProcessor(MiniClient client, MediaMappingPreferences prefs)
    {
        this.client = client;
        this.prefs = prefs;
    }

    public boolean onKey(KeyMap keyMap, int keyCode, KeyEvent event)
    {

        if (keyMap.hasLongPress(keyCode)) //Handle long press event of mapped key
        {
            SageCommand command = keyMap.getLongPressCommand(keyCode);

            if(command == SageCommand.NONE)
            {
                //Mapping turned off on this command.  Stop processing
                log.debug("KEYS: LONG PRESS KEYCODE SET TO NONE: {}; {}", keyCode, event);
                return false;
            }

            if (event.getAction() == KeyEvent.ACTION_DOWN && event.isLongPress()
                    || (lastEvent != null && this.skipUp && event.getRepeatCount() > 1 && (event.getEventTime() - lastEvent.getEventTime()) >= keyMap.getKeyRepeatRateMS(keyCode)
                    && (event.getEventTime() - event.getDownTime()) >= keyMap.getKeyRepeatDelayMS(keyCode)))
            {
                log.debug("KEYS: LONG PRESS KEYCODE: {}; {}", keyCode, event);

                skipKey = keyCode;

                if (prefs.isLongPressSelectShowOSDNav() &&
                        (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
                                event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A))
                {
                    client.eventbus().post(ShowNavigationEvent.INSTANCE);
                }
                else
                {
                    EventRouter.postCommand(client, command);
                }

                lastEvent = event;
                skipUp = true;

                return true;
            }
            else if(event.getAction() == KeyEvent.ACTION_UP)
            {
                if (skipUp && skipKey == keyCode)
                {
                    // After a Long Press
                    skipUp = false;
                    skipKey = -1;
                    log.debug("KEYS: Skipping Key {}", keyCode);
                    return true;
                }

                if (keyMap.hasSageCommandOverride(keyCode)) {
                    keyMap.performSageCommandOverride(keyCode, client);
                    return true;
                }

                if (keyMap.hasNormalPress(keyCode))
                {
                    command = keyMap.getNormalPressCommand(keyCode);

                    log.debug("KEYS: POST KEYCODE: {}; {}; longpress?: {}", keyCode, event, event.isLongPress());
                    EventRouter.postCommand(client, command);
                    return true;
                }
            }

            log.warn("Unhandled Key in Long Press: {} - {}", keyCode, event);
            return true;
        } else if (keyMap.hasNormalPress(keyCode)) {
            //Handle standard mapped press events
            SageCommand command = keyMap.getNormalPressCommand(keyCode);

            if(command == SageCommand.NONE)
            {
                //Mapping turned off on this command.  Stop processing
                log.debug("KEYS: POST KEYCODE SET TO NONE: {}; {}", keyCode, event);
                return false;
            }

            if(event.getAction() == KeyEvent.ACTION_DOWN) {
                //If this is repeat event
                if (event.getRepeatCount() > 0 && lastEvent != null && (event.getEventTime() - lastEvent.getEventTime()) < keyMap.getKeyRepeatRateMS(keyCode)
                        && (event.getEventTime() - event.getDownTime()) < keyMap.getKeyRepeatDelayMS(keyCode)) {
                    log.debug("Repeat time since last event:" + (event.getEventTime() - lastEvent.getEventTime()));
                    log.debug("Repeat time since keydown event:" + (event.getEventTime() - event.getDownTime()));
                } else {


                    log.debug("KEYS: POST KEYCODE: {}; {}; longpress?: {}", keyCode, event, event.isLongPress());
                    EventRouter.postCommand(client, command);
                    lastEvent = event;
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                if (skipUp && skipKey == keyCode) {
                    // After a Long Press
                    skipUp = false;
                    skipKey = -1;
                    log.debug("KEYS: Skipping Key {}", keyCode);
                    return true;
                }

                if (keyMap.hasSageCommandOverride(keyCode)) {
                    lastEvent = event;
                    keyMap.performSageCommandOverride(keyCode, client);
                    return true;
                }
            }

            return true;
        }
        else //Hanlde other events if they are keyboard or flirc.  Otherwise it is unmapped and will not be executed
        {

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                //If this is repeat event
                if (event.getRepeatCount() > 0 && lastEvent != null && (event.getEventTime() - lastEvent.getEventTime()) < keyMap.getKeyRepeatRateMS(keyCode)
                        && (event.getEventTime() - event.getDownTime()) < keyMap.getKeyRepeatDelayMS(keyCode)) {
                    log.debug("Repeat time since last event:" + (event.getEventTime() - lastEvent.getEventTime()));
                } else {

                    if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT) {
                        flircMeta += Keys.CTRL_MASK;
                        log.debug("FLIRC Meta Ctrl");

                        lastEvent = event;
                        return true;
                    }

                    if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
                        flircMeta += Keys.SHIFT_MASK;
                        log.debug("FLIRC Meta Shift");

                        lastEvent = event;
                        return true;
                    }

                    if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
                        flircMeta += Keys.ALT_MASK;
                        log.debug("FLIRC Meta Alt");

                        lastEvent = event;
                        return true;
                    }

                    if (flircMeta > 0) {
                        try {
                            processFlircKeyMetaKey(keyCode, event);

                            lastEvent = event;
                            return true;
                        } finally {
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

                        lastEvent = event;
                        return true;
                    }

                    if (keyCode >= KeyEvent.KEYCODE_F1 && keyCode <= KeyEvent.KEYCODE_F12) {
                        //F1 Virtual Code = 112
                        //F1 KeyCode = 131

                        //KeyEvent.KEYCODE_PAGE_DOWN = 93
                        //Keys.VK_PAGE_DOWN = 34

                        client.getCurrentConnection().postKeyEvent((keyCode - 19), 0, (char) (keyCode - 19));
                        return true;
                    }

                    if (client.properties().getBoolean(PrefStore.Keys.debug_log_unmapped_keypresses, false)) {
                        log.debug("KEYS: Unmapped Key Code: {}", event);

                        try {
                            Toast.makeText(MiniclientApplication.get(), "UNMAPPED KEY: " + keyEventMapper.getFieldName(event.getKeyCode()), Toast.LENGTH_LONG).show();
                        } catch (Throwable t) {
                        }

                    }

                    lastEvent = event;
                    return true;
                }
            }

            return true;
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
}
