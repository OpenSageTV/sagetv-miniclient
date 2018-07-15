package sagex.miniclient.android.ui;

import android.content.Context;
import android.view.KeyEvent;

import sagex.miniclient.MiniClient;
import sagex.miniclient.SageCommand;
import sagex.miniclient.android.ui.BaseKeyListener;
import sagex.miniclient.uibridge.EventRouter;

/**
 * Created by seans on 26/09/15.
 */
public class VideoPausedKeyListener extends BaseKeyListener {

    public VideoPausedKeyListener(Context context, MiniClient client) {
        super(context, client);
    }

    @Override
    protected void initializeKeyMaps() {
        super.initializeKeyMaps();

        // Key Mappings when VIDEO is playing, ie, player state == PAUSED
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, SageCommand.PLAY_PAUSE);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, SageCommand.REW);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, SageCommand.FF);
    }
}
