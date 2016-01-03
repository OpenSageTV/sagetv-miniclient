package sagex.miniclient.android.video.ijkplayer;

import android.widget.Toast;

import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.uibridge.EventRouter;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.MediaInfo;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * Created by seans on 06/10/15.
 */
public class IJKMediaPlayerImpl extends BaseMediaPlayerImpl<IMediaPlayer, IMediaDataSource> {
    long preSeekPos = -1;

    public IJKMediaPlayerImpl(MiniClientGDXActivity activity) {
        super(activity, true, true);
    }

    @Override
    public long getMediaTimeMillis() {
        if (player == null) return 0;
        player.getCurrentPosition();
        return player.getCurrentPosition();
    }

    @Override
    public void stop() {
        super.stop();
        player.stop();
    }

    @Override
    public void pause() {
        super.pause();
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    @Override
    public void play() {
        super.play();
        if (player != null && !player.isPlaying()) {
            player.start();
        }
    }

    @Override
    public void flush() {
        super.flush();
        if (player != null) {
            log.debug("Flush Will force a seek to clear buffers");
            player.seekTo(Long.MAX_VALUE);
        }
    }

    protected void setupPlayer(String sageTVurl) {
        // create player
        log.debug("Creating Player");
        releasePlayer();
        try {
            if (player == null) {
                player = new IjkMediaPlayer();
                //player = new IjkExoMediaPlayer(context);
            }
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_INFO);

            player.setDisplay(mSurface.getHolder());

            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1); // enable hardware acceleration
            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
            //player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
            //((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "seekable", 0);
            //player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
            //player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");

//            ((IjkMediaPlayer) player).setOnMediaCodecSelectListener(new IjkMediaPlayer.OnMediaCodecSelectListener() {
//                @Override
//                public String onMediaCodecSelect(IMediaPlayer iMediaPlayer, String s, int i, int i1) {
//                    log.info("Media Codec Selected is {}, {}, {}", s, i, i1);
//                    return s;
//                }
//            });

            player.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int what, int extra) {
                    log.error("IjkPlayer ERROR: {}, {}", what, extra);
                    stop();
                    releasePlayer();
                    EventRouter.post(MiniclientApplication.get().getClient(), EventRouter.MEDIA_STOP);
                    return false;
                }
            });

            log.debug("Sending {} to mediaplayer", sageTVurl);

            if (pushMode) {
                log.info("Playing URL {} PUSH mode", sageTVurl);
//            player.setDataSource(new File("/sdcard/Movies/sample-ts.ts").toURI().toString());
                dataSource = new IJKPushMediaSource();
                ((IJKPushMediaSource) dataSource).open(sageTVurl);
                player.setDataSource(dataSource);
                // player.setDataSource("/sdcard/Movies/twd1.mp4");
                // player.setDataSource("http://192.168.1.176:8000/twd1.mp4");
            } else {
                log.info("Playing URL Using DataSource: isPush:{}, sageTVUrl: {}", pushMode, sageTVurl);
                dataSource = new IJKPullMediaSource();
                ((IJKPullMediaSource) dataSource).open(sageTVurl);
//            FileMediaSource dataSource = new FileMediaSource(new File("/sdcard/Movies/sagetv-sample-20151222_092326_684.ts"));
                player.setDataSource(dataSource);
            }

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

                    MediaInfo mi = player.getMediaInfo();
                    if (mi != null) {
                        log.info("MEDIAINFO: video: {},{}", mi.mVideoDecoder, mi.mVideoDecoderImpl);
                    }
                }
            });
            player.prepareAsync();
            log.debug("mediaplayer has our URL");
        } catch (Exception e) {
            log.error("Failed to create player", e);
            Toast.makeText(context, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void seek(long timeInMS) {
        super.seek(timeInMS);
        if (player == null) {
            preSeekPos = timeInMS;
            return;
        }

        if (!pushMode) {
            player.seekTo(timeInMS);
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
