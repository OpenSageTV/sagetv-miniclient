package sagex.miniclient.android.video.ijkplayer;

import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.prefs.PrefStore.Keys;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.util.VerboseLogging;
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
    public long getPlayerMediaTimeMillis() {
        if (player == null) return 0;
        return player.getCurrentPosition();
    }

    @Override
    public void stop() {
        super.stop();
        if (player==null) return;
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

    @Override
    public Dimension getVideoDimensions() {
        if (player != null) {
            Dimension d = new Dimension(player.getVideoWidth(), player.getVideoHeight());
            if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("getVideoSize(): {}", d);
            return d;
        }
        return null;
    }

    protected void setupPlayer(String sageTVurl) {
        log.debug("Creating Player");
        releasePlayer();
        try {
            if (player == null) {
                player = new IjkMediaPlayer();
                //player = new IjkExoMediaPlayer(context);
            }
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_INFO);

            player.setDisplay(context.getVideoView().getHolder());

            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-avc", 1); // enable hardware acceleration
            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1); // enable hardware acceleration
            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);

            if (pushMode) {
                // in push mode, ijk will reset timestamps on media to 0, so we need to use return
                // unadjusted time.
                if (MiniclientApplication.get().getClient().properties().getBoolean(Keys.pts_seek_hack, false)) {
                    log.debug("pts seek hack enabled");
                    ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "no-time-adjust", 1);
                }
            }

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

            player.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height, int i2, int i3) {
                    log.debug("IJKPlayer.onVideoSizeChanged: {}x{}, {},{}", width, height, i2, i3);
                    setVideoSize(width, height);
                }
            });

            player.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int what, int extra) {
                    log.error("IjkPlayer ERROR: {}, {}", what, extra);
                    playerFailed();
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

            player.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer iMediaPlayer) {
                    log.debug("MEDIA COMPLETE");
                    stop();
                    notifySageTVStop();
                    state = EOS_STATE;
                }
            });

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
                        if (MiniclientApplication.get().getClient().properties().getBoolean(PrefStore.Keys.announce_software_decoder, false)) {
                            if (!"mediacodec".equalsIgnoreCase(mi.mVideoDecoder)) {
                                message("Using Software Decoder (" + (pushMode ? "PUSH MODE" : "PULL MODE") + ")");
                            } else {
                                //message("Using Hardware Decoder");
                            }
                        }
                    }
                    state = PLAY_STATE;
                }
            });
            player.prepareAsync();
            log.debug("mediaplayer has our URL");
        } catch (Exception e) {
            log.error("Failed to create player", e);
            playerFailed();
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
