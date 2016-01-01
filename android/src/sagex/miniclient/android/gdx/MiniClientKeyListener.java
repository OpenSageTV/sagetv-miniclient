package sagex.miniclient.android.gdx;

import android.view.KeyEvent;
import android.view.View;

import sagex.miniclient.MiniClient;

/**
 * Created by seans on 26/09/15.
 */
public class MiniClientKeyListener implements View.OnKeyListener {
    private final MiniClient client;

    BaseKeyListener normalKeyListener;
    VideoPausedKeyListener videoPausedKeyListener;
    VideoPlaybackKeyListener videoPlaybackKeyListener;

    public MiniClientKeyListener(MiniClient client) {
        this.client = client;
        normalKeyListener = new BaseKeyListener(client);
        videoPausedKeyListener = new VideoPausedKeyListener(client);
        videoPlaybackKeyListener = new VideoPlaybackKeyListener(client);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (client.isVideoPaused()) {
            return videoPausedKeyListener.onKey(v, keyCode, event);
        } else if (client.isVideoPlaying()) {
            return videoPlaybackKeyListener.onKey(v, keyCode, event);
        } else {
            normalKeyListener.onKey(v, keyCode, event);
        }
        return false;
    }
}
