package sagex.miniclient.android.ui;

import android.content.Context;
import android.view.View;

import sagex.miniclient.MiniClient;

public interface AndroidUIController {
    void setupVideoFrame();
    void removeVideoFrame();

    void finish();

    void setConnectingIsVisible(boolean b);

    Object getSystemService(String windowService);

    boolean isSwitchingPlayerOneTime();

    MiniClient getClient();


    void runOnUiThread(Runnable runnable);

    View getVideoView();
    View getUIView();

    Context getContext();

    void showHideKeyboard(boolean hasTextInput);
}
