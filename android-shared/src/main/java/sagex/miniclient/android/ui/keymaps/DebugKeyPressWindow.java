package sagex.miniclient.android.ui.keymaps;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.slf4j.LoggerFactory;

import sagex.miniclient.SageCommand;
import sagex.miniclient.android.R;

public class DebugKeyPressWindow {
    static final org.slf4j.Logger log = LoggerFactory.getLogger(DebugKeyPressWindow.class);

    TextView text = null;
    PopupWindow popupWindow = null;

    public void onCreate(Activity parent) {
        log.debug("Creating Debug Key Window");

        LayoutInflater layoutInflater = (LayoutInflater) parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.debug_keys_window, null);
        text = customView.findViewById(R.id.text);
        text.setText("Debug Keys");
        popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    public void show(Activity parent) {
        if (text == null || popupWindow == null) {
            onCreate(parent);
        }

        if (!popupWindow.isShowing()) {
            log.debug("Showing Debug Key Window");
            popupWindow.showAtLocation(parent.findViewById(android.R.id.content), Gravity.END | Gravity.TOP, 0, 0);
        }
    }

    public void hide() {
        popupWindow.dismiss();
    }

    public void showKey(String fieldName, boolean longPress, int keyCode) {
        String t = text.getText().toString();
        text.setText(String.format("%s - %s - %d", fieldName, (longPress) ? "LONG" : "NORM", keyCode) + "\n\n" + t);
    }

    public void showSageCommand(SageCommand command) {
        String t = text.getText().toString();
        text.setText(String.format("COMMAND: %s - %d", command.getDisplayName(), command.getEventCode()) + "\n" + t);
    }
}
