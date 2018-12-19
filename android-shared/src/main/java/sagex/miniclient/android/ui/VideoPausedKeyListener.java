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
public class VideoPausedKeyListener extends BaseKeyListener
{

    private MediaMappingPreferences prefsVideo;

    public VideoPausedKeyListener(Context context, MiniClient client)
    {
        super(context, client);
    }

    @Override
    protected void initializeKeyMaps()
    {
        super.initializeKeyMaps();

        prefsVideo = new MediaMappingPreferences(context, "videopaused");

        // Key Mappings when VIDEO is playing, ie, player state == PAUSED
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, prefsVideo.getSelect());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, prefsVideo.getLeft());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, prefsVideo.getRight());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, prefsVideo.getUp());
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, prefsVideo.getDown());

        // since we are remapping left and right, then, remap long presses to send left/right
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, prefsVideo.getSelectLongPress());
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, prefsVideo.getLeftLongPress());
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, prefsVideo.getRightLongPress());
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, prefsVideo.getUpLongPress());
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, prefsVideo.getDownLongPress());
    }
}
