package sagex.miniclient.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by seans on 21/11/15.
 */
public class StaticFieldMapper<FieldType> {
    protected final Class<?> owner;
    private final String prefix;
    private final boolean stripPrefix;
    protected Map<String, FieldType> fieldMap = new HashMap<String, FieldType>();
    protected Map<FieldType, String> fieldMapReverse = new HashMap<FieldType, String>();

    public StaticFieldMapper(Class<?> owner, String prefix, boolean stripPrefix) {
        this.owner = owner;
        this.prefix = (prefix != null ? prefix.toLowerCase() : null);
        this.stripPrefix = stripPrefix;
        mapFields(owner);
    }

    private void mapFields(Class<?> owner) {
        String name = null;
        for (Field f : owner.getDeclaredFields()) {
            if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && accept(f)) {
                try {
                    name = createName(f);
                    fieldMap.put(name, (FieldType) f.get(null));
                    fieldMapReverse.put((FieldType) f.get(null), name);
                } catch (Throwable t) {
                    System.out.println("Can't access field: " + f);
                }
            }
        }
    }

    protected String createName(Field f) {
        String val = f.getName();
        if (prefix != null && stripPrefix) {
            val = f.getName().substring(prefix.length());
        }

        while (val.startsWith("_")) {
            val = val.substring(1);
        }

        return val.toLowerCase();
    }

    protected boolean accept(Field f) {
        if (prefix != null) {
            return f.getName().toLowerCase().startsWith(prefix);
        }
        return true;
    }

    public FieldType getField(String name) {
        if (name == null) return null;
        return fieldMap.get(name.toLowerCase());
    }

    public String getFieldName(FieldType fieldId) {
        return fieldMapReverse.get(fieldId);
    }

    public Collection<String> getFieldNames() {
        return fieldMap.keySet();
    }

    public Map<String, FieldType> getFieldMap() {
        return Collections.unmodifiableMap(fieldMap);
    }
}
