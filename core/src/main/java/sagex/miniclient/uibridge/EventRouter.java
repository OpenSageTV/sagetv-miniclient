package sagex.miniclient.uibridge;

import sagex.miniclient.MiniClient;
import sagex.miniclient.UserEvent;

/**
 * Created by seans on 31/10/15.
 */
public class EventRouter {
    // see EventRouter and UserEvent in SageTV Sources

    // media keys
    public static final UserEvent MEDIA_PAUSE = new UserEvent(UserEvent.PAUSE);
    public static final UserEvent MEDIA_PLAY = new UserEvent(UserEvent.PLAY);
    public static final UserEvent MEDIA_PLAY_PAUSE = new UserEvent(UserEvent.PLAY_PAUSE);
    public static final UserEvent MEDIA_STOP = new UserEvent(UserEvent.STOP);
    public static final UserEvent MEDIA_FF = new UserEvent(UserEvent.FF);
    public static final UserEvent MEDIA_REW = new UserEvent(UserEvent.REW);

    public static final UserEvent VOLUME_UP = new UserEvent(UserEvent.VOLUME_UP);
    public static final UserEvent VOLUME_DOWN = new UserEvent(UserEvent.VOLUME_DOWN);
    public static final UserEvent VOLUME_MUTE = new UserEvent(UserEvent.MUTE);

    // navigation keys
    public static final UserEvent UP = new UserEvent(UserEvent.UP);
    public static final UserEvent DOWN = new UserEvent(UserEvent.DOWN);
    public static final UserEvent LEFT = new UserEvent(UserEvent.LEFT);
    public static final UserEvent RIGHT = new UserEvent(UserEvent.RIGHT);
    public static final UserEvent ENTER = new UserEvent(UserEvent.SELECT);
    public static final UserEvent DELETE = new UserEvent(UserEvent.DELETE);
    public static final UserEvent SELECT = ENTER;
    public static final UserEvent BACK = new UserEvent(UserEvent.BACK);
    public static final UserEvent FORWORAD = new UserEvent(UserEvent.FORWARD);
    public static final UserEvent BACKSPACE = new UserEvent(-1, -1, -1, Keys.VK_BACK_SPACE, 0, '\b');

    public static final UserEvent WATCHED = new UserEvent(UserEvent.WATCHED);
    public static final UserEvent GUIDE = new UserEvent(UserEvent.GUIDE);
    public static final UserEvent POWER = new UserEvent(UserEvent.POWER);
    public static final UserEvent INFO = new UserEvent(UserEvent.INFO);
    public static final UserEvent HOME = new UserEvent(UserEvent.HOME);
    public static final UserEvent PAGE_UP = new UserEvent(UserEvent.PAGE_UP);
    public static final UserEvent PAGE_DOWN = new UserEvent(UserEvent.PAGE_DOWN);
    public static final UserEvent OPTIONS = new UserEvent(UserEvent.OPTIONS);

    public static final UserEvent NUM_0 = new UserEvent(-1, -1, -1, Keys.VK_0, 0, '0');
    public static final UserEvent NUM_1 = new UserEvent(-1, -1, -1, Keys.VK_1, 0, '1');
    public static final UserEvent NUM_2 = new UserEvent(-1, -1, -1, Keys.VK_2, 0, '2');
    public static final UserEvent NUM_3 = new UserEvent(-1, -1, -1, Keys.VK_3, 0, '3');
    public static final UserEvent NUM_4 = new UserEvent(-1, -1, -1, Keys.VK_4, 0, '4');
    public static final UserEvent NUM_5 = new UserEvent(-1, -1, -1, Keys.VK_5, 0, '5');
    public static final UserEvent NUM_6 = new UserEvent(-1, -1, -1, Keys.VK_6, 0, '6');
    public static final UserEvent NUM_7 = new UserEvent(-1, -1, -1, Keys.VK_7, 0, '7');
    public static final UserEvent NUM_8 = new UserEvent(-1, -1, -1, Keys.VK_8, 0, '8');
    public static final UserEvent NUM_9 = new UserEvent(-1, -1, -1, Keys.VK_9, 0, '9');

    /**
     * Post the UserEvent event to the server using the given MiniClient connection
     *
     * @param client
     * @param event
     */
    public static void post(MiniClient client, UserEvent event) {
        if (client == null || client.getCurrentConnection() == null || event == null) return;

        if (event.isKB()) {
            client.getCurrentConnection().postKeyEvent(event.getKeyCode(), event.getKeyModifiers(), event.getKeyChar());
        } else if (event.isIR()) {
            //client.getCurrentConnection().postIREvent(event.getIRCode());
        } else {
            client.getCurrentConnection().postSageCommandEvent(event.getType());
        }
    }

    public static void postCommand(MiniClient client, int command) {
        client.getCurrentConnection().postSageCommandEvent(command);
    }
}
