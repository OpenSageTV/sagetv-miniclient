package sagex.miniclient.android.ui.keymaps;

import android.view.KeyEvent;

import sagex.miniclient.MiniClient;
import sagex.miniclient.SageCommand;
import sagex.miniclient.android.preferences.MediaMappingPreferences;
import sagex.miniclient.uibridge.EventRouter;

public class VideoPausedKeyMap extends KeyMap {
    private MediaMappingPreferences prefsVideo;

    public VideoPausedKeyMap(KeyMap parent, MediaMappingPreferences prefsVideo) {
        super(parent);
        this.prefsVideo = prefsVideo;
    }

    @Override
    public void initializeKeyMaps() {
        super.initializeKeyMaps();

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

    @Override
    public boolean hasSageCommandOverride(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_BACK;
    }

    @Override
    public void performSageCommandOverride(int keyCode, MiniClient client) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // if video is playing, then, stop it.
            EventRouter.postCommand(client, SageCommand.STOP);
        } else {
            super.performSageCommandOverride(keyCode, client);
        }
    }
}
