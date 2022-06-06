package sagex.miniclient.uibridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.MiniClient;
import sagex.miniclient.SageCommand;
import sagex.miniclient.events.DebugSageCommandEvent;
import sagex.miniclient.events.ShowKeyboardEvent;
import sagex.miniclient.events.ShowNavigationEvent;
import sagex.miniclient.prefs.PrefStore;


public class EventRouter
{
    public static final Logger log = LoggerFactory.getLogger(EventRouter.class);

    public static void postCommand(MiniClient client, int command)
    {
        log.debug("Post Command Called:  " + command);

        //If the player is already paused, bypass sending command to SageTV, and allow the player to handle
        if (client.isVideoPaused() && SageCommand.parseByID(command) == SageCommand.PAUSE)
        {
            log.debug("Override pause to advance frame");
            client.getPlayer().pause();
            return;
        }
        if ((client.isVideoPaused() || client.isVideoPlaying()) && SageCommand.parseByID(command) == SageCommand.STOP)
        {
            log.debug("Telling active player to stop playback");
            client.getPlayer().stop();
        }

        if (client.properties().getBoolean(PrefStore.Keys.debug_sage_commands, false))
        {
            client.eventbus().post(new DebugSageCommandEvent(SageCommand.parseByID(command)));
        }

        client.getCurrentConnection().postSageCommandEvent(command);
    }

    public static void postCommand(MiniClient client, SageCommand command)
    {
        log.debug("Post SageCommandCalled: " + command.getDisplayName() + " Key:" + command.getKey() + " EventCode:" + command.getEventCode());

        //If the player is already paused, bypass sending command to SageTV, and allow the player to handle
        if (client.isVideoPaused() && command == SageCommand.PAUSE)
        {
            log.debug("Override pause to advance frame");
            client.getPlayer().pause();
            return;
        }

        if ((client.isVideoPaused() || client.isVideoPlaying()) && command == SageCommand.STOP)
        {
            log.debug("Telling active player to stop playback");
            client.getPlayer().stop();
        }

        if (client.properties().getBoolean(PrefStore.Keys.debug_sage_commands, false))
        {
            client.eventbus().post(new DebugSageCommandEvent(command));
        }

        if (command.getEventCode() >= 0)
        {
            client.getCurrentConnection().postSageCommandEvent(command.getEventCode());
        }
        else
        {
            if (command == SageCommand.NAV_OSD)
            {
                client.eventbus().post(ShowNavigationEvent.INSTANCE);
            }
            else if (command == SageCommand.KEYBOARD_OSD)
            {
                client.eventbus().post(ShowKeyboardEvent.INSTANCE);
            }
            else
            {
                log.warn("Unhandled SageCommand: {}", command);
            }
        }
    }
}
