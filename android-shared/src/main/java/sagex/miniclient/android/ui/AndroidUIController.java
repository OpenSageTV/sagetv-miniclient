package sagex.miniclient.android.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import sagex.miniclient.MiniClient;

public interface AndroidUIController {
    void setupVideoFrame();
    void removeVideoFrame();

    void finish();

    void setConnectingIsVisible(boolean b);

    Object getSystemService(String windowService);

    boolean isSwitchingPlayerOneTime();

    MiniClient getClient();

    void showErrorMessage(String message, String cause);
    void runOnUiThread(Runnable runnable);

    View getVideoView();
    View getUIView();
    TextView getPleaseWaitText();
    TextView getCaptionsText();

    Context getContext();

    void showHideKeyboard(boolean hasTextInput);
}
