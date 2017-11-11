package sagex.miniclient.android.util;

import android.content.Context;
import android.media.AudioManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by seans on 11/11/17.
 */

public class AudioUtil {
    static Logger log = LoggerFactory.getLogger(AudioUtil.class);

    public static void requestAudioFocus(Context context) {
        try {
            log.debug("Requesting Audio Focus...");
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                }
            };

            // Request audio focus for playback
            int result = am.requestAudioFocus(focusChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                log.debug("We now have audio focus");
            } else {
                log.debug("We failed to get exlcusive audio focus.");
            }
        } catch (Throwable t) {
            log.error("Failed while requesting audio focus", t);
        }
    }

}
