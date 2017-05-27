package sagex.miniclient.prefs;

import java.util.Set;

/**
 * Created by seans on 05/03/17.
 */

public class COWPrefStore implements PrefStore {
    private final PrefStore roStore;
    private final PrefStore rwStore;

    public COWPrefStore(PrefStore roStore, PrefStore rwStore) {
        this.roStore=roStore;
        this.rwStore=rwStore;
    }
    @Override
    public String getString(String key) {
        return rwStore.getString(key, roStore.getString(key));
    }

    @Override
    public String getString(String key, String defValue) {
        return rwStore.getString(key, roStore.getString(key, defValue));
    }

    @Override
    public void setString(String key, String value) {
        if (rwStore.canSet(key))
            rwStore.setString(key, value);
        else
            roStore.setString(key, value);
    }

    @Override
    public long getLong(String key) {
        return rwStore.getLong(key, roStore.getLong(key));
    }

    @Override
    public long getLong(String key, long defValue) {
        return rwStore.getLong(key, roStore.getLong(key, defValue));
    }

    @Override
    public void setLong(String key, long value) {
        if (rwStore.canSet(key))
            rwStore.setLong(key, value);
        else
            roStore.setLong(key, value);
    }

    @Override
    public int getInt(String key) {
        return rwStore.getInt(key, roStore.getInt(key));
    }

    @Override
    public int getInt(String key, int defValue) {
        return rwStore.getInt(key, roStore.getInt(key, defValue));
    }

    @Override
    public void setInt(String key, int value) {
        if (rwStore.canSet(key))
            rwStore.setInt(key, value);
        else
            roStore.setInt(key, value);
    }

    @Override
    public double getDouble(String key) {
        return rwStore.getDouble(key, roStore.getDouble(key));
    }

    @Override
    public double getDouble(String key, double defValue) {
        return rwStore.getDouble(key, roStore.getDouble(key, defValue));
    }

    @Override
    public void setDouble(String key, double value) {
        if (rwStore.canSet(key))
            rwStore.setDouble(key, value);
        else
            roStore.setDouble(key, value);
    }

    @Override
    public boolean getBoolean(String key) {
        return rwStore.getBoolean(key, roStore.getBoolean(key));
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return rwStore.getBoolean(key, roStore.getBoolean(key, defValue));
    }

    @Override
    public void setBoolean(String key, boolean value) {
        if (rwStore.canSet(key))
            rwStore.setBoolean(key, value);
        else
            roStore.setBoolean(key, value);
    }

    @Override
    public Set<Object> keys() {
        return roStore.keys();
    }

    @Override
    public void remove(String key) {
        if (rwStore.canSet(key) && rwStore.contains(key))
            rwStore.remove(key);
        else
            roStore.remove(key);
    }

    @Override
    public boolean contains(String key) {
        return roStore.contains(key) || rwStore.contains(key);
    }

    @Override
    public boolean canSet(String key) {
        return rwStore.canSet(key) || roStore.canSet(key);
    }
}
