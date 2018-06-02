package sagex.miniclient.android.video.exoplayer2;

import android.net.Uri;
import android.os.Handler;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.video.VideoListener;

import java.io.IOException;

import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.util.VerboseLogging;

import static sagex.miniclient.util.Utils.toHHMMSS;

/**
 * Created by seans on 24/09/16.
 */

public class Exo2MediaPlayerImpl extends BaseMediaPlayerImpl<SimpleExoPlayer, DataSource> {
    private static final long PTS_ROLLOVER = 0x200000000L * 1000000L / 90000L / 1000L;
    private static final long TWENTY_HOURS = 20 * 60 * 60 * 1000;

    long resumePos = -1;
    long logLastTime = -1;

    public Exo2MediaPlayerImpl(MiniClientGDXActivity activity) {
        super(activity, true, false);
    }

    boolean ExoIsPlaying() {
        if (player == null) return false;
        return player.getPlayWhenReady();
    }

    void ExoPause() {
        if (player == null) return;
        player.setPlayWhenReady(false);
    }

    void ExoStart() {
        if (player == null) return;
        player.setPlayWhenReady(true);
    }

    protected void releasePlayer() {
        if (player == null)
            return;

        try {
            if (ExoIsPlaying()) {
                ExoPause();
            }
            //player.reset();
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                log.debug("Player Is Stopped");
        } catch (Throwable t) {

        }

        try {
            player.release();
        } catch (Throwable t) {
        }
        player = null;
        super.releasePlayer();
    }

    @Override
    public Dimension getVideoDimensions() {
        if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("getVideoDimensions");
        if (player != null) {
            if (player.getVideoFormat() != null) {
                Dimension d = new Dimension(player.getVideoFormat().width, player.getVideoFormat().height);
                if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("getVideoSize(): {}", d);
                return d;
            } else {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                    log.debug("getVideoDimensions: player.getFormat is null");
            }
        } else {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                log.debug("getVideoDimensions: player is null");
        }
        return null;
    }

    @Override
    public long getPlayerMediaTimeMillis(long lastServerTime) {
        if (player == null) return 0;
        long time = player.getCurrentPosition();

        // happens after a seek
        if (time == Long.MIN_VALUE) {
            // the player is adjusting
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                log.debug("ExoPlayer: getPlayerMediaTimeMillis(): player adjusting (lastServerTime was: {})", toHHMMSS(lastServerTime, true));
            return 0;
        }

        // NOTE: exoplayer will lose it's time after a seek/resume, so this ensures that it
        // will send back the last known server start time plus the player time
        if (pushMode) {
            if (time<=0) {
                // player is adjusting, after a push/seek.
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                    log.debug("ExoPlayer: getPlayerMediaTimeMillis(): player adjusting using 0 but lastServerTime was {}", toHHMMSS(lastServerTime, true));
                return 0;
            }

            if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
                if (time!=logLastTime) {
                    log.debug("ExoPlayer: getPlayerMediaTimeMillis(): serverTime: {}, time: {}, total: {}", toHHMMSS(lastServerTime, true),
                            toHHMMSS(time, true), toHHMMSS(lastServerTime + time, true));
                }
                logLastTime=time;
            }
            if (time + lastServerTime > PTS_ROLLOVER) {
                time = time - PTS_ROLLOVER;
            }
            time = lastServerTime + time;
        }
        return time;
    }

    @Override
    public void stop() {
        super.stop();
        if (playerReady) {
            if (player == null) return;
            player.setPlayWhenReady(false);
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (playerReady) {
            ExoPause();
        }
    }

    @Override
    public void play() {
        super.play();
        if (playerReady) {
            ExoStart();
        }
    }

    @Override
    public void seek(long timeInMS) {
        super.seek(timeInMS);
        if (playerReady) {
            if (!pushMode) {
                if (player != null) {
                    player.seekTo(timeInMS);
                } else {
                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                        log.debug("Seek Resume(Player is Null) {}", timeInMS);
                    resumePos = timeInMS;
                }
            }
        } else {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("Seek Resume {}", timeInMS);
            resumePos = timeInMS;
        }
    }

    @Override
    public void flush() {
        super.flush();
        if (player == null) return;
        player.seekTo(Long.MIN_VALUE);
    }

    @Override
    protected void setupPlayer(String sageTVurl) {
        if (player != null) {
            releasePlayer();
        }

        // VerboseLogUtil.setEnableAllTags(true);

        //if (VerboseLogging.DETAILED_PLAYER_LOGGING)
        log.debug("Setting up the Exo2 media player for: {}", sageTVurl);

        if (pushMode) {
            dataSource = new Exo2PushDataSource();
        } else {
            if (!sageTVurl.startsWith("stv://")) {
                sageTVurl = "stv://" + context.getClient().getConnectedServerInfo().address +"/"+ sageTVurl;
            }
            dataSource = new Exo2PullDataSource(context.getClient().getConnectedServerInfo().address);
        }
        // mp4 and mkv will play (but not aac audio)
        // File file = new File("/sdcard/Movies/sample-mkv.mkv");
        // File file = new File("/sdcard/Movies/sample-mp4.mp4");
        // ts will not play
        // File file = new File("/sdcard/Movies/sample-ts.ts");
        // Uri.parse(file.toURI().toString())
        //SageTVPlayer.RendererBuilder rendererBuilder = new SageTVExtractorRendererBuilder(context, dataSource, Uri.parse(sageTVurl));
        //SageTVPlayer.RendererBuilder rendererBuilder = new DataSourceExtractorRendererBuilder(context, "sagetv", Uri.parse("http://192.168.1.176:8000/The%20Walking%20Dead%20S05E14%20Spend.mp4"));
        // ExtractorRendererBuilder rendererBuilder = new ExtractorRendererBuilder(context, "sagetv", Uri.parse(file.toURI().toString()));
        //player = new SageTVPlayer(rendererBuilder);

        Handler mainHandler = new Handler();

        DefaultTrackSelector video = new DefaultTrackSelector();

        // See if we need to disable ffmpeg audio decoding
        boolean preferExtensionDecoders = MiniclientApplication.get().getClient().properties().getBoolean(PrefStore.Keys.disable_audio_passthrough, false);
        @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                        (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                        : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context, extensionRendererMode);

        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);

        // EventLogger is in the demo package
        // EventLogger eventLogger = new EventLogger(trackSelector);

        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
        //player = ExoPlayerFactory.newSimpleInstance(context, video, new DefaultLoadControl());

        //EventLogger eventLogger = new EventLogger();
        ///player.setInternalErrorListener(eventLogger);
        //player.setInfoListener(eventLogger);
        //player.addListener(eventLogger);
        player.addListener(new Player.EventListener() {
            @Override
            public void onLoadingChanged(boolean b) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                        log.debug("Player Has Ended, set EOS");
                    if (playWhenReady)
                        stop();
                    //notifySageTVStop();
                    eos = true;
                    Exo2MediaPlayerImpl.this.state = Exo2MediaPlayerImpl.EOS_STATE;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException e) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
        player.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                    log.debug("ExoPlayer.onVideoSizeChanged: {}x{}, ratio: {}", width, height, pixelWidthHeightRatio);

                setVideoSize(width, height, pixelWidthHeightRatio);
            }

            @Override
            public void onRenderedFirstFrame() {

            }
        });


//        player.addTextOutput(new TextRenderer.Output() {
//            @Override
//            public void onCues(List<Cue> cues) {
//                log.debug("Got a Text Cue");
//            }
//        });

        // player.setBackgrounded(false);
        if (resumePos >= 0) {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                log.debug("Resume Seek Postion: {}", resumePos);
            player.seekTo(resumePos);
            resumePos = -1;
        } else {
            //player.seekTo(0);
        }

        final String sageTVurlFinal = sageTVurl;
        ExtractorMediaSource mediaSource = new ExtractorMediaSource(
                Uri.parse(sageTVurl),
                new DataSource.Factory() {
                    @Override
                    public DataSource createDataSource() {
                        return dataSource;
                    }
                },
                new DefaultExtractorsFactory(), mainHandler, new ExtractorMediaSource.EventListener() {
            @Override
            public void onLoadError(IOException e) {
                log.error("FAILED to load: " + sageTVurlFinal);
            }
        });

        player.prepare(mediaSource);

        // start playing
        player.setVideoSurface(context.getVideoView().getHolder().getSurface());
        player.setPlayWhenReady(true);

        if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("Video Player is online");
        playerReady = true;
        state = PLAY_STATE;
    }
}
