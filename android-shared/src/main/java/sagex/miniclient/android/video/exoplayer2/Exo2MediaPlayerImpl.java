package sagex.miniclient.android.video.exoplayer2;

import android.net.Uri;
import android.os.Handler;
import android.view.SurfaceView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.video.VideoListener;

import java.io.IOException;

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

public class Exo2MediaPlayerImpl extends BaseMediaPlayerImpl<SimpleExoPlayer, DataSource> {
    long resumePos = -1;
    long logLastTime = -1;
    int initialAudioTrackIndex = -1;

    DefaultTrackSelector trackSelector;

    public Exo2MediaPlayerImpl(AndroidUIController activity) {
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
        long time = player.getCurrentPosition();

        // NOTE: exoplayer will lose it's time after a seek/resume, so this ensures that it
        // will send back the last known server start time plus the player time
        if (pushMode) {
            // happens after a seek
            if (time == Long.MIN_VALUE || time == Long.MAX_VALUE) {
                // the player is adjusting
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                    log.debug("ExoPlayer: getPlayerMediaTimeMillis(): player adjusting (lastServerTime was: {})", toHHMMSS(lastServerTime, true));
                return 0;
            }

            if (time < 0) {
                // player is adjusting, after a push/seek.
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                    log.debug("ExoPlayer: getPlayerMediaTimeMillis(): player adjusting using 0 but lastServerTime was {}", toHHMMSS(lastServerTime, true));
                return 0;
            }

            if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
                if (time != logLastTime) {
                    log.debug("ExoPlayer: getPlayerMediaTimeMillis(): serverTime: {}, time: {}, total: {}", toHHMMSS(lastServerTime, true),
                            toHHMMSS(time, true), toHHMMSS(lastServerTime + time, true));
                }
                logLastTime = time;
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
    public void setSubtitleTrack(int streamPos) {

    }

    @Override
    public void setAudioTrack(int streamPos) {

        if (!ExoIsPlaying()) {
            initialAudioTrackIndex = streamPos;
        } else {
            initialAudioTrackIndex = -1;
            changeTrack(C.TRACK_TYPE_AUDIO, streamPos, 0);
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
        initialAudioTrackIndex = -1;

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
                sageTVurl = "stv://" + context.getClient().getConnectedServerInfo().address + "/" + sageTVurl;
            }
            dataSource = new Exo2PullDataSource(context.getClient().getConnectedServerInfo().address);
        }

        Handler mainHandler = new Handler();

        // See if we need to disable ffmpeg audio decoding
        final boolean preferExtensionDecoders = MiniclientApplication.get().getClient().properties().getBoolean(PrefStore.Keys.disable_audio_passthrough, false);
        @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                        : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context.getContext(), extensionRendererMode);

        trackSelector = new DefaultTrackSelector();
        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

        player.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                        log.debug("Player Has Ended, set EOS");
                    if (playWhenReady)
                        stop();
                    //notifySageTVStop();
                    eos = true;
                    Exo2MediaPlayerImpl.this.state = Exo2MediaPlayerImpl.EOS_STATE;
                }
                if (playbackState == Player.STATE_READY) {
                    //debugAvailableTracks();

                    if (initialAudioTrackIndex != -1) {
                        setAudioTrack(initialAudioTrackIndex);
                    }
                }
            }
        });

        player.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                    log.debug("ExoPlayer.onVideoSizeChanged: {}x{}, pixel ratio: {}", width, height, pixelWidthHeightRatio);

                // note if pixel ratio is != 0 then calc the ar and apply it.
                if (pixelWidthHeightRatio != 0f) {
                    setVideoSize(width, height, pixelWidthHeightRatio * ((float) width / (float) height));
                } else {
                    setVideoSize(width, height, 0);
                }
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
        player.setVideoSurface(((SurfaceView) context.getVideoView()).getHolder().getSurface());
        player.setPlayWhenReady(true);

        if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("Video Player is online");
        playerReady = true;
        state = PLAY_STATE;
    }


    public void changeTrack(int trackType, int groupIndex, int trackIndex) {
        if (trackSelector == null) return;

        DefaultTrackSelector.SelectionOverride override;
        DefaultTrackSelector.ParametersBuilder parametersBuilder;
        MappingTrackSelector.MappedTrackInfo trackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (trackInfo == null) return;

        try {

            TrackGroupArray trackGroup = trackInfo.getTrackGroups(trackType);

            if (trackInfo.getTrackSupport(trackType, groupIndex, trackIndex) == RendererCapabilities.FORMAT_HANDLED) {
                //Clear set new track selection
                parametersBuilder = trackSelector.buildUponParameters();

                override = new DefaultTrackSelector.SelectionOverride(groupIndex, trackIndex);
                parametersBuilder.setSelectionOverride(trackType, trackGroup, override);

                trackSelector.setParameters(parametersBuilder);
            } else {
                log.debug("ExoPlayer is unable to render the track, TrackType {}, GroupIndex {}, TrackIndex {}", trackType, groupIndex, trackIndex);
            }
        } catch (Exception ex) {
            log.debug("ExoPlayer error changing track, TrackType {}, GroupIndex {}, TrackIndex {}", trackType, groupIndex, trackIndex);
        }
    }

    public void debugAvailableTracks() {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();


        if (mappedTrackInfo == null) {
            log.debug("JVL - No Mapped Track Info");
            return;
        }

        for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);

            log.debug("JVL - Track Render Group {}", i);

            if (trackGroups.length != 0) {
                int label;

                switch (player.getRendererType(i)) {
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

                for (int j = 0; j < trackGroups.length; j++) {
                    log.debug("\t Track Group {}", j);

                    for (int k = 0; k < trackGroups.get(j).length; k++) {
                        log.debug("\t\t Track {}, Channels {}, Bitrate {}, Language {}", k, trackGroups.get(j).getFormat(k).channelCount, trackGroups.get(j).getFormat(k).bitrate, trackGroups.get(j).getFormat(k).language);
                        if (mappedTrackInfo.getTrackSupport(i, j, k) == RendererCapabilities.FORMAT_HANDLED) {
                            //Add debug info
                            log.debug("\t\t Format is handled");
                        } else {
                            //Add debug info
                            log.debug("\t\t Format IS NOT HANDLED");
                        }
                    }

                }

            } else {
                log.debug("JVL - Track Group Empty");
            }
        }

    }

}
