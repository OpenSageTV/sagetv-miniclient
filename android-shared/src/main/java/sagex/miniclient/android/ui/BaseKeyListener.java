package sagex.miniclient.android.ui;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import sagex.miniclient.MiniClient;
import sagex.miniclient.SageCommand;
import sagex.miniclient.android.AndroidKeyEventMapper;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.events.BackPressedEvent;
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
public class BaseKeyListener implements View.OnKeyListener
{
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

    // These are overridden by prefs
    private long keyRepeatRateDelay;
    private long keyInitialRepeatDelay;

    Map<Object, SageCommand> LONGPRESS_KEYMAP;
    Map<Object, SageCommand> KEYMAP;

    public BaseKeyListener(Context context, MiniClient client)
    {
        this.client = client;
        this.context = context;
        this.prefs = new MediaMappingPreferences(context);

        LONGPRESS_KEYMAP = new HashMap<>();
        KEYMAP = new HashMap<>();

        // set our repeats from the configuration
        keyRepeatRateDelay = client.properties().getInt(PrefStore.Keys.repeat_key_ms, 100);
        keyInitialRepeatDelay = client.properties().getInt(PrefStore.Keys.repeat_key_delay_ms, 1000);

        initializeKeyMaps();
    }


    protected void initializeKeyMaps()
    {
        // easy to get home
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_BACK, SageCommand.HOME);

        // navivation native keymap
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP,  prefs.getUp());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN,  prefs.getDown());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT,  prefs.getLeft());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT,  prefs.getRight());

        //I am going to treat these all as [SELECT].  This is to make this less confusing.
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_ENTER, prefs.getSelect()); // for harmony remote
        KEYMAP.put(KeyEvent.KEYCODE_ENTER, prefs.getSelect());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, prefs.getSelect());
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_A, prefs.getSelect());

        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_0, prefs.getNum0());
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_1, prefs.getNum1());
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_2, prefs.getNum2());
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_3, prefs.getNum3());
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_4, prefs.getNum4());
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_5, prefs.getNum5());
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_6, prefs.getNum6());
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_7, prefs.getNum7());
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_8, prefs.getNum8());
        KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_9, prefs.getNum9());

        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PLAY, prefs.getPlay());
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PAUSE, prefs.getPause());
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, prefs.getPlayPause());
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_STOP, prefs.getStop());
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, prefs.getFastForward());
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_REWIND, prefs.getRewind());
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_NEXT, prefs.getNextTrack());
        KEYMAP.put(KeyEvent.KEYCODE_MEDIA_PREVIOUS, prefs.getPreviousTrack());

        KEYMAP.put(KeyEvent.KEYCODE_VOLUME_UP, prefs.getVolumeUp());
        KEYMAP.put(KeyEvent.KEYCODE_VOLUME_DOWN, prefs.getVolumeDown());
        KEYMAP.put(KeyEvent.KEYCODE_VOLUME_MUTE, prefs.getMute());
        KEYMAP.put(KeyEvent.KEYCODE_CHANNEL_UP, prefs.getChannelUp());
        KEYMAP.put(KeyEvent.KEYCODE_CHANNEL_DOWN, prefs.getChannelDown());

        // flirc
        KEYMAP.put(KeyEvent.KEYCODE_ESCAPE, SageCommand.OPTIONS);
        KEYMAP.put(KeyEvent.KEYCODE_MOVE_HOME, SageCommand.HOME);

        // standard remotes
        KEYMAP.put(KeyEvent.KEYCODE_HOME, SageCommand.HOME); //Not going to add to the custom list since it probably can not be remapped
        KEYMAP.put(KeyEvent.KEYCODE_MENU, prefs.getMenu());
        KEYMAP.put(KeyEvent.KEYCODE_GUIDE, prefs.getGuide());
        KEYMAP.put(KeyEvent.KEYCODE_INFO, prefs.getInfo());
        KEYMAP.put(KeyEvent.KEYCODE_DEL, prefs.getDelete());
        KEYMAP.put(KeyEvent.KEYCODE_SEARCH, prefs.getSearch());

        //These look to be gamepad buttons
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_SELECT, prefs.getGamepadSelect());
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_START, prefs.getGamepadStart());
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_A, prefs.getA());
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_Y, prefs.getY());
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_X, prefs.getX());
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_B, prefs.getB());
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_R1, prefs.getR1());
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_R2, prefs.getR2());
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_L1, prefs.getL1());
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_L2, prefs.getL2());

        //Program Keys
        //TODO: Investigate adding recordings and videos
        KEYMAP.put(KeyEvent.KEYCODE_PROG_YELLOW, prefs.getYellow());
        KEYMAP.put(KeyEvent.KEYCODE_PROG_BLUE, prefs.getBlue());
        KEYMAP.put(KeyEvent.KEYCODE_PROG_RED, prefs.getRed());
        KEYMAP.put(KeyEvent.KEYCODE_PROG_GREEN, prefs.getGreen());

        //Mapping them, but they are not currently configurable
        KEYMAP.put(KeyEvent.KEYCODE_PAGE_UP, SageCommand.PAGE_UP);
        KEYMAP.put(KeyEvent.KEYCODE_PAGE_DOWN, SageCommand.PAGE_DOWN);

        // UI Long Presses
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, prefs.getUpLongPress());
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, prefs.getDownLongPress());
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, prefs.getRightLongPress());
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, prefs.getLeftLongPress());

        //I am going to treat these all as [SELECT].  This is to make this less confusing.
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_NUMPAD_ENTER, prefs.getSelect()); // for harmony remote
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_ENTER, prefs.getSelect());
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, prefs.getSelect());
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_BUTTON_A, prefs.getSelect());
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event)
    {

        if(LONGPRESS_KEYMAP.containsKey(keyCode)) //Handle long press event of mapped key
        {
            SageCommand command = LONGPRESS_KEYMAP.get(keyCode);

            if(command == SageCommand.NONE)
            {
                //Mapping turned off on this command.  Stop processing
                log.debug("KEYS: LONG PRESS KEYCODE SET TO NONE: {}; {}", keyCode, event);
                return false;
            }

            if (event.getAction() == KeyEvent.ACTION_DOWN && event.isLongPress()
                    || (lastEvent!=null && this.skipUp && event.getRepeatCount() > 1 && (event.getEventTime()- lastEvent.getEventTime()) >= this.keyRepeatRateDelay
                    && (event.getEventTime() - event.getDownTime()) >= this.keyInitialRepeatDelay))
            {
                log.debug("KEYS: LONG PRESS KEYCODE: {}; {}", keyCode, event);

                skipKey = keyCode;
                //UserEvent key = new UserEvent(LONGPRESS_KEYMAP.get(keyCode));


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

                if (KEYMAP.containsKey(keyCode))
                {
                    command = KEYMAP.get(keyCode);

                    log.debug("KEYS: POST KEYCODE: {}; {}; longpress?: {}", keyCode, event, event.isLongPress());
                    EventRouter.postCommand(client, command);
                }

            }

            return true;
        }
        else if(KEYMAP.containsKey(keyCode)) //Handle standard mapped press events
        {
            SageCommand command = KEYMAP.get(keyCode);

            if(command == SageCommand.NONE)
            {
                //Mapping turned off on this command.  Stop processing
                log.debug("KEYS: POST KEYCODE SET TO NONE: {}; {}", keyCode, event);
                return false;
            }

            if(event.getAction() == KeyEvent.ACTION_DOWN)
            {
                //If this is repeat event
                if(event.getRepeatCount() > 0 && lastEvent!=null && (event.getEventTime() - lastEvent.getEventTime()) < this.keyRepeatRateDelay
                        && (event.getEventTime() - event.getDownTime()) < this.keyInitialRepeatDelay)
                {
                    log.debug("Repeat time since last event:" + (event.getEventTime() - lastEvent.getEventTime()));
                    log.debug("Repeat time since keydown event:" + (event.getEventTime() - event.getDownTime()));
                }
                else
                {


                    log.debug("KEYS: POST KEYCODE: {}; {}; longpress?: {}", keyCode, event, event.isLongPress());
                    EventRouter.postCommand(client, command);
                    lastEvent = event;
                }
            }

            return true;
        }
        else //Hanlde other events if they are keyboard or flirc.  Otherwise it is unmapped and will not be executed
        {

            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                //If this is repeat event
                if(event.getRepeatCount() > 0 && lastEvent!=null && (event.getEventTime() - lastEvent.getEventTime()) < this.keyRepeatRateDelay
                        && (event.getEventTime() - event.getDownTime()) < this.keyInitialRepeatDelay)
                {
                    log.debug("Repeat time since last event:" + (event.getEventTime() - lastEvent.getEventTime()));
                }
                else
                {

                    if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT)
                    {
                        flircMeta += Keys.CTRL_MASK;
                        log.debug("FLIRC Meta Ctrl");

                        lastEvent = event;
                        return true;
                    }

                    if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT)
                    {
                        flircMeta += Keys.SHIFT_MASK;
                        log.debug("FLIRC Meta Shift");

                        lastEvent = event;
                        return true;
                    }

                    if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT)
                    {
                        flircMeta += Keys.ALT_MASK;
                        log.debug("FLIRC Meta Alt");

                        lastEvent = event;
                        return true;
                    }

                    if (flircMeta > 0)
                    {
                        try
                        {
                            processFlircKeyMetaKey(keyCode, event);

                            lastEvent = event;
                            return true;
                        }
                        finally
                        {
                            log.debug("Resetting Flirc Meta");
                            flircMeta = 0;
                        }
                    }

                    // Check to see if this is a keyboard command
                    if ((keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) || (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9)
                            || keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_TAB || PUNCTUATION.indexOf(event.getUnicodeChar()) != -1)
                    {
                        char toSend = (char) event.getUnicodeChar();

                        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z)
                        {
                            toSend = (char) event.getUnicodeChar(KeyEvent.META_SHIFT_LEFT_ON);
                        }

                        client.getCurrentConnection().postKeyEvent(toSend, androidToSageKeyModifier(event), (char) event.getUnicodeChar());

                        lastEvent = event;
                        return true;
                    }

                    if(keyCode >= KeyEvent.KEYCODE_F1 && keyCode <= KeyEvent.KEYCODE_F12)
                    {
                        //F1 Virtual Code = 112
                        //F1 KeyCode = 131

                        //KeyEvent.KEYCODE_PAGE_DOWN = 93
                        //Keys.VK_PAGE_DOWN = 34

                        client.getCurrentConnection().postKeyEvent((keyCode - 19), 0, (char)(keyCode - 19));
                        return true;
                    }

                    if (client.properties().getBoolean(PrefStore.Keys.debug_log_unmapped_keypresses, false))
                    {
                        log.debug("KEYS: Unmapped Key Code: {}", event);

                        try
                        {
                            Toast.makeText(MiniclientApplication.get(), "UNMAPPED KEY: " + keyEventMapper.getFieldName(event.getKeyCode()), Toast.LENGTH_LONG).show();
                        }
                        catch (Throwable t) { }

                    }

                    // bit of hack to handle hiding system UI when keyboard is visible
                    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                    {
                        client.eventbus().post(BackPressedEvent.INSTANCE);
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
