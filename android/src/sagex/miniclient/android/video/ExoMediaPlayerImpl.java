package sagex.miniclient.android.video;


import android.net.Uri;

import com.google.android.exoplayer.demo.EventLogger;
import com.google.android.exoplayer.demo.player.DemoPlayer;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.util.VerboseLogUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.android.video.exoplayer.ExoPullDataSource;
import sagex.miniclient.android.video.exoplayer.ExoPushBufferDataSource;
import sagex.miniclient.android.video.exoplayer.SageTVExtractorRendererBuilder;
import sagex.miniclient.android.video.exoplayer.SageTVPlayer;
import sagex.miniclient.httpbridge.PushBufferDataSource;

/**
 * Created by seans on 27/09/15.
 */
public class ExoMediaPlayerImpl extends DataSourceMediaPlayerImpl<DemoPlayer> {
    private static final Logger log = LoggerFactory.getLogger(ExoMediaPlayerImpl.class);

    DataSource dataSource = null;

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
    public void load(byte b, byte b1, String s, final String urlString, Object o, boolean b2, int i) {
        // we need to use the http bridge
        if (createPlayerOnUI) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    releasePlayer();
                    setupPlayer(urlString);
                }
            });
        } else {
            releasePlayer();
            setupPlayer(urlString);
        }
    }

    /**
     * Needs to run on the UI thread
     *
     * @param uri
     */
    protected void setupPlayer(String uri) {
        if (player != null) {
            releasePlayer();
        }

        VerboseLogUtil.setEnableAllTags(true);

        log.debug("Setting up the media player: {}", uri);

        if (pushMode) {
            this.dataSource = new ExoPushBufferDataSource();
        } else {
            dataSource = new ExoPullDataSource();
        }

        // mp4 and mkv will play (but not aac audio)
        // File file = new File("/sdcard/Movies/sample-mkv.mkv");
        // File file = new File("/sdcard/Movies/sample-mp4.mp4");
        // ts will not play
        // File file = new File("/sdcard/Movies/sample-ts.ts");
        // Uri.parse(file.toURI().toString())
        SageTVPlayer.RendererBuilder rendererBuilder = new SageTVExtractorRendererBuilder(context, dataSource, Uri.parse(uri));
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
    }

    @Override
    public long getMediaTimeMillis() {
        log.debug("getMediaTimeMillis()");
        return player.getCurrentPosition();
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
            if (dataSource instanceof ExoPullDataSource) {
                if (player != null) {
                    player.seekTo(timeInMS);
                } else {
                    log.debug("Seek Resume(Player is Null) {}", timeInMS);
                }
            } else {
                if (dataSource != null) {
                    ((ExoPushBufferDataSource) dataSource).flush();
                }
            }
        } else {
            log.debug("Seek Resume {}", timeInMS);
            resumePos = timeInMS;
        }
    }

    @Override
    public void flush() {
        if (dataSource != null) {
            ((ExoPushBufferDataSource) dataSource).flush();
        }

        if (playerReady) {
            player.seekTo(Long.MAX_VALUE);
        }
    }

    @Override
    public long getLastFileReadPos() {
        if (dataSource == null) return 0;
        return player.getCurrentPosition();
    }

    @Override
    public void pushData(byte[] cmddata, int bufDataOffset, int buffSize) throws IOException {
        while (dataSource == null) {
            log.debug("Push Happened but DataSource is not ready :(");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.interrupted();
                break;
            }
        }
        // log.debug("push data");
        ((ExoPushBufferDataSource) dataSource).pushBytes(cmddata, bufDataOffset, buffSize);
    }

    @Override
    public int getBufferLeft() {
        if (dataSource == null) return PushBufferDataSource.PIPE_SIZE;
        return ((ExoPushBufferDataSource) dataSource).bufferAvailable();
    }

    @Override
    protected sagex.miniclient.httpbridge.DataSource getDataSource() {
        log.error("getDataSource() called", new Exception("getDataSource called"));
        return null;
    }
}
