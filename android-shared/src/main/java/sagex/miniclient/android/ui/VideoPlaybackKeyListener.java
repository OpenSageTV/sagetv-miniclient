package sagex.miniclient.android.ui;

import android.content.Context;
import android.view.KeyEvent;

import sagex.miniclient.MiniClient;
import sagex.miniclient.SageCommand;
import sagex.miniclient.android.preferences.MediaMappingPreferences;
import sagex.miniclient.android.ui.BaseKeyListener;
import sagex.miniclient.uibridge.EventRouter;

/**
 * Created by seans on 26/09/15.
 */
public class VideoPlaybackKeyListener extends BaseKeyListener {

    public VideoPlaybackKeyListener(Context context, MiniClient client) {
        super(context, client);

        MediaMappingPreferences prefs = new MediaMappingPreferences(context, "videoplaying");
    }

    @Override
    protected void initializeKeyMaps()
    {
        super.initializeKeyMaps();

        // Key Mappings when VIDEO is playing, ie, player state == PLAY
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, prefs.getSelect());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, prefs.getLeft());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, prefs.getRight());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, prefs.getUp());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, prefs.getDown());

        // since we are remapping left and right, then, remap long presses to send left/right
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, prefs.getSelectLongPress());
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, prefs.getLeftLongPress());
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, prefs.getRightLongPress());
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, prefs.getUpLongPress());
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, prefs.getDownLongPress());
    }
}
