package sagex.miniclient.prefs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * Created by seans on 08/11/15.
 */
public class PropertiesPrefStore implements PrefStore {
    private static final String PLACESHIFTER_PROPERTIES = "SageTVPlaceshifter.properties";

    private static final Logger log = LoggerFactory.getLogger(PropertiesPrefStore.class);

    private final Properties props;
    private final File propFile;

    public PropertiesPrefStore(File configDir) {
        this.props = new Properties();
        this.propFile = new java.io.File(configDir, PLACESHIFTER_PROPERTIES);
        load();
    }

    public PropertiesPrefStore(Properties props, File propFile) {
        this.props = props;
        this.propFile = propFile;
        if (props.size()==0)
            load();
    }

    void load() {
        // If the properties file is in the working directory; then use that one
        // and save it back there. Otherwise
        // use the one in the user's home directory
        if (propFile.isFile()) {
            java.io.InputStream is = null;
            try {
                is = new java.io.FileInputStream(propFile);
                props.load(is);
            } catch (java.io.IOException e) {
                log.error("Failed to load {}", propFile, e);
            } finally {
                try {
                    if (is != null)
                        is.close();
                } catch (Exception e) {
                }
                is = null;
            }
        }
    }

    void save() {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(propFile);
            props.store(fos, "MiniClient Properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                } catch ( IOException e) {}
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

    @Override
    public String getString(String key, String defValue) {
        return props.getProperty(key, defValue);
    }

    @Override
    public void setString(String key, String value) {
        props.setProperty(key, value);
        save();
    }

    @Override
    public long getLong(String key) {
        return getLong(key, 0);
    }

    @Override
    public long getLong(String key, long defValue) {
        String val = getString(key, String.valueOf(defValue));
        return Long.parseLong(val);
    }

    @Override
    public void setLong(String key, long value) {
        props.setProperty(key, String.valueOf(value));
        save();
    }

    @Override
    public int getInt(String key) {
        return getInt(key, 0);
    }

    @Override
    public int getInt(String key, int defValue) {
        return (int) getLong(key, defValue);
    }

    @Override
    public void setInt(String key, int value) {
        setLong(key, value);
    }

    @Override
    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    @Override
    public double getDouble(String key, double defValue) {
        String val = getString(key, String.valueOf(defValue));
        return Double.parseDouble(val);
    }

    @Override
    public void setDouble(String key, double value) {
        props.setProperty(key, String.valueOf(value));
        save();
    }

    @Override
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        String val = getString(key, String.valueOf(defValue));
        return val != null && (val.equalsIgnoreCase("true") || val.equals("1"));
    }

    @Override
    public void setBoolean(String key, boolean value) {
        props.setProperty(key, String.valueOf(value));
        save();
    }

    @Override
    public Set<Object> keys() {
        return props.keySet();
    }

    @Override
    public void remove(String key) {
        props.remove(key);
    }

    @Override
    public boolean contains(String key) {
        return props.contains(key);
    }

    @Override
    public boolean canSet(String key) {
        return true;
    }
}
