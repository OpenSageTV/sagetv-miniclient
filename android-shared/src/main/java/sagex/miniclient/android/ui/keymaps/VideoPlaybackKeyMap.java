package sagex.miniclient.android.ui.keymaps;

import android.view.KeyEvent;

import sagex.miniclient.android.preferences.MediaMappingPreferences;

public class VideoPlaybackKeyMap extends KeyMap {
    private MediaMappingPreferences prefsVideo;

    public VideoPlaybackKeyMap(KeyMap parent, MediaMappingPreferences prefsVideo) {
        super(parent);
        this.prefsVideo = prefsVideo;
    }

    @Override
    public void initializeKeyMaps() {
        super.initializeKeyMaps();

        // Key Mappings when VIDEO is playing, ie, player state == PLAY
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
