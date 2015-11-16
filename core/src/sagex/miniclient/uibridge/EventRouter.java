package sagex.miniclient.uibridge;

import java.util.HashMap;
import java.util.Map;

import sagex.miniclient.MiniClient;

/**
 * Created by seans on 31/10/15.
 */
public class EventRouter {
    // see EventRouter and UserEvent in SageTV Sources

    // media keys
    public static final SageTVKey MEDIA_PAUSE = new SageTVKey(Keys.VK_S, Keys.CTRL_MASK);
    public static final SageTVKey MEDIA_PLAY = new SageTVKey(Keys.VK_D, Keys.CTRL_MASK);
    public static final SageTVKey MEDIA_PLAY_PAUSE = new SageTVKey(Keys.VK_S, Keys.CTRL_MASK | Keys.SHIFT_MASK);
    public static final SageTVKey MEDIA_STOP = new SageTVKey(Keys.VK_G, Keys.CTRL_MASK);
    public static final SageTVKey MEDIA_FF = new SageTVKey(Keys.VK_F, Keys.CTRL_MASK);
    public static final SageTVKey MEDIA_REW = new SageTVKey(Keys.VK_A, Keys.CTRL_MASK);

    // navigation keys
    public static final SageTVKey UP = new SageTVKey(Keys.VK_UP);
    public static final SageTVKey DOWN = new SageTVKey(Keys.VK_DOWN);
    public static final SageTVKey LEFT = new SageTVKey(Keys.VK_LEFT);
    public static final SageTVKey RIGHT = new SageTVKey(Keys.VK_RIGHT);
    public static final SageTVKey ENTER = new SageTVKey(Keys.VK_ENTER);
    public static final SageTVKey DELETE = new SageTVKey(Keys.VK_DELETE);
    public static final SageTVKey BACKSPACE = new SageTVKey(Keys.VK_BACK_SPACE);
    public static final SageTVKey SELECT = ENTER;
    public static final SageTVKey BACK = new SageTVKey(Keys.VK_LEFT, Keys.ALT_MASK);

    public static final SageTVKey WATCHED = new SageTVKey(Keys.VK_W, Keys.CTRL_MASK);
    public static final SageTVKey GUIDE = new SageTVKey(Keys.VK_X, Keys.CTRL_MASK);
    public static final SageTVKey POWER = new SageTVKey(Keys.VK_Z, Keys.CTRL_MASK);
    public static final SageTVKey INFO = new SageTVKey(Keys.VK_I, Keys.CTRL_MASK);
    public static final SageTVKey HOME = new SageTVKey(Keys.VK_HOME);
    public static final SageTVKey OPTIONS = new SageTVKey(Keys.VK_O, Keys.CTRL_MASK);
    public static final SageTVKey ESCAPE = OPTIONS;

    public static final SageTVKey SPACE = new SageTVKey(Keys.VK_SPACE); // o === space??

    // these are NATIVE key to SageTV key maps.  Initally Empty, so, each Native
    // client needs to set these on startup
    public static final Map<Object, SageTVKey> NATIVE_UI_KEYMAP = new HashMap<Object, SageTVKey>();
    public static final Map<Object, SageTVKey> NATIVE_UI_LONGPRESS_KEYMAP = new HashMap<Object, SageTVKey>();

    /**
     * Post the SageTVKey event to the server using the given MiniClient connection
     *
     * @param client
     * @param key
     */
    public static void post(MiniClient client, SageTVKey key) {
        client.getCurrentConnection().postKeyEvent(key.keyCode, key.modifiers, key.keyChar);
    }
}
