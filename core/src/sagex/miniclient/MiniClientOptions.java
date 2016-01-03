package sagex.miniclient;

import java.io.File;
import java.util.List;

import sagex.miniclient.prefs.PrefStore;

/**
 * Created by seans on 08/11/15.
 */
public interface MiniClientOptions {
    PrefStore getPrefs();

    File getConfigDir();

    File getCacheDir();

    IBus getBus();

    /**
     * Allows the Hardware device to add/remove codec support based on the hardware supported codecs/containers
     *
     * @param videoCodecs
     * @param audioCodecs
     * @param pushFormats
     * @param pullFormats
     */
    void prepareCodecs(List<String> videoCodecs, List<String> audioCodecs, List<String> pushFormats, List<String> pullFormats);
}
