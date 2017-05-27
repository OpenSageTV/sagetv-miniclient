package sagex.miniclient.android.gdx;

import android.view.KeyEvent;
import android.view.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.MiniClient;
import sagex.miniclient.prefs.PrefStore;

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
        boolean useRemote = (client.getConnectedServerInfo().use_stateful_remote!=null)?client.getConnectedServerInfo().use_stateful_remote:client.properties().getBoolean(PrefStore.Keys.use_stateful_remote, true);

        if (useRemote) {
            // if there's a popup then just use normal keys
            if (client.getCurrentConnection().getMenuHint().popupName != null) {
                log.debug("Using Normal Key Listener");
                return normalKeyListener.onKey(v, keyCode, event);
            }

            // if we are playing a video, then check pause/play
            if (client.getCurrentConnection().getMenuHint().isOSDMenuNoPopup()) {
                log.debug("Using Stateful Key Listener");
                if (client.isVideoPaused()) {
                    log.debug("Using Paused Key Listener");
                    return videoPausedKeyListener.onKey(v, keyCode, event);
                } else if (client.isVideoPlaying()) {
                    log.debug("Using Playback Key Listener");
                    return videoPlaybackKeyListener.onKey(v, keyCode, event);
                }
            }
            if (client.isVideoVisible()) {
                log.debug("Using Default Normal Key Listener. MenuPlayerState: {}, Menu Hint was {}, Key Event was {}", client.getCurrentConnection().getMediaCmd().getPlaya().getState(), client.getCurrentConnection().getMenuHint(), event);
            } else {
                log.debug("Using Default Normal Key Listener. (No Player Visible). Menu Hint was {}, Key Event was {}", client.getCurrentConnection().getMenuHint(), event);
            }
        }

        return normalKeyListener.onKey(v, keyCode, event);
    }
}
