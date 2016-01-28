package sagex.miniclient.events;

/**
 * Created by seans on 27/01/16.
 */
public class ConnectionLost {
    public final boolean reconnecting;

    public ConnectionLost(boolean willReconnect) {
        this.reconnecting = false;
    }
}
