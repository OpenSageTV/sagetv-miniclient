package sagex.miniclient.android.ui.keymaps;

import android.view.KeyEvent;

import sagex.miniclient.SageCommand;

public class GuideKeyMap extends KeyMap {
    public GuideKeyMap(KeyMap parent) {
        super(parent);
    }

    @Override
    public void initializeKeyMaps() {
        super.initializeKeyMaps();

        // Key Mapping for GUIDE
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, SageCommand.REW_2);
        LONGPRESS_KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, SageCommand.FF_2);
    }

    @Override
    public int getKeyRepeatRateMS(int keyCode) {
        // prevent paging forward/back using long press from happening too quickly
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
            return 750;

        if (parent != null) return parent.getKeyRepeatRateMS(keyCode);

        // should never get here
        return 0;
    }
}
