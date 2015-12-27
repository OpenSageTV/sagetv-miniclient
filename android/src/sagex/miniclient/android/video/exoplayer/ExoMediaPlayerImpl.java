package sagex.miniclient.android.video.exoplayer;


import android.net.Uri;

import com.google.android.exoplayer.demo.EventLogger;
import com.google.android.exoplayer.demo.player.DemoPlayer;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.util.VerboseLogUtil;

import java.io.IOException;

import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.net.HasPushBuffer;

/**
 * Created by seans on 27/09/15.
 */
public class ExoMediaPlayerImpl extends BaseMediaPlayerImpl<DemoPlayer, DataSource> {
    public ExoMediaPlayerImpl(MiniClientGDXActivity activity) {
        super(activity, true, false);
    }

    boolean ExoIsPlaying() {
        return player.getPlayWhenReady();
    }

    void ExoPause() {
        player.setPlayWhenReady(false);
    }

    void ExoStart() {
        player.setPlayWhenReady(true);
    }

    long resumePos = -1;

    protected void releasePlayer() {
        if (player == null)
            return;
        log.debug("Releasing Player");
        try {
            if (ExoIsPlaying()) {
                ExoPause();
            }
            //player.reset();
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
    protected void releaseDataSource() {
        if (dataSource != null) {
            try {
                if (dataSource instanceof HasPushBuffer) {
                    ((HasPushBuffer) dataSource).release();
                } else {
                    dataSource.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataSource = null;
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

        log.debug("Setting up the media player: {}", sageTVurl);

        if (pushMode) {
            dataSource = new ExoPushDataSource();
        } else {
            dataSource = new ExoPullDataSource();
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
        player = new DemoPlayer(rendererBuilder);
        EventLogger eventLogger = new EventLogger();
        player.setInternalErrorListener(eventLogger);
        player.setInfoListener(eventLogger);
        player.addListener(eventLogger);

        player.setBackgrounded(false);
        if (resumePos >= 0) {
            log.debug("Resume Seek Postion: {}", resumePos);
            player.seekTo(resumePos);
            resumePos = -1;
        } else {
            //player.seekTo(0);
        }
        player.prepare();

        // start playing
        player.setSurface(mSurface.getHolder().getSurface());
        player.setPlayWhenReady(true);

        log.debug("Video Player is online");
        playerReady = true;
        state = LOADED_STATE;
    }

    @Override
    public long getMediaTimeMillis() {
        long time = player.getCurrentPosition();
        log.debug("getMediaTimeMillis(): {}", time);
        return time;
    }

    @Override
    public void stop() {
        log.debug("stop()");
        if (playerReady) {
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
                    log.debug("Seek Resume(Player is Null) {}", timeInMS);
                    resumePos = timeInMS;
                }
            }
        } else {
            log.debug("Seek Resume {}", timeInMS);
            resumePos = timeInMS;
        }
    }
}
