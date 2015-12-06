package sagex.miniclient;

import java.io.File;

import sagex.miniclient.prefs.PrefStore;

/**
 * Created by seans on 08/11/15.
 */
public interface MiniClientOptions {
    PrefStore getPrefs();

    File getConfigDir();

    File getCacheDir();

    IBus getBus();
}
