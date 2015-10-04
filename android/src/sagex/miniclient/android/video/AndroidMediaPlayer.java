package sagex.miniclient.android.video;

import android.media.MediaCodec;
import android.net.Uri;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Rectangle;

/**
 * Created by seans on 27/09/15.
 */
public class AndroidMediaPlayer implements MiniPlayerPlugin {
    private static final Logger log = LoggerFactory.getLogger(AndroidMediaPlayer.class);

    private static final int BUFFER_SIZE = 10 * 1024 * 1024;
    private final MiniClientGDXActivity activityContext;

    ExoPlayer player = null;
    DataSource dataSource = null;
    boolean pushMode = false;
    boolean playerReady = false;

    public AndroidMediaPlayer(MiniClientGDXActivity context) {
        this.activityContext = context;
    }

    @Override
    public void free() {
        log.debug("free()");
    }

    @Override
    public void setPushMode(boolean b) {
        log.debug("setPushMode: " + b);
        this.pushMode = b;
    }

    @Override
    public void load(byte b, byte b1, String s, final String urlString, Object o, boolean usePush, int i) {
        log.debug("load: b:" + b + "; b1:" + b1 + "; s: " + s + "; url:" + urlString + "; o: " + o + "; usePush: " + usePush + "; i: " + i);
        // setup datasource so we can start buffering
        if (pushMode) {
            dataSource = new PushBufferMediaDataSource(Uri.parse(urlString));
        } else {
            dataSource = new PullBufferMediaDataSource(Uri.parse(urlString));
        }
        activityContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setupMediaPlayer(Uri.parse(urlString));
            }
        });
    }

    /**
     * Needs to run on the UI thread
     *
     * @param uri
     */
    private void setupMediaPlayer(Uri uri) {
        if (player == null) {
            playerReady = false;

            log.debug("Setting up the media player");

            // Build the video and audio renderers.
            // by setting null for the extractors, then Exo will attempt to use all the known extractors
            // which is Ts, Mp4, Mp3, etc
            ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, BUFFER_SIZE, null);
            MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);

            player = ExoPlayer.Factory.newInstance(2, 1000, 5000);
            player.prepare(videoRenderer, audioRenderer);
            player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, activityContext.getVideoSurface());
            log.debug("Video Player is online");

            playerReady = true;
        }
    }

    @Override
    public long getMediaTimeMillis() {
        log.debug("getMediaTimeMillis()");
        return player.getCurrentPosition();
    }

    @Override
    public int getState() {
        log.debug("getState()");
        return 0;
    }

    @Override
    public void setMute(boolean b) {
        log.debug("setMute(): " + b);
    }

    @Override
    public void stop() {
        log.debug("stop()");
        player.stop();
    }

    /**
     * Waits until the player has been contructed
     */
    void waitForPlayer() {
        while (!playerReady) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void pause() {
        log.debug("pause()");
        waitForPlayer();
        log.debug("pause(): have player");
        player.setPlayWhenReady(false);
    }

    @Override
    public void play() {
        log.debug("play()");
        waitForPlayer();
        player.setPlayWhenReady(true);
    }

    @Override
    public void seek(long maxValue) {
        log.debug("seek(): " + maxValue);
    }

    @Override
    public void inactiveFile() {
        log.debug("inactivateFile()");
    }

    @Override
    public long getLastFileReadPos() {
        log.debug("getLastFileReadPos()");
        return 0;
    }

    @Override
    public int getVolume() {
        log.debug("getVolume()");
        return 0;
    }

    @Override
    public int setVolume(float v) {
        log.debug("setVolume(): " + v);
        return 0;
    }

    @Override
    public void setVideoRectangles(Rectangle srcRect, Rectangle destRect, boolean b) {
        log.debug("setVideoRectangles(): " + srcRect + ", " + destRect + ", " + b);
    }

    @Override
    public Dimension getVideoDimensions() {
        log.debug("getVideoDimensions()");
        return new Dimension(1280, 720);
    }

    /**
     * Used only if PUSH is being used
     *
     * @param cmddata
     * @param bufDataOffset
     * @param buffSize
     * @throws IOException
     */
    @Override
    public void pushData(byte[] cmddata, int bufDataOffset, int buffSize) throws IOException {
        log.debug("push data: offset: {}, size: {}", bufDataOffset, buffSize);
        if (dataSource instanceof PushBufferMediaDataSource) {
            ((PushBufferMediaDataSource) dataSource).pushBytes(cmddata, bufDataOffset, buffSize);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void run() {
        log.debug("run()");
    }
}
