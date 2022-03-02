package sagex.miniclient.android.video.exoplayer2;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.upstream.DataSource;

import android.net.Uri;
import android.os.Handler;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RendererCapabilities;
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
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.video.VideoSize;

import java.util.List;
import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.ui.AndroidUIController;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.util.Utils;
import sagex.miniclient.util.VerboseLogging;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import static sagex.miniclient.util.Utils.toHHMMSS;

/**
 * Created by seans on 24/09/16.
 */

public class Exo2MediaPlayerImpl extends BaseMediaPlayerImpl<ExoPlayer, DataSource> {
    static final int MAX_PLAYBACK_RETRY_COUNT = 12;

    MediaSource mediaSource;
    long playbackStartPosition = -1;
    int initialAudioTrackIndex = -1;
    long currentPlaybackPosition = 0;
    ReentrantLock playbackPositionLock;
    DefaultTrackSelector trackSelector;

    boolean errorState = false;
    int retryCount = 0;


    boolean showCaptions = false;
    Handler handler;
    Runnable progressRunnable;
    String url;

    SubtitleView subView;

    public Exo2MediaPlayerImpl(AndroidUIController activity) {
        super(activity, true, false);
        playbackPositionLock = new ReentrantLock();
    }

    public long getPlaybackPosition() {
        long position = 0;

        try {
            playbackPositionLock.lock();
            position = this.currentPlaybackPosition;
        } catch (Exception ex) {
            log.error("Exception thrown getting playback position", ex);
        } finally {
            playbackPositionLock.unlock();
        }

        return position;
    }

    public void setPlaybackPosition(long position) {
        try {
            playbackPositionLock.lock();

            if (position > 0) {
                currentPlaybackPosition = position;
            } else {
                //Set to zero if less than zero;
                currentPlaybackPosition = 0;
            }

        } catch (Exception ex) {
            log.error("Error setting playback position", ex);
        } finally {
            playbackPositionLock.unlock();
        }
    }

    boolean ExoIsPlaying() {
        if (player == null) {
            return false;
        }

        return player.getPlayWhenReady();
    }

    void ExoPause() {
        if (player == null) {
            return;
        }

        log.debug("Pause was called");
        player.setPlayWhenReady(false);
    }

    void ExoStart() {
        if (player == null) {
            return;
        }
        log.debug("Start was called");
        player.setPlayWhenReady(true);
    }

    protected void releasePlayer() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    try {
                        if (ExoIsPlaying()) {
                            ExoPause();
                        }
                    } catch (Exception ex) {
                        log.error("Error pausing/stoppig video before releasing", ex);
                    }

                    try {
                        player.release();
                    } catch (Exception ex) {
                        log.error("Error calling release on player", ex);
                    }

                    player = null;
                    Exo2MediaPlayerImpl.super.releasePlayer();
                }
            }
        });

        this.RemoveSubTitleView();
    }

    @Override
    public Dimension getVideoDimensions() {
        if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
            log.debug("getVideoDimensions");
        }
        if (player != null) {
            if (player.getVideoFormat() != null) {
                Dimension d = new Dimension(player.getVideoFormat().width, player.getVideoFormat().height);
                if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
                    log.debug("getVideoSize(): {}", d);
                }
                return d;
            } else {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
                    log.debug("getVideoDimensions: player.getFormat is null");
                }
            }
        } else {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
                log.debug("getVideoDimensions: player is null");
            }
        }
        return null;
    }

    @Override
    public long getPlayerMediaTimeMillis(long lastServerTime) {
        long position = this.getPlaybackPosition();

        //log.debug("ExoLogging - getPlayerMediaTimeMillis Called lastServerTime=" + Utils.toHHMMSS(lastServerTime) + " position=" + Utils.toHHMMSS(position));

        if (lastServerTime < 0) {
            log.debug("Flush - Flush was called waiting for last serverTime to be > 0");
            return -1;
        }

        return lastServerTime + position;

    }

    @Override
    public void stop() {
        super.stop();

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (playerReady) {
                    if (player == null) {
                        return;
                    }
                    player.setPlayWhenReady(false);
                }
            }
        });
    }

    @Override
    public void pause() {


        if (this.getState() == MiniPlayerPlugin.PAUSE_STATE && !pushMode) {
            log.debug("In pause state.  Seek frame instead...");
            //TODO: Could not find the framerate in ExoPlayer.  Going to assume 30fps for now.
            this.seek(this.getPlaybackPosition() + Math.round(1000.0 / 30.0));
            return;
        }


        super.pause();

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (playerReady) {
                    ExoPause();
                }
            }
        });
    }

    @Override
    public void play() {
        super.play();

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (playerReady) {
                    ExoStart();
                }
            }
        });
    }

    private void seekToImpl(long timeInMillis) {
        if (timeInMillis > 0) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.debug("Seek - Called.  Current Position: {}  Seek Request: {} Difference: {}", player.getContentPosition(), timeInMillis, player.getContentPosition() - timeInMillis);

                        int wait = 0;
                        player.seekTo(timeInMillis);

                        //Wait up to a second for the seek to complete
                        while (player.getCurrentPosition() < timeInMillis && wait < 10) {
                            log.debug("Seek -  Waiting for current position to match Seek request.  Current Position: {}  Seek Request: {} ", player.getContentPosition(), timeInMillis);
                            Thread.sleep(100);
                            wait++;
                        }

                        Exo2MediaPlayerImpl.this.currentPlaybackPosition = player.getCurrentPosition();
                    } catch (Exception ex) {
                        log.error("Error during seek request. Position MS: " + timeInMillis, ex);
                        log.debug("Current playback position MS: " + timeInMillis);
                    }
                }
            });
        }
    }

    @Override
    public void seek(long timeInMS) {
        try {
            playbackPositionLock.lock();


            //currentPlaybackPosition = 0; //Set this to zero during seek.  Lock will hopefully keep it at zero unti we are completed

            log.debug("ExoLogging - pushmode {}, timeinMS {}, playerReady {}", pushMode, timeInMS, playerReady);

            super.seek(timeInMS);

            if (playerReady) {
                if (!pushMode) {
                    if (player != null) {
                        seekToImpl(timeInMS);
                    } else {
                        if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
                            log.debug("Seek Resume(Player is Null) {}", timeInMS);
                        }
                        playbackStartPosition = timeInMS;
                    }
                } else {
                    if (player != null) {
                        seekToImpl(timeInMS);
                    }
                }
            } else {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
                    log.debug("Seek Resume {}", timeInMS);
                }
                playbackStartPosition = timeInMS;
            }
        } catch (Exception ex) {
            log.debug("Exception thrown durring seek: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            playbackPositionLock.unlock();
        }
    }

    @Override
    public void setSubtitleTrack(int streamPos) {
        if (streamPos == Exo2MediaPlayerImpl.DISABLE_TRACK) {
            this.showCaptions = false;
            this.RemoveSubTitleView();
        } else {
            this.showCaptions = true;
            this.AddSubTitleView();
        }

        changeTrack(C.TRACK_TYPE_TEXT, streamPos, 0);
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
        log.debug("ExoLogging - Flush being called");
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

                    log.debug("After Flush was called Current Playback Position: {}",  Utils.toHHMMSS(player.getCurrentPosition()));

                    Exo2MediaPlayerImpl.this.currentPlaybackPosition = player.getCurrentPosition();
                }
                catch (Exception ex){}
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

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context.getContext());

        if(FfmpegLibrary.isAvailable())
        {
            final int preferExtensionDecoders = MiniclientApplication.get().getClient().properties().getInt(PrefStore.Keys.exoplayer_ffmpeg_extension_setting, 1);

            switch (preferExtensionDecoders)
            {
                case DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER:
                    log.debug("Setting FFmpeg Extension to Prefer");
                    renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
                    break;
                case DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON:
                    log.debug("Setting FFmpeg Extension to On");
                    renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
                    break;
                case DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF:
                    log.debug("Setting FFmpeg Extension to Off");
                    renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
                    break;
                default:
                    log.debug("Defaulting FFmpeg Extension to On");
                    renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
            }
        }

        CustomMediaCodecSelector mediaCodecSelector = new CustomMediaCodecSelector();
        renderersFactory.setMediaCodecSelector(mediaCodecSelector);

        trackSelector = new DefaultTrackSelector(context.getContext());


        SimpleExoPlayer.Builder builder =  new SimpleExoPlayer.Builder(context.getContext(), renderersFactory);

        builder.setTrackSelector(trackSelector);
        player = builder.build();
        player.addAnalyticsListener(new EventLogger(trackSelector));


        player.addListener(new Player.Listener()
        {
            @Override
            public void onPlayerError(PlaybackException error)
            {
                log.debug("PLAYER ERROR: " + error.getErrorCodeName());
                error.printStackTrace();

                if(retryCount == 0)
                {
                    //Show toast on first error
                    context.showErrorMessage(error.getErrorCodeName(), "Exo2MediaPlayer");
                }

                if(retryCount <= MAX_PLAYBACK_RETRY_COUNT)
                {

                    errorState = true;
                    retryCount++;

                    player.seekTo(player.getCurrentPosition() + 100);
                    player.prepare();
                }
                else
                {
                    context.showErrorMessage("Max playback retry reached!", "Exo2MediaPlayer");
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState)
            {



                if (playbackState == Player.STATE_ENDED)
                {
                    log.debug("Player.STATE_ENDED - Calling stop");

                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                    {
                        log.debug("Player Has Ended, set EOS");
                        //stop(); - JVL: Not sure if we will need to do this or not
                    }

                    //notifySageTVStop();
                    eos = true;
                    Exo2MediaPlayerImpl.this.state = Exo2MediaPlayerImpl.EOS_STATE;
                }
                if (playbackState == Player.STATE_READY)
                {
                    if(errorState)
                    {
                        errorState = false;
                        retryCount = 0;
                    }

                    //debugAvailableTracks();
                    log.debug("Player.STATE_READY - setAudioTrack getting called");


                    if (initialAudioTrackIndex != -1)
                    {
                        setAudioTrack(initialAudioTrackIndex);
                        initialAudioTrackIndex = -1;
                    }

                    log.debug("Player.STATE_READY - Debugging available tracks in file");
                    debugAvailableTracks();
                }
                if (playbackState == Player.STATE_IDLE)
                {
                    if(errorState)
                    {
                        log.debug("Player.STATE_IDLE - Error state is true, retry count " + retryCount);
                    }

                }

            }

            @Override
            public void onTimelineChanged(Timeline timeline, int reason)
            {
                seekPending = false;
            }

            @Override
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason)
            {
                switch (reason)
                {
                    case Player.DISCONTINUITY_REASON_SEEK:
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
        });




        player.addListener(new Player.Listener() {
            @Override
            public void onCues(List<Cue> cues)
            {
                if( showCaptions && subView != null)
                    subView.onCues( cues);
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
                log.debug("ExoLogging - Have start position");
                log.debug("ExoLogging - Start Position: " + playbackStartPosition);
            }

            log.debug("ExoLogging - Preparing playback");
            //player.prepare(mediaSource, !haveStartPosition, false);
            player.setMediaSource(mediaSource, !haveStartPosition);
            player.prepare();

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

        log.debug("Creating handler");
        handler = new Handler();


        context.runOnUiThread(progressRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                if(player!= null )
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

    /**
     * Counts support tracks of the given track type
     * @param RenderType The type of track (VIDEO, AUDIO, TEXT, ect...)
     */
    public int getTrackCount(int RenderType)
    {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        int count = 0;

        if (mappedTrackInfo == null)
        {
            log.warn("No Mapped Track Info found");
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
                        log.debug("\t\t Format is handled");
                        supported = true;
                    }
                    else
                    {
                        log.debug("\t\t Format IS NOT HANDLED");
                    }
                }

                if(supported)
                {
                    count++;
                }
            }
        }

        return count;
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
                        Format format = trackGroups.get(j).getFormat(k);

                        log.debug("\t\t Track {}, Channels {}, Bitrate {}, Language {}", k, trackGroups.get(j).getFormat(k).channelCount, trackGroups.get(j).getFormat(k).bitrate, trackGroups.get(j).getFormat(k).language);

                        if(player.getRendererType(i) == C.TRACK_TYPE_TEXT)
                        {
                            log.debug("\t\tContainer MimeType" + format.containerMimeType);
                            log.debug("\t\tSample MimeType" + format.sampleMimeType);
                            log.debug("\t\tCodecs" + format.codecs);
                            log.debug("\t\tLanguage" + format.language);
                            log.debug("\t\tID" + format.id);
                            log.debug("\t\tLabel: " + format.label);
                            log.debug("\t\tMetadata length: " + format.metadata.length());
                        }
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

    // cncb - Add and remove ExoPlayer2 SubTitleView for embedded PGS subtitles
    private void AddSubTitleView()
    {
        if( subView == null)
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
                        log.debug("Error adding SubTitleView: " + ex.getMessage());
                    }
                }
            });
        }
    }

    private void RemoveSubTitleView()
    {
        if( subView != null)
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
                        log.debug("Error removing SubTitleView: " + ex.getMessage());
                    }
                    finally
                    {
                        subView = null;
                    }
                }
            });
        }
    }

}
