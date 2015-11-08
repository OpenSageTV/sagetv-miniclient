package sagex.miniclient.android.video;

import android.widget.Toast;

import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.httpbridge.PullBufferDataSource;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by seans on 06/10/15.
 */
public class IJKMediaPlayerImpl extends DataSourceMediaPlayerImpl<IjkMediaPlayer> {

    long preSeekPos = -1;

    public IJKMediaPlayerImpl(MiniClientGDXActivity activity) {
        super(activity, true);
    }

    @Override
    public long getMediaTimeMillis() {
        if (player == null) return 0;
        return player.getCurrentPosition();
    }

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public void pause() {
        log.debug("pause()");
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    @Override
    public void play() {
        log.debug("play()");
        if (player != null && !player.isPlaying()) {
            player.start();
        }
    }

    @Override
    public void flush() {
        super.flush();
        if (httpBridge.hasDataSource()) {
            getDataSource().flush();
            log.debug("Seek1");
            player.seekTo(Long.MAX_VALUE);
        }
    }

    @Override
    public long getLastFileReadPos() {
        if (player == null) return 0;
        return player.getCurrentPosition();
    }

    protected void setupPlayer(String url) {
        log.debug("Creating Player");
        releasePlayer();
        try {
            if (player == null) {
                player = new IjkMediaPlayer();
            }
            player.setLogEnabled(true);
            //IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_WARN);
            player.setDisplay(mSurface.getHolder());

            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1); // enable hardware acceleration
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
            //player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
            //player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
            //player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");



            player.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int what, int extra) {
                    log.error("IjkPlayer ERROR: {}, {}", what, extra);
                    return false;
                }
            });

            log.debug("Sending URL to mediaplayer");
            player.setDataSource(url);
            // player.setDataSource("/sdcard/Movies/twd1.mp4");
            // player.setDataSource("http://192.168.1.176:8000/twd1.mp4");

            if (!pushMode && preSeekPos != -1) {
                player.seekTo(preSeekPos);
                preSeekPos = -1;
            }

            player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {
                    playerReady = true;
                }
            });
            player.prepareAsync();
            player.start();
            log.debug("mediaplayer has our URL");
        } catch (Exception e) {
            Toast.makeText(context, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void seek(long maxValue) {
        log.debug("SEEK: {}", maxValue);
        if (player == null) {
            preSeekPos = maxValue;
            return;
        }
        if (getDataSource() instanceof PullBufferDataSource) {
            player.seekTo(maxValue);
        } else {
            getDataSource().flush();
        }
    }

    protected void releasePlayer() {
        if (player == null)
            return;
        log.debug("Releasing Player");
        try {
            if (player.isPlaying()) {
                player.pause();
                player.stop();
            }
            player.reset();
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
}
