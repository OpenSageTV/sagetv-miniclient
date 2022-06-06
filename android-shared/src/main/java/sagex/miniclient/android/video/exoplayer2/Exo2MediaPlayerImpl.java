package sagex.miniclient.android.video.exoplayer2;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.upstream.DataSource;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegLibrary;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.video.VideoSize;

import java.util.List;

import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.ui.AndroidUIController;
import sagex.miniclient.android.util.Logger;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.android.video.MediaSessionCallbackHandler;
import sagex.miniclient.media.SubtitleCodec;
import sagex.miniclient.media.SubtitleTrack;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.util.Utils;
import sagex.miniclient.util.VerboseLogging;
import java.util.concurrent.locks.ReentrantLock;
import android.support.v4.media.session.MediaSessionCompat;

/**
 * Created by seans on 24/09/16.
 */

public class Exo2MediaPlayerImpl extends BaseMediaPlayerImpl<ExoPlayer, DataSource>
{
    static final Logger log = Logger.getLogger(Exo2MediaPlayerImpl.class);
    static final int MAX_PLAYBACK_RETRY_COUNT = 12;

    private MediaSource mediaSource;
    private long playbackStartPosition = -1;
    private int initialAudioTrackIndex = -1;
    private long currentPlaybackPosition = 0;
    private ReentrantLock playbackPositionLock;
    private DefaultTrackSelector trackSelector;
    private int selectedSubtitleTrack = DISABLE_TRACK;

    private boolean errorState = false;
    private int retryCount = 0;

    private boolean showCaptions = false;
    private Handler handler;
    private Runnable progressRunnable;
    private String url;

    MediaSessionCompat mediaSession;

    private SubtitleView subView;

    public Exo2MediaPlayerImpl(AndroidUIController activity)
    {
        super(activity, true, false);
        playbackPositionLock = new ReentrantLock();
    }

    public long getPlaybackPosition()
    {
        long position = 0;

        try
        {
            playbackPositionLock.lock();
            position = this.currentPlaybackPosition;
        }
        catch (Exception ex)
        {
            log.logError("Unexpected error getting playback position", ex);
        }
        finally
        {
            playbackPositionLock.unlock();
        }

        return position;
    }

    public void setPlaybackPosition(long position)
    {
        try
        {
            playbackPositionLock.lock();

            if (position > 0)
            {
                currentPlaybackPosition = position;
            }
            else
            {
                //Set to zero if less than zero;
                currentPlaybackPosition = 0;
            }

        }
        catch (Exception ex)
        {
            log.logError("Unexpected error setting playback position", ex);

        }
        finally
        {
            playbackPositionLock.unlock();
        }
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

        log.logInfo("Pause was called");
        player.setPlayWhenReady(false);
    }

    void ExoStart()
    {
        if (player == null)
        {
            return;
        }
        log.logDebug("Start was called");
        player.setPlayWhenReady(true);
    }

    protected void releasePlayer()
    {
        if(mediaSession != null)
        {
            log.logDebug("Releaseing Android Media Session");
            mediaSession.setActive(false);
            mediaSession.release();
        }

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
                    catch (Exception ex)
                    {
                        log.logError("Error pausing video during player releasing", ex);
                    }

                    try
                    {
                        player.release();
                    }
                    catch (Exception ex)
                    {
                        log.logError("Error calling release on player", ex);
                    }

                    player = null;
                    Exo2MediaPlayerImpl.super.releasePlayer();
                }
            }
        });

        this.RemoveSubTitleView();
    }

    @Override
    public Dimension getVideoDimensions()
    {
        log.logDebug("getVideoDimensions");

        if (player != null)
        {
            if (player.getVideoFormat() != null)
            {
                Dimension d = new Dimension(player.getVideoFormat().width, player.getVideoFormat().height);
                log.logDebug("getVideoSize(): " + d);

                return d;
            }
            else
            {
                log.logDebug("getVideoDimensions: player.getFormat is null");
            }
        }
        else
        {
            log.logDebug("getVideoDimensions: player is null");
        }
        return null;
    }

    @Override
    public long getPlayerMediaTimeMillis(long lastServerTime)
    {
        long position = this.getPlaybackPosition();

        //log.debug("ExoLogging - getPlayerMediaTimeMillis Called lastServerTime=" + Utils.toHHMMSS(lastServerTime) + " position=" + Utils.toHHMMSS(position));

        if (lastServerTime < 0)
        {
            log.logDebug("getPlayerMediaTimeMillis(): Flush was called waiting for last serverTime to be > 0");
            return -1;
        }

        return lastServerTime + position;
    }

    @Override
    public void stop()
    {
        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                log.logDebug("Stop called");
                if(player != null)
                {
                    player.stop();
                }

                if(mediaSession != null)
                {
                    mediaSession.setActive(false);
                }

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

        super.stop();
    }

    @Override
    public void pause()
    {
        log.logDebug("Pause called");

        if (this.getState() == MiniPlayerPlugin.PAUSE_STATE && !pushMode)
        {
            log.logDebug("Already in pause state.  Seeking frame instead...");
            //TODO: Could not find the framerate in ExoPlayer.  Going to assume 30fps for now.
            this.seek(this.getPlaybackPosition() + Math.round(1000.0 / 30.0));
            return;
        }


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

        updateMediaSessionPlaybackState(Exo2MediaPlayerImpl.this.getPlaybackPosition());
    }

    @Override
    public void play()
    {
        log.logDebug("Play called");

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

        updateMediaSessionPlaybackState(Exo2MediaPlayerImpl.this.getPlaybackPosition());
    }

    private void seekToImpl(long timeInMillis)
    {
        if (timeInMillis > 0)
        {
            context.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        log.logDebug("Seek Called - Current Position: " + player.getContentPosition() + "  Seek Request: " + timeInMillis + " Difference: " + (player.getContentPosition() - timeInMillis));

                        int wait = 0;
                        player.seekTo(timeInMillis);

                        //Wait up to a second for the seek to complete
                        while (player.getCurrentPosition() < timeInMillis && wait < 10)
                        {
                            log.logDebug("Seek Called -  Waiting for current position to match Seek request.  Current Position: " + player.getContentPosition() + "  Seek Request: " + timeInMillis);
                            Thread.sleep(100);
                            wait++;
                        }

                        Exo2MediaPlayerImpl.this.currentPlaybackPosition = player.getCurrentPosition();
                    }
                    catch (Exception ex)
                    {
                        log.logError("Error during seek request. Position MS: " + timeInMillis, ex);
                    }
                }
            });
        }
    }

    @Override
    public void seek(long timeInMS)
    {
        try
        {
            playbackPositionLock.lock();


            //currentPlaybackPosition = 0; //Set this to zero during seek.  Lock will hopefully keep it at zero unti we are completed

            log.logDebug("Seek - pushmode: " + pushMode + ", timeinMS " + timeInMS + ", playerReady " + playerReady);

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

                        log.logDebug("Seek player is null storing position: " + timeInMS);

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

                log.logDebug("Seek Resume: " + timeInMS);
                playbackStartPosition = timeInMS;
            }
        }
        catch (Exception ex)
        {
            log.logError("Unexpected error during seek", ex);
            ex.printStackTrace();
        }
        finally
        {
            playbackPositionLock.unlock();
        }
    }

    @Override
    public void setSubtitleTrack(int streamPos)
    {
        log.logDebug("Set Subtitle Track Called: " + streamPos);

        if (streamPos == Exo2MediaPlayerImpl.DISABLE_TRACK)
        {
            this.showCaptions = false;
            this.RemoveSubTitleView();
        }
        else
        {
            this.showCaptions = true;
            this.AddSubTitleView();
        }

        changeTrack(C.TRACK_TYPE_TEXT, streamPos, 0);
    }

    @Override
    public int getSelectedSubtitleTrack()
    {
        return this.selectedSubtitleTrack;
    }

    @Override
    public int getSubtitleTrackCount()
    {
        return getTrackCount(C.TRACK_TYPE_TEXT);
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
        log.logDebug("Flush called");
        super.flush();

        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    playbackPositionLock.lock();

                    if (player == null)
                    {
                        return;
                    }

                    player.setMediaSource(mediaSource, true);
                    player.prepare();

                    log.logDebug("After Flush was called Current Playback Position: " + Utils.toHHMMSS(player.getCurrentPosition()));
                    Exo2MediaPlayerImpl.this.currentPlaybackPosition = player.getCurrentPosition();
                }
                catch (Exception ex)
                {
                }
                finally
                {
                    playbackPositionLock.unlock();
                }


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
        log.logDebug("Setting up the Exo2 media player for: " + sageTVurl);

        if (pushMode)
        {
            log.logDebug("Creating Exo2PushDataSource datasource");
            dataSource = new Exo2PushDataSource();
        }
        else
        {
            if (!httpls)
            {
                log.logDebug("Creating datasource");
                dataSource = new Exo2PullDataSource(context.getClient().getConnectedServerInfo().address);
            }
            else
            {
                log.logDebug("Creating null datasource");
                dataSource = null;
            }
        }

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context.getContext());

        if (FfmpegLibrary.isAvailable())
        {
            final int preferExtensionDecoders = MiniclientApplication.get().getClient().properties().getInt(PrefStore.Keys.exoplayer_ffmpeg_extension_setting, 1);

            switch (preferExtensionDecoders)
            {
                case DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER:
                    log.logDebug("Setting FFmpeg Extension to Prefer");
                    renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
                    break;
                case DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON:
                    log.logDebug("Setting FFmpeg Extension to On");
                    renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
                    break;
                case DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF:
                    log.logDebug("Setting FFmpeg Extension to Off");
                    renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
                    break;
                default:
                    log.logDebug("Defaulting FFmpeg Extension to On");
                    renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
            }
        }

        CustomMediaCodecSelector mediaCodecSelector = new CustomMediaCodecSelector();
        renderersFactory.setMediaCodecSelector(mediaCodecSelector);

        trackSelector = new DefaultTrackSelector(context.getContext());


        ExoPlayer.Builder builder = new ExoPlayer.Builder(context.getContext(), renderersFactory);

        builder.setTrackSelector(trackSelector);
        player = builder.build();
        //player.addAnalyticsListener(new EventLogger(trackSelector));

        player.addListener(new Player.Listener()
        {
            @Override
            public void onPlayerError(PlaybackException error)
            {
                log.logDebug("PLAYER ERROR: " + error.getErrorCodeName());
                error.printStackTrace();

                if (retryCount == 0)
                {
                    //Show toast on first error
                    context.showErrorMessage(error.getErrorCodeName(), "Exo2MediaPlayer");
                }

                if (retryCount <= MAX_PLAYBACK_RETRY_COUNT)
                {

                    errorState = true;
                    retryCount++;

                    player.seekTo(player.getCurrentPosition() + 100);
                    player.prepare();
                }
                else
                {
                    log.logDebug("PLAYER ERROR: " + error.getErrorCodeName());
                    log.logError("Playback Exception: " + error.getErrorCodeName(), error);
                    context.showErrorMessage("Max playback retry reached!", "Exo2MediaPlayer");
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState)
            {
                if (playbackState == Player.STATE_ENDED)
                {
                    log.logDebug("Player.STATE_ENDED");
                    log.logDebug("Player Has Ended, set EOS");
                    //stop(); - JVL: Not sure if we will need to do this or not
                    //notifySageTVStop();
                    eos = true;
                    state = Exo2MediaPlayerImpl.EOS_STATE;
                }
                if (playbackState == Player.STATE_READY)
                {
                    log.logDebug("Player.STATE_READY - Media loaded and ready for playback");
                    if (errorState)
                    {
                        errorState = false;
                        retryCount = 0;
                    }

                    log.logDebug("Player.STATE_READY - setAudioTrack getting called");
                    if (initialAudioTrackIndex != -1)
                    {
                        setAudioTrack(initialAudioTrackIndex);
                        initialAudioTrackIndex = -1;
                    }

                    log.logDebug("Player.STATE_READY - Debugging available tracks in file");
                    debugAvailableTracks();

                    long duration = 0;

                    if(player.getDuration() < 0)
                    {
                        duration = -1;
                    }
                    else
                    {
                        duration = player.getDuration();
                    }
                    //Library files start with stc:// but do not have push in it
                    //Live TV has push: with a lot of other data in it

                    setMediaSessionMetadata(sageTVurl, duration);
                    mediaSession.setActive(true);
                    updateMediaSessionPlaybackState(Exo2MediaPlayerImpl.this.getPlaybackPosition());
                }
                if (playbackState == Player.STATE_IDLE)
                {
                    if (errorState)
                    {
                        log.logDebug("Player.STATE_IDLE - Error state is true, retry count: " + retryCount);
                    }
                }

            }

            @Override
            public void onTimelineChanged(Timeline timeline, int reason)
            {
                updateMediaSessionPlaybackState(Exo2MediaPlayerImpl.this.getPlaybackPosition());
                seekPending = false;
            }

            @Override
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason)
            {
                switch (reason)
                {
                    case Player.DISCONTINUITY_REASON_SEEK:
                        updateMediaSessionPlaybackState(Exo2MediaPlayerImpl.this.getPlaybackPosition());
                        seekPending = false;
                        break;

                }
            }

            @Override
            public void onVideoSizeChanged(VideoSize videoSize)
            {
                int width = videoSize.width;
                int height = videoSize.height;
                float pixelWidthHeightRatio = videoSize.pixelWidthHeightRatio;

                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                {
                    log.logDebug("ExoPlayer.onVideoSizeChanged: " + width + "x" + height + ", pixel ratio: " + pixelWidthHeightRatio);
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
        });


        player.addListener(new Player.Listener()
        {
            @Override
            public void onCues(List<Cue> cues)
            {
                if (showCaptions && subView != null)
                    subView.onCues(cues);
            }
        });


        final String sageTVurlFinal = sageTVurl;
        if (!httpls)
        {

            com.google.android.exoplayer2.upstream.DataSource.Factory dataSourceFactory = new DataSource.Factory()
            {
                @Override
                public DataSource createDataSource()
                {
                    return dataSource;
                }
            };

            //mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(sageTVurl));

            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(sageTVurl)));


            boolean haveStartPosition = (playbackStartPosition >= 0);

            if (haveStartPosition)
            {
                player.seekTo(playbackStartPosition);
                log.logDebug("ExoLogging - Have start position");
                log.logDebug("ExoLogging - Start Position: " + playbackStartPosition);
            }

            log.logDebug("ExoLogging - Preparing playback");
            //player.prepare(mediaSource, !haveStartPosition, false);
            player.setMediaSource(mediaSource, !haveStartPosition);
            player.prepare();

        }

        //Set seek preferences
        //player.setSeekParameters(SeekParameters.CLOSEST_SYNC);

        // start playing
        player.setVideoSurface(((SurfaceView) context.getVideoView()).getHolder().getSurface());
        player.setPlayWhenReady(true);

        //Create Media Session
        mediaSession = new MediaSessionCompat(this.context.getContext(), "SageTV Android TV Client");
        mediaSession.setCallback(new MediaSessionCallbackHandler(this, context.getClient(), context.getContext()));

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);

        if (VerboseLogging.DETAILED_PLAYER_LOGGING)
        {
            log.logDebug("Video Player is online");
        }

        this.playerReady = true;
        super.play();

        log.logDebug("Creating handler");
        handler = new Handler();


        context.runOnUiThread(progressRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                if (player != null)
                {
                    Exo2MediaPlayerImpl.this.setPlaybackPosition(player.getCurrentPosition());
                    handler.postDelayed(progressRunnable, 500);
                }
                else
                {
                    Exo2MediaPlayerImpl.this.setPlaybackPosition(0);
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
                    log.logTrace("Track Selector Null");
                    return;
                }

                MappingTrackSelector.MappedTrackInfo trackInfo = trackSelector.getCurrentMappedTrackInfo();

                if (trackInfo == null)
                {
                    log.logTrace("Track info null");
                    return;
                }

                try
                {
                    TrackGroupArray trackGroup = trackInfo.getTrackGroups(trackType);

                    if (groupIndex == Exo2MediaPlayerImpl.DISABLE_TRACK) //Disable trackType from rendering
                    {
                        parametersBuilder = trackSelector.buildUponParameters();
                        parametersBuilder.setRendererDisabled(trackType, true); //This should set the track type to render true

                        trackSelector.setParameters(parametersBuilder);
                        log.logDebug("JVL - Track change executed for disable: TrackType=" + trackType + " TrackGroup=" + trackGroup + " TrackIndex=" + trackIndex);
                        selectedSubtitleTrack = DISABLE_TRACK;
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
                            log.logDebug("JVL - Track change executed: TrackType=" + trackType + " TrackGroup=" + trackGroup + " TrackIndex=" + trackIndex);
                            selectedSubtitleTrack = groupIndex;
                        }
                        else
                        {
                            log.logDebug("Unable to render the track, TrackType= " + trackType + ", GroupIndex= " + groupIndex + ", TrackIndex= " + trackIndex);
                        }
                    }
                }
                catch (Exception ex)
                {
                    log.logError("Error render the track, TrackType= " + trackType + ", GroupIndex= " + groupIndex + ", TrackIndex= " + trackIndex, ex);
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * Counts support tracks of the given track type
     *
     * @param RenderType The type of track (VIDEO, AUDIO, TEXT, ect...)
     */
    public int getTrackCount(int RenderType)
    {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        int count = 0;

        if (mappedTrackInfo == null)
        {
            log.logWarning("No Mapped Track Info found");
            return count;
        }

        TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(RenderType);

        if (trackGroups.length != 0)
        {
            //This code is assuming one track to a group.  It will increment the count by one
            //if there is a supported track in the groun
            for (int j = 0; j < trackGroups.length; j++)
            {
                boolean supported = false;

                for (int k = 0; k < trackGroups.get(j).length; k++)
                {
                    if (mappedTrackInfo.getTrackSupport(RenderType, j, k) == C.FORMAT_HANDLED)
                    {
                        log.logDebug("Format is handled");
                        supported = true;
                    }
                    else
                    {
                        log.logDebug("Format IS NOT HANDLED");
                    }
                }

                if (supported)
                {
                    count++;
                }
            }
        }

        return count;
    }

    @Override
    public SubtitleTrack[] getSubtitleTracks()
    {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(C.TRACK_TYPE_TEXT);
        int trackCount = trackGroups.length;
        SubtitleTrack[] tracks = new SubtitleTrack[0];

        if (trackCount > 0)
        {
            tracks = new SubtitleTrack[trackCount];

            for (int i = 0; i < trackCount; i++)
            {
                TrackGroup trackGroup = trackGroups.get(i);

                SubtitleCodec codec = SubtitleCodec.parse(trackGroup.getFormat(0).sampleMimeType);
                String langugae = trackGroup.getFormat(0).language;
                String label = trackGroup.getFormat(0).label;
                boolean supported = (mappedTrackInfo.getTrackSupport(C.TRACK_TYPE_TEXT, i, 0) == C.FORMAT_HANDLED);

                SubtitleTrack track = new SubtitleTrack(i, codec, langugae, label, supported);
                tracks[i] = track;
            }
        }

        return tracks;
    }

    public void debugAvailableTracks()
    {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();

        if (mappedTrackInfo == null)
        {
            log.logWarning("No Mapped Track Info");
            return;
        }

        for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++)
        {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);

            log.logDebug("Track Render Group " + i);

            if (trackGroups.length != 0)
            {
                int label;

                switch (player.getRendererType(i))
                {
                    case C.TRACK_TYPE_AUDIO:

                        log.logDebug("TRACK_TYPE_AUDIO");
                        break;

                    case C.TRACK_TYPE_VIDEO:

                        log.logDebug("TRACK_TYPE_VIDEO");
                        break;

                    case C.TRACK_TYPE_TEXT:

                        log.logDebug("TRACK_TYPE_TEXT");
                        break;

                    default:
                        continue;
                }

                for (int j = 0; j < trackGroups.length; j++)
                {
                    log.logDebug("\t Track Group " + j);

                    for (int k = 0; k < trackGroups.get(j).length; k++)
                    {
                        Format format = trackGroups.get(j).getFormat(k);

                        log.logDebug("\t\tTrack : " + k);
                        log.logDebug("\t\tContainer MimeType: " + format.containerMimeType);
                        log.logDebug("\t\tSample MimeType: " + format.sampleMimeType);
                        log.logDebug("\t\tCodecs: " + format.codecs);
                        log.logDebug("\t\tLanguage: " + format.language);


                        if (player.getRendererType(i) == C.TRACK_TYPE_TEXT)
                        {

                            /*if((MimeTypes.APPLICATION_CEA708.equalsIgnoreCase(format.sampleMimeType) || MimeTypes.APPLICATION_CEA608.equalsIgnoreCase(format.sampleMimeType))
                                    && format.language.equalsIgnoreCase("en"))
                            {
                                log.debug("-----Setting Subtitle track to active");
                                //Enable this track.  This is just debugging
                                this.setSubtitleTrack(j);
                            }
                            else
                            {
                                log.debug("-----NOT Setting Subtitle track to active");
                            }*/
                        }


                        if (player.getRendererType(i) == C.TRACK_TYPE_AUDIO)
                        {
                            log.logDebug("\t\tChannel: " + format.channelCount);
                            log.logDebug("\t\tBitrate: " + format.bitrate);
                            log.logDebug("\t\tAverageBitrate: " + format.averageBitrate);
                            log.logDebug("\t\tPeakBitrate: " + format.peakBitrate);
                            log.logDebug("\t\tPCM Encoding: " + format.pcmEncoding);

                        }

                        if (player.getRendererType(i) == C.TRACK_TYPE_VIDEO)
                        {
                            if (format.colorInfo != null)
                            {
                                log.logDebug("\t\tColor: " + format.colorInfo.toString());
                            }
                            log.logDebug("\t\tBitrate: " + format.bitrate);
                            log.logDebug("\t\tAverageBitrate: " + format.averageBitrate);
                            log.logDebug("\t\tPeakBitrate: " + format.peakBitrate);
                            log.logDebug("\t\tFramerate: " + format.frameRate);
                            log.logDebug("\t\tHeight: " + format.height);
                            log.logDebug("\t\tWidth: " + format.width);

                        }

                        log.logDebug("\t\tID: " + format.id);
                        log.logDebug("\t\tLabel: " + format.label);
                        if (format.metadata != null)
                        {
                            log.logDebug("\t\t\tMetadata length: " + format.metadata.length());
                        }


                        if (mappedTrackInfo.getTrackSupport(i, j, k) == RendererCapabilities.FORMAT_HANDLED)
                        {
                            //Add debug info
                            log.logDebug("\t\t Format is handled");
                        }
                        else
                        {
                            //Add debug info
                            log.logDebug("\t\t Format IS NOT HANDLED");
                        }
                    }

                }

            }
            else
            {
                log.logDebug("Track Group Empty");
            }
        }
    }

    // cncb - Add and remove ExoPlayer2 SubTitleView for embedded PGS subtitles
    private void AddSubTitleView()
    {
        if (subView == null)
        {
            subView = new SubtitleView(context.getContext());
            subView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            context.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        FrameLayout layout = (FrameLayout) context.getVideoView().getParent();
                        layout.addView(subView);
                    }
                    catch (Exception ex)
                    {
                        log.logError("Error adding SubTitleView: ", ex);
                    }
                }
            });
        }
    }

    private void RemoveSubTitleView()
    {
        if (subView != null)
        {
            context.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        FrameLayout layout = (FrameLayout) context.getVideoView().getParent();
                        layout.removeView(subView);
                    }
                    catch (Exception ex)
                    {
                        log.logError("Error removing SubTitleView ",  ex);
                    }
                    finally
                    {
                        subView = null;
                    }
                }
            });
        }
    }

    private void updateMediaSessionPlaybackState(long playbackPostion)
    {
        if(mediaSession != null && mediaSession.isActive()) {
            PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
            stateBuilder.setActions(this.getMediaSessionActions());

            if (player != null && getState() == PLAY_STATE) {
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, playbackPostion, 1.0f);
            } else {
                stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
            }

            mediaSession.setPlaybackState(stateBuilder.build());
        }
    }

    private long getMediaSessionActions()
    {
        long actions = 0;

        if(player != null)
        {
            if (getState() == PLAY_STATE)
            {
                actions = PlaybackState.ACTION_STOP;
                actions |= PlaybackState.ACTION_PAUSE;
                actions |= PlaybackState.ACTION_FAST_FORWARD;
                actions |= PlaybackState.ACTION_REWIND;
                actions |= PlaybackState.ACTION_SEEK_TO;
                actions |= PlaybackState.ACTION_SKIP_TO_NEXT;
                actions |= PlaybackState.ACTION_SKIP_TO_PREVIOUS;
            }
            else
            {
                actions = PlaybackState.ACTION_PLAY;
            }

        }

        return actions;
    }

    private void setMediaSessionMetadata(String displayTitle, long duration)
    {
        MediaMetadataCompat.Builder metaDataBuilder = new MediaMetadataCompat.Builder();

        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, displayTitle);
        metaDataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);
        mediaSession.setMetadata(metaDataBuilder.build());
    }
}
