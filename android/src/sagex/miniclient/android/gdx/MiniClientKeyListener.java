package sagex.miniclient.android.gdx;

import android.view.KeyEvent;
import android.view.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.MiniClient;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.UIRenderer;

/**
 * Created by seans on 26/09/15.
 */
public class MiniClientKeyListener implements View.OnKeyListener {
    private static final Logger log = LoggerFactory.getLogger(MiniClientKeyListener.class);

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
            // if there's a popup then just use normal keys
            if (client.getCurrentConnection().getMenuHint().popupName != null) {
                log.debug("Using Normal Key Listener");
                return normalKeyListener.onKey(v, keyCode, event);
            }

            // if we are playing a video, then check pause/play
            if (client.getCurrentConnection().getMenuHint().isOSDMenu()) {
                log.debug("Using Stateful Key Listener");
                if (client.isVideoPaused()) {
                    log.debug("Using Paused Key Listener");
                    return videoPausedKeyListener.onKey(v, keyCode, event);
                } else if (client.isVideoPlaying()) {
                    log.debug("Using Playback Key Listener");
                    return videoPlaybackKeyListener.onKey(v, keyCode, event);
                }
            }
        }

        return normalKeyListener.onKey(v, keyCode, event);
    }
}
