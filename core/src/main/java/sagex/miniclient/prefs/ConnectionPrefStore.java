package sagex.miniclient.prefs;

import java.util.Set;
import java.util.TreeSet;

import sagex.miniclient.ServerInfo;

/**
 * Created by seans on 05/03/17.
 */

public class ConnectionPrefStore implements PrefStore {
    private final PrefStore parent;
    private final String connName;

    public ConnectionPrefStore(String connName, PrefStore parent) {
        this.parent=parent;
        this.connName=connName;
    }

    public String getName() {
        return connName;
    }

    String getKey(String id) {
        return ServerInfo.getPrefKey(connName, id);
    }

    @Override
    public String getString(String key) {
        return parent.getString(getKey(key));
    }

    @Override
    public String getString(String key, String defValue) {
        return parent.getString(getKey(key), defValue);
    }

    @Override
    public void setString(String key, String value) {
        parent.setString(getKey(key), value);
    }

    @Override
    public long getLong(String key) {
        return parent.getLong(getKey(key));
    }

    @Override
    public long getLong(String key, long defValue) {
        return parent.getLong(getKey(key), defValue);
    }

    @Override
    public void setLong(String key, long value) {
        parent.setLong(getKey(key), value);
    }

    @Override
    public int getInt(String key) {
        return parent.getInt(getKey(key));
    }

    @Override
    public int getInt(String key, int defValue) {
        return parent.getInt(getKey(key), defValue);
    }

    @Override
    public void setInt(String key, int value) {
        parent.setInt(getKey(key), value);
    }

    @Override
    public double getDouble(String key) {
        return parent.getDouble(getKey(key));
    }

    @Override
    public double getDouble(String key, double defValue) {
        return parent.getDouble(getKey(key), defValue);
    }

    @Override
    public void setDouble(String key, double value) {
        parent.setDouble(getKey(key), value);
    }

    @Override
    public boolean getBoolean(String key) {
        return parent.getBoolean(getKey(key));
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return parent.getBoolean(getKey(key), defValue);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        parent.setBoolean(getKey(key), value);
    }

    @Override
    public Set<Object> keys() {
        Set<Object> myKeys = new TreeSet<Object>();
        Set<Object> keys = parent.keys();
        if (keys!=null) {
            String base = getKey("");
            for (Object o: keys) {
                if (o.toString().startsWith(base)) {
                    myKeys.add(o.toString().substring(base.length()+1));
                }
            }
        }
        return myKeys;
    }

    @Override
    public void remove(String key) {
        parent.remove(getKey(key));
    }

    @Override
    public boolean contains(String key) {
        return parent.contains(getKey(key));
    }

    @Override
    public boolean canSet(String key) {
        return true;
    }
}
