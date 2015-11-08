package sagex.miniclient.android;

import android.app.Application;
import android.preference.PreferenceManager;

import java.io.File;

import sagex.miniclient.MiniClientOptions;
import sagex.miniclient.android.prefs.AndroidPrefStore;
import sagex.miniclient.prefs.PrefStore;

/**
 * Created by seans on 08/11/15.
 */
public class AndroidMiniClientOptions implements MiniClientOptions {
    private final AndroidPrefStore prefs;
    private final File configDir;
    private final File cacheDir;

    AndroidMiniClientOptions(Application ctx) {
        this.prefs = new AndroidPrefStore(PreferenceManager.getDefaultSharedPreferences(ctx));
        this.configDir = ctx.getFilesDir();
        this.cacheDir = ctx.getCacheDir();
    }

    @Override
    public PrefStore getPrefs() {
        return prefs;
    }

    @Override
    public File getConfigDir() {
        return configDir;
    }

    @Override
    public File getCacheDir() {
        return cacheDir;
    }
}
