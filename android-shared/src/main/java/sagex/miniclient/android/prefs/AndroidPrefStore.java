package sagex.miniclient.android.prefs;

import android.content.SharedPreferences;

import java.util.Set;
import java.util.TreeSet;

import sagex.miniclient.prefs.PrefStore;

/**
 * Created by seans on 08/11/15.
 */
public class AndroidPrefStore implements PrefStore {
    private final SharedPreferences prefs;

    public AndroidPrefStore(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

    @Override
    public String getString(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    @Override
    public void setString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    @Override
    public long getLong(String key) {
        return getLong(key, 0);
    }

    @Override
    public long getLong(String key, long defValue) {
        try {
            return prefs.getLong(key, defValue);
        } catch (ClassCastException cce) {
        }
        return Long.parseLong(prefs.getString(key, String.valueOf(defValue)));
    }

    @Override
    public void setLong(String key, long value) {
        prefs.edit().putLong(key, value).apply();
    }

    @Override
    public int getInt(String key) {
        return getInt(key, 0);
    }

    @Override
    public int getInt(String key, int defValue) {
        try {
            return prefs.getInt(key, defValue);
        } catch (ClassCastException cce) {
        }
        return Integer.parseInt(prefs.getString(key, String.valueOf(defValue)));
    }

    @Override
    public void setInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    @Override
    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    @Override
    public double getDouble(String key, double defValue) {
        return prefs.getFloat(key, (float) defValue);
    }

    @Override
    public void setDouble(String key, double value) {
        prefs.edit().putFloat(key, (float) value).commit();
    }

    @Override
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).commit();
    }

    @Override
    public Set<Object> keys() {
        return new TreeSet<Object>(prefs.getAll().keySet());
    }

    @Override
    public void remove(String key) {
        prefs.edit().remove(key).commit();
    }

    @Override
    public boolean contains(String key) {
        return prefs.contains(key);
    }

    @Override
    public boolean canSet(String key) {
        return true;
    }
}
