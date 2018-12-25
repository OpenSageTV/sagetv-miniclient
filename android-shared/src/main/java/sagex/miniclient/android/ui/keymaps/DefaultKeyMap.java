package sagex.miniclient.android.ui.keymaps;

import android.view.KeyEvent;

import sagex.miniclient.MiniClient;
import sagex.miniclient.SageCommand;
import sagex.miniclient.android.events.BackPressedEvent;
import sagex.miniclient.android.preferences.MediaMappingPreferences;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.EventRouter;

public class DefaultKeyMap extends KeyMap {
    private final int keyRepeatRateDelay;
    private final int keyInitialRepeatDelay;

    private MediaMappingPreferences prefs;

    public DefaultKeyMap(KeyMap parent, MiniClient client) {
        super(parent);
        this.prefs = new MediaMappingPreferences(client.properties());

        // set our repeats from the configuration
        keyRepeatRateDelay = client.properties().getInt(PrefStore.Keys.repeat_key_ms, 100);
        keyInitialRepeatDelay = client.properties().getInt(PrefStore.Keys.repeat_key_delay_ms, 1000);
    }

    @Override
    public int getKeyRepeatRateMS(int keyCode) {
        return keyRepeatRateDelay;
    }

    @Override
    public int getKeyRepeatDelayMS(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && prefs.isLongPressSelectShowOSDNav()) {
            // only wait 500ms to show the OSD
            return 500;
        }
        return keyInitialRepeatDelay;
    }

    @Override
    public void initializeKeyMaps() {
        super.initializeKeyMaps();

        // easy to get home
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_BACK, SageCommand.HOME);

        // navivation native keymap
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, prefs.getUp());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, prefs.getDown());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, prefs.getLeft());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, prefs.getRight());

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
        KEYMAP.put(KeyEvent.KEYCODE_FORWARD_DEL, prefs.getDelete());
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
    public boolean isNavigationKey(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            return true;
        }
        return super.isNavigationKey(keyCode);
    }

    @Override
    public boolean hasSageCommandOverride(int keyCode, boolean longPress) {
        return !longPress && keyCode == KeyEvent.KEYCODE_BACK;
    }

    @Override
    public void performSageCommandOverride(int keyCode, MiniClient client, boolean longPress) {
        if (!longPress && keyCode == KeyEvent.KEYCODE_BACK) {
            // bit of hack to handle hiding system UI when keyboard is visible
            client.eventbus().post(BackPressedEvent.INSTANCE);
            // note this might get cancelled if the OSD was visible and we closed it in the previous line above
            EventRouter.postCommand(client, SageCommand.BACK);
        } else {
            super.performSageCommandOverride(keyCode, client, longPress);
        }
    }

    @Override
    public boolean shouldCancelLongPress(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
            return true;
        return super.shouldCancelLongPress(keyCode);
    }
}
