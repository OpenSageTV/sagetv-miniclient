package sagex.miniclient.android.video;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.session.MediaSessionCompat;

import sagex.miniclient.MiniClient;
import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.SageCommand;
import sagex.miniclient.uibridge.EventRouter;

/**
 * This class handles the events from the Android Media Session commands and transfers them back
 * to SageTV.  This is meant to be used with all Player implementations
 */
public class MediaSessionCallbackHandler extends MediaSessionCompat.Callback
{
    private MiniPlayerPlugin player;
    private MiniClient client;
    private Context context;

    public MediaSessionCallbackHandler(MiniPlayerPlugin player, MiniClient client, Context context)
    {
        this.player = player;
        this.client = client;
        this.context = context;
    }

    @Override
    public void onCommand(String command, Bundle extras, ResultReceiver cb)
    {
        super.onCommand(command, extras, cb);
    }

    @Override
    public void onPlay()
    {
        super.onPlay();
        EventRouter.postCommand(client, SageCommand.PLAY);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        EventRouter.postCommand(client, SageCommand.PAUSE);
    }

    @Override
    public void onSkipToNext()
    {
        super.onSkipToNext();
        EventRouter.postCommand(client, SageCommand.FF);
    }

    @Override
    public void onSkipToPrevious()
    {
        super.onSkipToPrevious();
        EventRouter.postCommand(client, SageCommand.REW);
    }

    @Override
    public void onFastForward()
    {
        super.onFastForward();
        EventRouter.postCommand(client, SageCommand.FF);
    }

    @Override
    public void onRewind()
    {
        super.onRewind();
        EventRouter.postCommand(client, SageCommand.REW);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        EventRouter.postCommand(client, SageCommand.STOP);
    }

    @Override
    public void onSeekTo(long pos)
    {
        super.onSeekTo(pos);
        player.seek(pos);
    }

    @Override
    public void onSetCaptioningEnabled(boolean enabled)
    {
        super.onSetCaptioningEnabled(enabled);
        //TODO: Set first english closed caption/subtitle
    }
}
