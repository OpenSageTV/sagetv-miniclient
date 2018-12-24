package sagex.miniclient.android.ui;

import android.content.Context;
import android.view.KeyEvent;

import sagex.miniclient.MiniClient;
import sagex.miniclient.SageCommand;

/**
 * Created by seans on 26/09/15.
 */
public class GuideKeyListener extends BaseKeyListener {


    public GuideKeyListener(Context context, MiniClient client) {
        super(context, client);

    }

    @Override
    protected void initializeKeyMaps() {
        super.initializeKeyMaps();

        // Key Mapping for GUIDE
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, SageCommand.REW_2);
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, SageCommand.FF_2);
    }
}
