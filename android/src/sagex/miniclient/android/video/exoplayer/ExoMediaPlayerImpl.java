package sagex.miniclient.android.video.exoplayer;


import android.net.Uri;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.demo.EventLogger;
import com.google.android.exoplayer.demo.player.DemoPlayer;
import com.google.android.exoplayer.demo.player.SageTVPlayer;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.util.VerboseLogUtil;

import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.prefs.PrefStore.Keys;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.util.VerboseLogging;

/**
 * Created by seans on 27/09/15.
 */
public class ExoMediaPlayerImpl extends BaseMediaPlayerImpl<DemoPlayer, DataSource> {
    public ExoMediaPlayerImpl(MiniClientGDXActivity activity) {
        super(activity, true, false);
    }

    boolean ExoIsPlaying() {
        if (player==null) return false;
        return player.getPlayWhenReady();
    }

    void ExoPause() {
        if (player==null) return;
        player.setPlayWhenReady(false);
    }

    void ExoStart() {
        if (player==null) return;
        player.setPlayWhenReady(true);
    }

    long resumePos = -1;

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
        log.debug("getVideoDimensions");
        if (player != null) {
            if (player.getFormat() != null) {
                Dimension d = new Dimension(player.getFormat().width, player.getFormat().height);
                if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("getVideoSize(): {}", d);
                return d;
            } else {
                log.debug("getVideoDimensions: player.getFormat is null");
            }
        } else {
            log.debug("getVideoDimensions: player is null");
        }
        return null;
    }

    /**
     * Needs to run on the UI thread
     *
     * @param sageTVurl
     */
    protected void setupPlayer(String sageTVurl) {
        if (player != null) {
            releasePlayer();
        }

        VerboseLogUtil.setEnableAllTags(true);

        if (VerboseLogging.DETAILED_PLAYER_LOGGING)
            log.debug("Setting up the media player: {}", sageTVurl);

        if (pushMode) {
            dataSource = new ExoPushDataSource();
        } else {
            dataSource = new ExoPullDataSource();
        }
        if (pushMode && context.getClient().properties().getBoolean(Keys.pts_seek_hack, false)) {
            // note required adjustments to ExoPlayer
            log.debug("pts seek hack enabled");
            System.setProperty(Keys.pts_seek_hack, "true");
        } else {
            System.setProperty(Keys.pts_seek_hack, "false");
        }

        // mp4 and mkv will play (but not aac audio)
        // File file = new File("/sdcard/Movies/sample-mkv.mkv");
        // File file = new File("/sdcard/Movies/sample-mp4.mp4");
        // ts will not play
        // File file = new File("/sdcard/Movies/sample-ts.ts");
        // Uri.parse(file.toURI().toString())
        SageTVPlayer.RendererBuilder rendererBuilder = new SageTVExtractorRendererBuilder(context, dataSource, Uri.parse(sageTVurl));
        //SageTVPlayer.RendererBuilder rendererBuilder = new DataSourceExtractorRendererBuilder(context, "sagetv", Uri.parse("http://192.168.1.176:8000/The%20Walking%20Dead%20S05E14%20Spend.mp4"));
        // ExtractorRendererBuilder rendererBuilder = new ExtractorRendererBuilder(context, "sagetv", Uri.parse(file.toURI().toString()));
        player = new SageTVPlayer(rendererBuilder);
        EventLogger eventLogger = new EventLogger();
        player.setInternalErrorListener(eventLogger);
        player.setInfoListener(eventLogger);
        player.addListener(eventLogger);
        player.addListener(new DemoPlayer.Listener() {
            @Override
            public void onStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                        log.debug("Player Has Ended, set EOS");
                    stop();
                    notifySageTVStop();
                    ExoMediaPlayerImpl.this.state = ExoMediaPlayerImpl.EOS_STATE;
                }
            }

            @Override
            public void onError(Exception e) {
                playerFailed();
            }

            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                    log.debug("ExoPlayer.onVideoSizeChanged: {}x{}, ratio: {}", width, height, pixelWidthHeightRatio);
                setVideoSize(width, height);
            }
        });

        player.setBackgrounded(false);
        if (resumePos >= 0) {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                log.debug("Resume Seek Postion: {}", resumePos);
            player.seekTo(resumePos);
            resumePos = -1;
        } else {
            //player.seekTo(0);
        }
        player.prepare();

        // start playing
        player.setSurface(context.getVideoView().getHolder().getSurface());
        player.setPlayWhenReady(true);

        if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("Video Player is online");
        playerReady = true;
        state = PLAY_STATE;
    }

    @Override
    public long getPlayerMediaTimeMillis() {
        if (player==null) return 0;
        long time = player.getCurrentPosition();
        //log.debug("getMediaTimeMillis(): {} - duration: {}", time, player.getDuration());
        //if (pushMode) return -1l;
        return time;
    }

    @Override
    public void stop() {
        super.stop();
        if (playerReady) {
            if (player==null) return;
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
        if (player==null) return;
        ((SageTVPlayer) player).flush();
    }
}
