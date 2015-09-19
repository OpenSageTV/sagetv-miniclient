package sagex.miniclient;

/**
 * Simple holder pattern that holds a value type
 * @author seans
 *
 * @param <T>
 */
public class Holder<T> {
	T value;
	
	public Holder() {
	}
	
	public Holder(T val) {
		this.value=val;
	}

	public T get() {
		return value;
	}
	
	public void set(T value) {
		this.value=value;
	}
	public void force(Object value) {
		this.value=(T)value;
	}
}
