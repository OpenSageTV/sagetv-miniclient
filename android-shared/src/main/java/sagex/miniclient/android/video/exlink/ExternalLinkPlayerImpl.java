package sagex.miniclient.android.video.exlink;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.IOException;

import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.android.R;
import sagex.miniclient.android.ui.AndroidUIController;
import sagex.miniclient.media.SubtitleTrack;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Rectangle;

/**
 * Created by cncb on 04/30/21.
 * Special "Player" that launches external links in other apps.
 */

public class ExternalLinkPlayerImpl implements MiniPlayerPlugin
{
    private AndroidUIController _UIController;
    private String _UrlString;
    boolean _Launched = false;

    public ExternalLinkPlayerImpl(AndroidUIController activity)
    {
        _UIController = activity;
    }

    private void Launch()
    {
        _UIController.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                String launchURL = "NA";
                String message = "";
                try
                {
                    String[] parts = _UrlString.split("\\.");
                    launchURL = Uri.decode( parts[parts.length - 2]);
                    Intent intent = null;

                    if( launchURL.startsWith("http")) // URL specified
                    {
                        Uri link = Uri.parse( launchURL);
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(link);
                        if (launchURL.contains("netflix.com")) // Special "extra" for Netflix to go directly to video
                            intent.putExtra("source", "30");
                    }
                    else  // Package specified
                    {
                        intent = _UIController.getContext().getPackageManager().getLeanbackLaunchIntentForPackage(launchURL);
                        if( intent == null)
                        {
                            intent = _UIController.getContext().getPackageManager().getLaunchIntentForPackage(launchURL);
                            if( intent == null)
                                message = String.format( _UIController.getContext().getString(R.string.exlink_app_not_found_msg1), launchURL);
                        }
                    }

                    if( intent != null)
                    {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        _UIController.getContext().startActivity(intent);
                        message = String.format( _UIController.getContext().getString(R.string.exlink_launch_msg), launchURL);
                        _Launched = true;
                    }
                }
                catch (Exception ex)
                {
                    message = String.format( _UIController.getContext().getString(R.string.exlink_app_not_found_msg2), launchURL);
                }
                finally
                {
                    Toast.makeText(_UIController.getContext(), message, Toast.LENGTH_LONG).show();
                    message += "\r\n" +  _UIController.getContext().getString(R.string.exlink_exit_msg);
                    _UIController.getCaptionsText().setText( message);
                }
            }
        });
    }

    @Override
    public void free()
    {
        _UIController.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                _UIController.getCaptionsText().setText( "");
            }
        });
    }

    @Override
    public void setPushMode(boolean b) {

    }

    @Override
    public void load(byte majorTypeHint, byte minorTypeHint, String encodingHint, String urlString, String hostname, boolean timeshifted, long bufferSize)
    {
        _UrlString = urlString;
    }

    @Override
    public long getMediaTimeMillis(long lastServerTime) {
        return 0;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public void setMute(boolean b) {}

    @Override
    public void stop() { }

    @Override
    public void pause() { }

    @Override
    public void play()
    {
        if( !_Launched)
            this.Launch();
    }

    @Override
    public void seek(long timeMS) { }

    @Override
    public void setServerEOS() { }

    @Override
    public long getLastFileReadPos() {
        return 0;
    }

    @Override
    public int getVolume() {
        return 0;
    }

    @Override
    public int setVolume(float v) {
        return 0;
    }

    @Override
    public void setAudioTrack(int streamPos) { }

    @Override
    public void setSubtitleTrack(int streamPos) { }

    @Override
    public int getSelectedSubtitleTrack() { return DISABLE_TRACK; }

    @Override
    public void setVideoRectangles(Rectangle srcRect, Rectangle destRect, boolean hideCursor) { }

    @Override
    public Dimension getVideoDimensions() {
        return null;
    }

    @Override
    public void pushData(byte[] cmddata, int bufDataOffset, int buffSize) throws IOException { }

    @Override
    public void flush() { }

    @Override
    public int getBufferLeft() {
        return 0;
    }

    @Override
    public void setVideoAdvancedAspect(String aspectMode) { }

    @Override
    public void run() { }

    @Override
    public int getSubtitleTrackCount()
    {
        return 0;
    }

    @Override
    public SubtitleTrack[] getSubtitleTracks() {
        return new SubtitleTrack[0];
    }
}
