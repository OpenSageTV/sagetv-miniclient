package sagex.miniclient;

import java.io.File;
import java.util.List;
import java.util.Properties;

import sagex.miniclient.prefs.PrefStore;

/**
 * Created by seans on 08/11/15.
 */
public interface MiniClientOptions {
    /**
     * Application level preferences
     * @return
     */
    PrefStore getPrefs();

    File getConfigDir();

    File getCacheDir();

    IBus getBus();

    /**
     * Allows the Hardware device to add/remove codec support based on the hardware supported codecs/containers
     *  @param videoCodecs
     * @param audioCodecs
     * @param pushFormats
     * @param pullFormats
     */
    void prepareCodecs(List<String> videoCodecs, List<String> audioCodecs, List<String> pushFormats, List<String> pullFormats);

    public boolean isTouchUI();
    public boolean isTVUI();
    public boolean isDesktopUI();
    public boolean isUsingAdvancedAspectModes();
    public String getAdvancedApectModes();
    public String getDefaultAdvancedAspectMode();
}
