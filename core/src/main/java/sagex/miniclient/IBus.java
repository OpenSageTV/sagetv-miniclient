package sagex.miniclient;

/**
 * Inteface to allow OTTO like bus in the MiniClient.  We'll use Otto on android.
 * <p/>
 * Created by seans on 05/12/15.
 */
public interface IBus {
    void register(Object object);

    void unregister(Object object);

    void post(Object event);
}
