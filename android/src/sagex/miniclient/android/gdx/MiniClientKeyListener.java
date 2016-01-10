package sagex.miniclient.android.gdx;

import android.view.KeyEvent;
import android.view.View;

import sagex.miniclient.MiniClient;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.UIRenderer;

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
        if (client.properties().getBoolean(PrefStore.Keys.use_stateful_remote, true)) {
//            if (client.getUIRenderer() != null && client.getUIRenderer().getState() == UIRenderer.STATE_MENU) {
            // TODO: do better checking to make sure we are not showing dialog over video or OSD
            if (client.isVideoPaused()) {
                return videoPausedKeyListener.onKey(v, keyCode, event);
            } else if (client.isVideoPlaying()) {
                return videoPlaybackKeyListener.onKey(v, keyCode, event);
            }
//            } else {
//                if (client.isVideoPaused()) {
//                    return videoPausedKeyListener.onKey(v, keyCode, event);
//                } else {
//                    return videoPlaybackKeyListener.onKey(v, keyCode, event);
//                }
//            }
        }
        return normalKeyListener.onKey(v, keyCode, event);
    }
}
