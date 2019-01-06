package sagex.miniclient.uibridge;

import sagex.miniclient.MiniClient;
import sagex.miniclient.SageCommand;
import sagex.miniclient.events.DebugSageCommandEvent;
import sagex.miniclient.prefs.PrefStore;


public class EventRouter
{

    /*
    public static final UserEvent MEDIA_PAUSE = new UserEvent(UserEvent.PAUSE);
    public static final UserEvent MEDIA_PLAY = new UserEvent(UserEvent.PLAY);
    public static final UserEvent MEDIA_PLAY_PAUSE = new UserEvent(UserEvent.PLAY_PAUSE);
    public static final UserEvent MEDIA_STOP = new UserEvent(UserEvent.STOP);
    public static final UserEvent MEDIA_FF = new UserEvent(UserEvent.SMOOTH_FF);
    public static final UserEvent MEDIA_REW = new UserEvent(UserEvent.SMOOTH_REW);
    public static final UserEvent MEDIA_FF_2 = new UserEvent(UserEvent.FF);
    public static final UserEvent MEDIA_REW_2 = new UserEvent(UserEvent.REW);
    public static final UserEvent MEDIA_NEXT = new UserEvent(UserEvent.FF_2);
    public static final UserEvent MEDIA_PREVIOUS = new UserEvent(UserEvent.REW_2);



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
    public static final UserEvent PAGE_LEFT = new UserEvent(UserEvent.PAGE_LEFT);
    public static final UserEvent PAGE_RIGHT = new UserEvent(UserEvent.PAGE_RIGHT);
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

    public static final UserEvent F1 = new UserEvent(-1, -1, -1, Keys.VK_F1, 0, (char)Keys.VK_F1);
    public static final UserEvent F2 = new UserEvent(-1, -1, -1, Keys.VK_F2, 0, (char)Keys.VK_F2);
    public static final UserEvent F3 = new UserEvent(-1, -1, -1, Keys.VK_F3, 0, (char)Keys.VK_F3);
    public static final UserEvent F4 = new UserEvent(-1, -1, -1, Keys.VK_F4, 0, (char)Keys.VK_F4);
    public static final UserEvent F5 = new UserEvent(-1, -1, -1, Keys.VK_F5, 0, (char)Keys.VK_F5);
    public static final UserEvent F6 = new UserEvent(-1, -1, -1, Keys.VK_F6, 0, (char)Keys.VK_F6);
    public static final UserEvent F7 = new UserEvent(-1, -1, -1, Keys.VK_F7, 0, (char)Keys.VK_F7);
    public static final UserEvent F8 = new UserEvent(-1, -1, -1, Keys.VK_F8, 0, (char)Keys.VK_F8);
    public static final UserEvent F9 = new UserEvent(-1, -1, -1, Keys.VK_F9, 0, (char)Keys.VK_F9);
    public static final UserEvent F10 = new UserEvent(-1, -1, -1, Keys.VK_F10, 0, (char)Keys.VK_F10);
    public static final UserEvent F11 = new UserEvent(-1, -1, -1, Keys.VK_F11, 0, (char)Keys.VK_F11);
    public static final UserEvent F12 = new UserEvent(-1, -1, -1, Keys.VK_F12, 0, (char)Keys.VK_F12);
    public static final UserEvent AR_TOGGLE = new UserEvent(UserEvent.AR_TOGGLE);
    */

    /*
    public static void post(MiniClient client, UserEvent event)
    {
        if (client == null || client.getCurrentConnection() == null || event == null)
        {
            return;
        }

        if (event.isKB())
        {
            client.getCurrentConnection().postKeyEvent(event.getKeyCode(), event.getKeyModifiers(), event.getKeyChar());
        }
        else
        {
            client.getCurrentConnection().postSageCommandEvent(event.getCommand().getEventCode());
        }
    }
    */

    public static void postCommand(MiniClient client, int command)
    {
        if (client.properties().getBoolean(PrefStore.Keys.debug_sage_commands, false)) {
            client.eventbus().post(new DebugSageCommandEvent(SageCommand.parseByID(command)));
        }

        client.getCurrentConnection().postSageCommandEvent(command);
    }

    public static void postCommand(MiniClient client, SageCommand command)
    {
        if (client.properties().getBoolean(PrefStore.Keys.debug_sage_commands, false)) {
            client.eventbus().post(new DebugSageCommandEvent(command));
        }

        if(command != SageCommand.UNKNOWN && command != SageCommand.NONE)
        {
            client.getCurrentConnection().postSageCommandEvent(command.getEventCode());
        }
    }
}
