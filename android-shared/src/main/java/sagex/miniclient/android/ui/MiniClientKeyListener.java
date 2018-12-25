package sagex.miniclient.android.ui;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.MiniClient;
import sagex.miniclient.android.preferences.MediaMappingPreferences;
import sagex.miniclient.android.ui.keymaps.DefaultKeyMap;
import sagex.miniclient.android.ui.keymaps.GuideKeyMap;
import sagex.miniclient.android.ui.keymaps.KeyMap;
import sagex.miniclient.android.ui.keymaps.KeyMapProcessor;
import sagex.miniclient.android.ui.keymaps.PluginListKeyMap;
import sagex.miniclient.android.ui.keymaps.VideoPausedKeyMap;
import sagex.miniclient.android.ui.keymaps.VideoPlaybackKeyMap;
import sagex.miniclient.util.VerboseLogging;

/**
 * Created by seans on 26/09/15.
 */
public class MiniClientKeyListener implements View.OnKeyListener {
    private static final Logger log = LoggerFactory.getLogger(MiniClientKeyListener.class);

    private final MiniClient client;
    MediaMappingPreferences prefsVideoPlaying;
    MediaMappingPreferences prefsVideoPaused;
    MediaMappingPreferences prefs;

    KeyMapProcessor keyProcessor;

    KeyMap defaultKeyMap;
    KeyMap guideKeyMap;
    KeyMap pluginKeyMap;
    KeyMap videoPausedKeyMap;
    KeyMap videoPlaybackKeyMap;

    public MiniClientKeyListener(Context context, MiniClient client) {
        this.client = client;

        prefsVideoPaused = new MediaMappingPreferences("videopaused", client.properties());
        prefsVideoPlaying = new MediaMappingPreferences("videoplaying", client.properties());
        prefs = new MediaMappingPreferences(client.properties());

        keyProcessor = new KeyMapProcessor(client, prefs);

        defaultKeyMap = new DefaultKeyMap(null, client);
        defaultKeyMap.initializeKeyMaps();
        if (prefs.isSmartRemoteEnabled()) {
            guideKeyMap = new GuideKeyMap(defaultKeyMap);
            guideKeyMap.initializeKeyMaps();
            pluginKeyMap = new PluginListKeyMap(defaultKeyMap);
            pluginKeyMap.initializeKeyMaps();
            videoPausedKeyMap = new VideoPausedKeyMap(defaultKeyMap, prefsVideoPaused);
            videoPausedKeyMap.initializeKeyMaps();
            videoPlaybackKeyMap = new VideoPlaybackKeyMap(defaultKeyMap, prefsVideoPlaying);
            videoPlaybackKeyMap.initializeKeyMaps();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (client.getCurrentConnection()==null) return false;

        if (prefs.isSmartRemoteEnabled() && client.getCurrentConnection().getMenuHint()!=null) {
            // if there's a popup then just use normal keys
            if (client.getCurrentConnection().getMenuHint().popupName != null) {
                if (VerboseLogging.LOG_KEYS)
                    log.debug("Using Normal Key Listener for {} - {}", keyCode, event);
                return keyProcessor.onKey(defaultKeyMap, keyCode, event);
            }

            // if we are playing a video, then check pause/play
            if (client.getCurrentConnection().getMenuHint().isOSDMenuNoPopup()) {
                if (VerboseLogging.LOG_KEYS)
                    log.debug("Using Video Stateful Key Listener");
                if (client.isVideoPaused()) {
                    if (VerboseLogging.LOG_KEYS)
                        log.debug("Using Paused Key Listener for {} - {}", keyCode, event);
                    return keyProcessor.onKey(videoPausedKeyMap, keyCode, event);
                } else if (client.isVideoPlaying()) {
                    if (VerboseLogging.LOG_KEYS)
                        log.debug("Using Playback Key Listener for {} - {}", keyCode, event);
                    return keyProcessor.onKey(videoPlaybackKeyMap, keyCode, event);
                }
            }

            if (client.getCurrentConnection().getMenuHint().isPluginMenu()) {
                if (VerboseLogging.LOG_KEYS)
                    log.debug("Using Plugin Key Listener for {} - {}", keyCode, event);
                return keyProcessor.onKey(pluginKeyMap, keyCode, event);
            } else if (client.getCurrentConnection().getMenuHint().isGuideMenu()) {
                if (VerboseLogging.LOG_KEYS)
                    log.debug("Using Guide Key Listener for {} = {}", keyCode, event);
                return keyProcessor.onKey(guideKeyMap, keyCode, event);
            }
        }

        if (VerboseLogging.LOG_KEYS)
            log.debug("Using Default Normal Key Listener. Menu Hint was {}, Key Event was {}", client.getCurrentConnection().getMenuHint(), event);
        return keyProcessor.onKey(defaultKeyMap, keyCode, event);
    }
}
