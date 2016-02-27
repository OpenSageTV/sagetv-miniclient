package sagex.miniclient.android.events;

/**
 * Created by seans on 23/01/16.
 */
public class MessageEvent {
    private final String message;

    public MessageEvent(String msg) {
        this.message = msg;
    }

    public String getMessage() {
        return message;
    }
}
