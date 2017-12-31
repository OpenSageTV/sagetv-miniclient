package sagex.miniclient.desktop;

import sagex.miniclient.IBus;
import sagex.miniclient.MiniClientOptions;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.prefs.PropertiesPrefStore;

import java.io.File;
import java.util.List;
import java.util.Properties;

public class DesktopClientOptions implements MiniClientOptions {
    File home = null;
    PrefStore prefStore = null;
    File cacheDir;

    public File getHome() {
        if (home==null) {
            home = new File(new File(System.getProperty("user.dir")), ".sagetvminiclient");
            home.mkdirs();
        }
        return home;
    }

    public File getResource(String resName) {
        File f = new File(getHome(), resName);
        if (!f.exists()) {
            if (f.getParentFile()!=null) {
                f.getParentFile().mkdirs();
            }
        }
        return f;
    }

    @Override
    public PrefStore getPrefs() {
        if (prefStore==null) {
            prefStore = new PropertiesPrefStore(new Properties(), getResource("client.properties"));
            if (prefStore.getLong(PrefStore.Keys.image_cache_size_mb, 64)==64) {
                prefStore.setLong(PrefStore.Keys.image_cache_size_mb, 1024);
            }
            if (prefStore.getLong(PrefStore.Keys.disk_image_cache_size_mb, 512) == 512) {
                prefStore.setLong(PrefStore.Keys.disk_image_cache_size_mb, 10240);
            }
        }
        return prefStore;
    }

    @Override
    public File getConfigDir() {
        return getHome();
    }

    @Override
    public File getCacheDir() {
        if (cacheDir == null) {
            cacheDir = getResource("cache");
            if (!cacheDir.exists()) cacheDir.mkdirs();
        }
        return cacheDir;
    }

    @Override
    public IBus getBus() {
        return new DesktopBus();
    }

    @Override
    public void prepareCodecs(List<String> videoCodecs, List<String> audioCodecs, List<String> pushFormats, List<String> pullFormats, Properties codecs) {

    }

    @Override
    public boolean isTouchUI() {
        return false;
    }

    @Override
    public boolean isTVUI() {
        return false;
    }

    @Override
    public boolean isDesktopUI() {
        return true;
    }

    @Override
    public boolean isUsingAdvancedAspectModes() {
        return false;
    }

    @Override
    public String getAdvancedApectModes() {
        return null;
    }

    @Override
    public String getDefaultAdvancedAspectMode() {
        return null;
    }
}
