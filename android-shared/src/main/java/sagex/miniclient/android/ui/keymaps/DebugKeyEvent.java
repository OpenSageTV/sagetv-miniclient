package sagex.miniclient.android.ui.keymaps;

import android.view.KeyEvent;

public class DebugKeyEvent {
    public final int keyCode;
    public final KeyEvent event;
    public final boolean longPress;
    public final String fieldName;

    public DebugKeyEvent(int keyCode, KeyEvent event, boolean longPress, String fieldName) {
        this.keyCode = keyCode;
        this.event = event;
        this.longPress = longPress;
        this.fieldName = fieldName;
    }
}
