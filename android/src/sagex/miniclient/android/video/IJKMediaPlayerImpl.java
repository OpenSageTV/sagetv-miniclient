package sagex.miniclient.android.video;

import android.widget.Toast;

import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.httpbridge.PullBufferDataSource;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by seans on 06/10/15.
 */
public class IJKMediaPlayerImpl extends DataSourceMediaPlayerImpl<IMediaPlayer> {

    long preSeekPos = -1;
    private String lastUrl;
    private int flushCount = 0;

    public IJKMediaPlayerImpl(MiniClientGDXActivity activity) {
        super(activity, true, true);
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
            // playing with trying to have the player use a new URL when flushing
//            log.debug("Seek1");
//            //player.seekTo(Long.MAX_VALUE);
//            if (lastUrl!=null) {
//                player.stop();
//                player.reset();
//                String url = lastUrl;
//                if (lastUrl.indexOf("?")==-1) {
//                    url += ("?flush=" + (++flushCount));
//                } else {
//                    url += ("&flush=" + (++flushCount));
//                }
//                try {
//                    player.setDataSource(url);
//                    player.start();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
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
                //player = new IjkExoMediaPlayer(context);
            }
            //IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
            if (player instanceof IjkMediaPlayer) {
                player.setLogEnabled(true);
                IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_WARN);
            }

            player.setDisplay(mSurface.getHolder());

            if (player instanceof IjkMediaPlayer) {
                ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1); // enable hardware acceleration
                ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
                //player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
                ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
                //player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
                //player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");
            }



            player.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int what, int extra) {
                    log.error("IjkPlayer ERROR: {}, {}", what, extra);
                    return false;
                }
            });

            log.debug("Sending URL to mediaplayer");
            player.setDataSource(url);
            this.lastUrl = url;
            // player.setDataSource("/sdcard/Movies/twd1.mp4");
            // player.setDataSource("http://192.168.1.176:8000/twd1.mp4");


            player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {
                    playerReady = true;
                    player.start();
                    if (!pushMode && preSeekPos != -1) {
                        log.debug("Resuming At Position: {}", preSeekPos);
                        player.seekTo(preSeekPos);
                        preSeekPos = -1;
                    }
                }
            });
            player.prepareAsync();
            //player.start();
            log.debug("mediaplayer has our URL");
        } catch (Exception e) {
            Toast.makeText(context, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void seek(long timeInMS) {
        log.debug("SEEK: {}", timeInMS);
        if (player == null) {
            preSeekPos = timeInMS;
            return;
        }
        if (getDataSource() instanceof PullBufferDataSource) {
            player.seekTo(timeInMS);
        } else {
            getDataSource().flush();
        }
    }

    protected void releasePlayer() {
        if (player == null)
            return;
        log.debug("Releasing Player");

        try {
            try {
                if (player.isPlaying()) {
                    try {
                        player.pause();
                    } catch (Throwable t) {
                    }
                    try {
                        player.stop();
                    } catch (Throwable t) {
                    }
                }
            } catch (Throwable t) {
            }
            try {
                player.reset();
            } catch (Throwable t) {
            }
            log.debug("Player Is Stopped");
        } catch (Throwable t) {
        }

        try {
            player.release();
        } catch (Throwable t) {
        }

        try {
            clearSurface();
        } catch (Throwable t) {
        }
        player = null;

        super.releasePlayer();
    }
}
