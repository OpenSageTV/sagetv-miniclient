package sagex.miniclient.android.ui.keymaps;

import java.util.HashMap;
import java.util.Map;

import sagex.miniclient.SageCommand;

public class KeyMap {
    protected final KeyMap parent;

    Map<Object, SageCommand> LONGPRESS_KEYMAP = new HashMap<>();
    Map<Object, SageCommand> KEYMAP = new HashMap<>();

    public KeyMap(KeyMap parent) {
        this.parent = parent;
    }

    public void initializeKeyMaps() {
    }

    public int getKeyRepeatRateMS(int keyCode) {
        return -1;
    }

    public int getKeyRepeatDelayMS(int keyCode) {
        return -1;
    }

    public boolean hasLongPress(int keyCode) {
        boolean v = LONGPRESS_KEYMAP.containsKey(keyCode);
        if (v) return v;

        if (parent != null) {
            return parent.hasLongPress(keyCode);
        }
        return false;
    }

    public boolean hasNormalPress(int keyCode) {
        boolean v = KEYMAP.containsKey(keyCode);
        if (v) return v;
        if (parent != null) {
            return parent.hasNormalPress(keyCode);
        }
        return false;
    }

    public SageCommand getLongPressCommand(int keyCode) {
        SageCommand c = LONGPRESS_KEYMAP.get(keyCode);
        if (c != null) return c;
        if (parent != null) return parent.getLongPressCommand(keyCode);
        return null;
    }

    public SageCommand getNormalPressCommand(int keyCode) {
        SageCommand c = KEYMAP.get(keyCode);
        if (c != null) return c;
        if (parent != null) return parent.getNormalPressCommand(keyCode);
        return null;
    }
}
