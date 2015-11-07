package sagex.miniclient.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by seans on 24/10/15.
 */
public class Prefs {
    private static final Logger log = LoggerFactory.getLogger(Prefs.class);
    private final SharedPreferences prefs;

    public Prefs(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public static boolean getBoolean(Activity context, String key, boolean defVal) {
        return MiniclientApplication.get(context).getPrefs().getBoolean(key, defVal);
    }

    public static String getString(Activity context, String key, String defVal) {
        return MiniclientApplication.get(context).getPrefs().getString(key, defVal);
    }

    public SharedPreferences prefs() {
        return prefs;
    }

    public void setBoolean(String key, boolean val) {
        prefs.edit().putBoolean(key, val).apply();
    }

    public void setString(String key, String val) {
        prefs.edit().putString(key, val).apply();
    }

    public boolean getBoolean(String key, boolean defVal) {
        return prefs.getBoolean(key, defVal);
    }

    public String getString(String key, String defVal) {
        return prefs.getString(key, defVal);
    }

    public void setEnabled(PreferenceFragment frag, String key, boolean val) {
        try {
            frag.findPreference(key).setEnabled(val);
        } catch (Throwable t) {
            log.warn("Invalid Preference Key: {}", key);
        }
    }

    public static class Key {
        public static final String use_hardware_acceleration = "use_hardware_acceleration";
        public static final String use_log_to_sdcard = "use_log_to_sdcard";
        public static final String log_level = "log_level";
    }

}
