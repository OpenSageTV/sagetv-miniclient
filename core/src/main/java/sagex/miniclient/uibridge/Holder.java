package sagex.miniclient.uibridge;

/**
 * Simple holder pattern that holds a value type
 *
 * @param <T>
 * @author seans
 */
public class Holder<T> {
    T value;

    public Holder() {
    }

    public Holder(T val) {
        this.value = val;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public void force(Object value) {
        this.value = (T) value;
    }
}
