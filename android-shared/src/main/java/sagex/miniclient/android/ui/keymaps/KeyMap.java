package sagex.miniclient.android.ui.keymaps;

import java.util.HashMap;
import java.util.Map;

import sagex.miniclient.MiniClient;
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
        if (parent != null) return parent.getKeyRepeatRateMS(keyCode);
        // should never get here
        return -1;
    }

    public int getKeyRepeatDelayMS(int keyCode) {
        if (parent != null) return parent.getKeyRepeatDelayMS(keyCode);
        // should never get here
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

    public boolean hasSageCommandOverride(int keyCode, boolean longPress) {
        if (parent != null) return parent.hasSageCommandOverride(keyCode, longPress);
        return false;
    }

    public void performSageCommandOverride(int keyCode, MiniClient client, boolean longPress) {
        if (parent != null) parent.performSageCommandOverride(keyCode, client, longPress);
    }

    public boolean shouldCancelLongPress(int keyCode) {
        return false;
    }
}
