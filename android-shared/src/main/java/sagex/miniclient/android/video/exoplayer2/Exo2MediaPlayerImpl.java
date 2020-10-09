package sagex.miniclient.android.video.exoplayer2;

import android.net.Uri;
import android.os.Handler;
import android.view.SurfaceView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegLibrary;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.video.VideoListener;
import java.util.List;
import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.ui.AndroidUIController;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.util.VerboseLogging;

import static sagex.miniclient.util.Utils.toHHMMSS;

/**
 * Created by seans on 24/09/16.
 */

public class Exo2MediaPlayerImpl extends BaseMediaPlayerImpl<SimpleExoPlayer, DataSource>
{
    MediaSource mediaSource;
    long playbackStartPosition = -1;
    int initialAudioTrackIndex = -1;
    long currentPlaybackPosition = 0;
    DefaultTrackSelector trackSelector;

    boolean showCaptions = false;
    Handler handler;
    Runnable progressRunnable;
    String url;

    public Exo2MediaPlayerImpl(AndroidUIController activity)
    {
        super(activity, true, false);

    }

    boolean ExoIsPlaying()
    {
        if (player == null)
        {
            return false;
        }
        return player.getPlayWhenReady();
    }

    void ExoPause()
    {
        if (player == null)
        {
            return;
        }

        player.setPlayWhenReady(false);
    }

    void ExoStart()
    {
        if (player == null)
        {
            return;
        }

        player.setPlayWhenReady(true);
    }

    protected void releasePlayer()
    {
        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (player != null)
                {
                    try
                    {
                        if (ExoIsPlaying())
                        {
                            ExoPause();
                        }
                    }
                    catch (Throwable t) { }

                    try
                    {
                        player.release();
                    }
                    catch (Throwable t) { }

                    player = null;
                    Exo2MediaPlayerImpl.super.releasePlayer();
                }
            }
        });
    }

    @Override
    public Dimension getVideoDimensions()
    {
        if (VerboseLogging.DETAILED_PLAYER_LOGGING)
        {
            log.debug("getVideoDimensions");
        }
        if (player != null)
        {
            if (player.getVideoFormat() != null)
            {
                Dimension d = new Dimension(player.getVideoFormat().width, player.getVideoFormat().height);
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                {
                    log.debug("getVideoSize(): {}", d);
                }
                return d;
            }
            else
            {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                {
                    log.debug("getVideoDimensions: player.getFormat is null");
                }
            }
        }
        else
        {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
            {
                log.debug("getVideoDimensions: player is null");
            }
        }
        return null;
    }

    @Override
    public long getPlayerMediaTimeMillis(long lastServerTime)
    {
        log.debug("Returning playback time: " + (lastServerTime + this.currentPlaybackPosition));
        log.debug("\tLast server time: " + lastServerTime);
        log.debug("\tExoPlayerCurrentPlayback Position: " + this.currentPlaybackPosition);
        return lastServerTime + this.currentPlaybackPosition;
    }

    @Override
    public void stop()
    {
        super.stop();

        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (playerReady)
                {
                    if (player == null)
                    {
                        return;
                    }
                    player.setPlayWhenReady(false);
                }
            }
        });
    }

    @Override
    public void pause()
    {
        super.pause();

        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (playerReady)
                {
                    ExoPause();
                }
            }
        });
    }

    @Override
    public void play()
    {
        super.play();

        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (playerReady)
                {
                    ExoStart();
                }
            }
        });
    }

    private void seekToImpl(long timeInMillis)
    {
        log.trace("JVL - Called seekToImpl - timeInMillis {}", timeInMillis);
        log.trace("\tCurrent Playbacktime {}", player.getContentPosition());
        log.trace("\tSeek time difference {}", (player.getContentPosition() - timeInMillis) / 1000);

        if(timeInMillis > 0)
        {
            context.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        player.seekTo(timeInMillis);
                    }
                    catch(Exception ex)
                    {
                        log.error("Error during seek request. Position MS: " + timeInMillis, ex);
                        log.debug("Current playback position MS: " + timeInMillis);
                    }
                }
            });
        }
    }

    @Override
    public void seek(long timeInMS)
    {
        log.debug("SEEK CALLED: pushmode {}, timeinMS {}, playerReady {}", pushMode, timeInMS, playerReady);
        super.seek(timeInMS);

        if (playerReady)
        {
            if (!pushMode)
            {
                if (player != null)
                {
                    seekToImpl(timeInMS);
                }
                else
                {
                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                    {
                        log.debug("Seek Resume(Player is Null) {}", timeInMS);
                    }
                    playbackStartPosition = timeInMS;
                }
            }
            else
            {
                if (player != null)
                {
                    seekToImpl(timeInMS);
                }
            }
        }
        else
        {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
            {
                log.debug("Seek Resume {}", timeInMS);
            }
            playbackStartPosition = timeInMS;
        }
    }

    @Override
    public void setSubtitleTrack(int streamPos)
    {
        if(streamPos == Exo2MediaPlayerImpl.DISABLE_TRACK)
        {
            this.showCaptions = false;
        }
        else
        {
            this.showCaptions = true;
        }

        changeTrack(C.TRACK_TYPE_TEXT, streamPos, 0);
    }

    @Override
    public void setAudioTrack(int streamPos)
    {
        if (!ExoIsPlaying())
        {
            initialAudioTrackIndex = streamPos;
        }
        else
        {
            initialAudioTrackIndex = -1;
            changeTrack(C.TRACK_TYPE_AUDIO, streamPos, 0);
        }
    }

    @Override
    public synchronized void flush()
    {
        log.debug("JVL - Flush called from Exo");
        super.flush();

        if (player == null)
        {
            return;
        }

        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    player.prepare(mediaSource, true, false);
                }
                catch(Exception ex) { }

            }
        });
    }

    @Override
    protected void setupPlayer(String sageTVurl)
    {
        initialAudioTrackIndex = -1;

        if (player != null)
        {
            releasePlayer();
        }

        this.url = sageTVurl;

        // VerboseLogUtil.setEnableAllTags(true);

        //if (VerboseLogging.DETAILED_PLAYER_LOGGING)
        log.debug("Setting up the Exo2 media player for: {}", sageTVurl);

        if (pushMode)
        {
            log.debug("Creating Exo2PushDataSource datasource");
            dataSource = new Exo2PushDataSource();
        }
        else
        {
            if (!httpls)
            {
                log.debug("Creating Exo2PullDataSource datasource");
                dataSource = new Exo2PullDataSource(context.getClient().getConnectedServerInfo().address);
            }
            else
            {
                log.debug("Creating null datasource");
                dataSource = null;
            }
        }

        log.debug("Creating handler");
        Handler mainHandler = new Handler();

        CustomRenderersFactory customRenderersFactory = new CustomRenderersFactory(context.getContext());

        if(FfmpegLibrary.isAvailable())
        {
            final int preferExtensionDecoders = MiniclientApplication.get().getClient().properties().getInt(PrefStore.Keys.exoplayer_ffmpeg_extension_setting, 1);

            switch (preferExtensionDecoders)
            {
                case CustomRenderersFactory.EXTENSION_RENDERER_MODE_PREFER:
                    log.debug("Setting FFmpeg Extension to Prefer");
                    customRenderersFactory.setExtensionRendererMode(CustomRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
                    break;
                case CustomRenderersFactory.EXTENSION_RENDERER_MODE_ON:
                    log.debug("Setting FFmpeg Extension to On");
                    customRenderersFactory.setExtensionRendererMode(CustomRenderersFactory.EXTENSION_RENDERER_MODE_ON);
                    break;
                case CustomRenderersFactory.EXTENSION_RENDERER_MODE_OFF:
                    log.debug("Setting FFmpeg Extension to Off");
                    customRenderersFactory.setExtensionRendererMode(CustomRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
                    break;
                default:
                    log.debug("Defaulting FFmpeg Extension to On");
                    customRenderersFactory.setExtensionRendererMode(CustomRenderersFactory.EXTENSION_RENDERER_MODE_ON);
            }
        }

        CustomMediaCodecSelector mediaCodecSelector = new CustomMediaCodecSelector();
        customRenderersFactory.setMediaCodecSelector(mediaCodecSelector);

        trackSelector = new DefaultTrackSelector(context.getContext());
        //player = ExoPlayerFactory.newSimpleInstance(context.getContext(), customRenderersFactory, trackSelector);
        SimpleExoPlayer.Builder builder = new SimpleExoPlayer.Builder(context.getContext(), customRenderersFactory);
        builder.setTrackSelector(trackSelector);
        player = builder.build();

        player.addListener(new Player.EventListener()
        {
            @Override
            public void onPlayerError(ExoPlaybackException error)
            {
                log.debug("PLAYER ERROR: " + error.getMessage());
                context.showErrorMessage(error.getMessage(), "Exo2MediaPlayer");
                error.printStackTrace();
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState)
            {

                if (playbackState == Player.STATE_ENDED)
                {
                    log.debug("Player.STATE_ENDED - Calling stop");

                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                    {
                        log.debug("Player Has Ended, set EOS");
                    }
                    if (playWhenReady)
                    {
                        stop();
                    }
                    //notifySageTVStop();
                    eos = true;
                    Exo2MediaPlayerImpl.this.state = Exo2MediaPlayerImpl.EOS_STATE;
                }
                if (playbackState == Player.STATE_READY)
                {
                    //debugAvailableTracks();
                    log.debug("Player.STATE_READY - setAudioTrack getting called");


                    if (initialAudioTrackIndex != -1)
                    {
                        setAudioTrack(initialAudioTrackIndex);
                        initialAudioTrackIndex = -1;
                    }
                }

            }

            @Override
            public void onSeekProcessed()
            {
                seekPending = false;
            }

            @Override
            public void onTimelineChanged(Timeline timeline, int reason)
            {
                seekPending = false;
            }

            @Override
            public void onPositionDiscontinuity(int reason)
            {
                //log.warn("ExoPlayer: Continuity Error: {}", reason);
                seekPending = false;
            }
        });

        player.addVideoListener(new VideoListener()
        {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio)
            {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                {
                    log.debug("ExoPlayer.onVideoSizeChanged: {}x{}, pixel ratio: {}", width, height, pixelWidthHeightRatio);
                }

                // note if pixel ratio is != 0 then calc the ar and apply it.
                if (pixelWidthHeightRatio != 0f)
                {
                    setVideoSize(width, height, pixelWidthHeightRatio * ((float) width / (float) height));
                }
                else
                {
                    setVideoSize(width, height, 0);
                }
            }

            @Override
            public void onRenderedFirstFrame()
            {

            }
        });


        player.addTextOutput(new TextOutput()
        {
            @Override
            public void onCues(List<Cue> cues)
            {
                if(showCaptions)
                {
                    if (cues.isEmpty())
                    {
                        context.getCaptionsText().setText("");
                    }
                    else
                    {
                        String text = "";
                        for (int i = 0; i < cues.size(); i++)
                        {
                            text += cues.get(i).text;
                        }

                        context.getCaptionsText().setText(text);
                    }
                }
                else
                {
                    context.getCaptionsText().setText("");
                }
            }
        });

        final String sageTVurlFinal = sageTVurl;
        if (!httpls)
        {
            DataSource.Factory dataSourceFactory = new DataSource.Factory()
            {
                @Override
                public DataSource createDataSource()
                {
                    return dataSource;
                }
            };

            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(sageTVurl));

            boolean haveStartPosition = (playbackStartPosition >= 0);

            if (haveStartPosition)
            {
                player.seekTo(playbackStartPosition);
            }

            player.prepare(mediaSource, !haveStartPosition, false);


        }

        //Set seek preferences
        //player.setSeekParameters(SeekParameters.CLOSEST_SYNC);

        // start playing
        player.setVideoSurface(((SurfaceView) context.getVideoView()).getHolder().getSurface());
        player.setPlayWhenReady(true);

        if (VerboseLogging.DETAILED_PLAYER_LOGGING)
        {
            log.debug("Video Player is online");
        }

        this.playerReady = true;
        this.state = MiniPlayerPlugin.PLAY_STATE;

        handler = new Handler();


        context.runOnUiThread(progressRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                if(player!= null && player.getCurrentPosition() >= 0)
                {
                    currentPlaybackPosition = player.getCurrentPosition();
                }
                if(player!= null )
                {
                    handler.postDelayed(progressRunnable, 500);
                }
            }
        });

        handler.postDelayed(progressRunnable, 0);

    }

    public void changeTrack(int trackType, int groupIndex, int trackIndex)
    {
        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                DefaultTrackSelector.SelectionOverride override;
                DefaultTrackSelector.ParametersBuilder parametersBuilder;

                if (trackSelector == null)
                {
                    log.trace("JVL - Track Selector Null");
                    return;
                }

                MappingTrackSelector.MappedTrackInfo trackInfo = trackSelector.getCurrentMappedTrackInfo();

                if (trackInfo == null)
                {
                    log.trace("JVL - Track info null");
                    return;
                }

                try
                {
                    TrackGroupArray trackGroup = trackInfo.getTrackGroups(trackType);

                    if(groupIndex == Exo2MediaPlayerImpl.DISABLE_TRACK) //Disable trackType from rendering
                    {
                        parametersBuilder = trackSelector.buildUponParameters();
                        parametersBuilder.setRendererDisabled(trackType, true); //This should set the track type to render true

                        trackSelector.setParameters(parametersBuilder);
                        log.debug("JVL - Track change executed for disable: TrackType={} TrackGroup={} TrackIndex={}", trackType, trackGroup, trackIndex);
                    }
                    else
                    {
                        if (trackInfo.getTrackSupport(trackType, groupIndex, trackIndex) == RendererCapabilities.FORMAT_HANDLED)
                        {
                            //Clear set new track selection
                            parametersBuilder = trackSelector.buildUponParameters();

                            override = new DefaultTrackSelector.SelectionOverride(groupIndex, trackIndex);
                            parametersBuilder.setRendererDisabled(trackType, false); //This should set the track type to render true
                            parametersBuilder.setSelectionOverride(trackType, trackGroup, override);

                            trackSelector.setParameters(parametersBuilder);
                            log.debug("JVL - Track change executed: TrackType={} TrackGroup={} TrackIndex={}", trackType, trackGroup, trackIndex);
                        }
                        else
                        {
                            log.debug("ExoPlayer is unable to render the track, TrackType {}, GroupIndex {}, TrackIndex {}", trackType, groupIndex, trackIndex);
                        }
                    }
                }
                catch (Exception ex)
                {
                    log.debug("ExoPlayer error changing track, TrackType {}, GroupIndex {}, TrackIndex {}", trackType, groupIndex, trackIndex);
                    log.debug(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }

    public void debugAvailableTracks()
    {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();


        if (mappedTrackInfo == null)
        {
            log.debug("JVL - No Mapped Track Info");
            return;
        }

        for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++)
        {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);

            log.debug("JVL - Track Render Group {}", i);

            if (trackGroups.length != 0)
            {
                int label;

                switch (player.getRendererType(i))
                {
                    case C.TRACK_TYPE_AUDIO:

                        log.debug("JVL - TRACK_TYPE_AUDIO");
                        break;

                    case C.TRACK_TYPE_VIDEO:

                        log.debug("JVL - TRACK_TYPE_VIDEO");
                        break;

                    case C.TRACK_TYPE_TEXT:

                        log.debug("JVL - TRACK_TYPE_TEXT");
                        break;

                    default:
                        continue;
                }

                for (int j = 0; j < trackGroups.length; j++)
                {
                    log.debug("\t Track Group {}", j);

                    for (int k = 0; k < trackGroups.get(j).length; k++)
                    {
                        log.debug("\t\t Track {}, Channels {}, Bitrate {}, Language {}", k, trackGroups.get(j).getFormat(k).channelCount, trackGroups.get(j).getFormat(k).bitrate, trackGroups.get(j).getFormat(k).language);
                        if (mappedTrackInfo.getTrackSupport(i, j, k) == RendererCapabilities.FORMAT_HANDLED)
                        {
                            //Add debug info
                            log.debug("\t\t Format is handled");
                        }
                        else
                        {
                            //Add debug info
                            log.debug("\t\t Format IS NOT HANDLED");
                        }
                    }

                }

            }
            else
            {
                log.debug("JVL - Track Group Empty");
            }
        }

    }

}
