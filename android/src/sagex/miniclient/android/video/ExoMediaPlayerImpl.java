package sagex.miniclient.android.video;


import android.media.MediaCodec;
import android.net.Uri;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.android.gdx.MiniClientGDXActivity;

/**
 * Created by seans on 27/09/15.
 */
public class ExoMediaPlayerImpl extends DataSourceMediaPlayerImpl<ExoPlayer> {
    private static final Logger log = LoggerFactory.getLogger(ExoMediaPlayerImpl.class);

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;

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

    protected void releasePlayer() {
        if (player == null)
            return;
        log.debug("Releasing Player");
        try {
            if (ExoIsPlaying()) {
                ExoPause();
                player.stop();
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

    /**
     * Needs to run on the UI thread
     *
     * @param uri
     */
    protected void setupPlayer(String uri) {
        if (player != null) {
            releasePlayer();
        }

        log.debug("Setting up the media player");

        // Build the video and audio renderers.
        // by setting null for the extractors, then Exo will attempt to use all the known extractors
        // which is Ts, Mp4, Mp3, etc

        Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        DataSource dataSource = new DefaultUriDataSource(context, null, "sagetv/miniclient");
        ExtractorSampleSource sampleSource = new ExtractorSampleSource(Uri.parse(uri), dataSource, allocator,
                BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);
        MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context, sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);

        player = ExoPlayer.Factory.newInstance(2, 1000, 5000);
        player.addListener(new ExoPlayer.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                log.debug("ExoPlayer: playerWhenReady: {}, PlayBackState: {}", playWhenReady, playbackState);
                if (playbackState == ExoPlayer.STATE_READY) {
                    log.debug("ExoPlayer: setPlayerReady: true");
                    playerReady = true;
                }
            }

            @Override
            public void onPlayWhenReadyCommitted() {
                log.debug("ExoPlayer: playerWhenReady committed");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                log.debug("ExoPlayer: Error", error);
            }
        });

        player.prepare(videoRenderer, audioRenderer);
        player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, context.getVideoSurface());

        // start playing
        player.setPlayWhenReady(true);

        log.debug("Video Player is online");
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
            player.stop();
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
    public void seek(long bytePos) {
        super.seek(bytePos);
        if (playerReady) {
            player.seekTo(bytePos);
        }
    }

    @Override
    public void flush() {
        super.flush();
        if (playerReady) {
            player.seekTo(Long.MAX_VALUE);
        }
    }
}
