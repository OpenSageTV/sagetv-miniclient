package sagex.miniclient.android.gdx;

import android.view.KeyEvent;

import sagex.miniclient.MiniClient;
import sagex.miniclient.uibridge.EventRouter;

/**
 * Created by seans on 26/09/15.
 */
public class VideoPausedKeyListener extends BaseKeyListener {

    public VideoPausedKeyListener(MiniClient client) {
        super(client);
    }

    @Override
    protected void initializeKeyMaps() {
        super.initializeKeyMaps();

        // Key Mappings when VIDEO is playing, ie, player state == PAUSED
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, EventRouter.MEDIA_PLAY_PAUSE);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, EventRouter.MEDIA_REW);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, EventRouter.MEDIA_FF);
    }
}
