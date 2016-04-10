package sagex.miniclient.android.video.ijkplayer;

import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.prefs.PrefStore;
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
    private static final long PTS_ROLLOVER = 0x200000000L * 1000000L / 90000L / 1000L;
    private static final long TWENTY_HOURS = 20 * 60 * 60 * 1000;
    long preSeekPos = -1;
    long playerGetTimeOffset = -1;
    long lastTime = 0;
    boolean resumeMode = false;
    long lastGetTime = 0;

    public IJKMediaPlayerImpl(MiniClientGDXActivity activity) {
        super(activity, true, true);
    }

    /**
     * IJK's getMediaTime() is a little quirky for PS/TS streams.  Basically, when you start
     * from 0, then seeking works fine.  But, when you resume from some other time, the
     * players internal clock is reset to 0.  So, the stream plays at the right position, but
     * the timescale is reset to 0.  For that reason, when pushMode is enabled, you can follow
     * the logic to see what has to happen to ensure the correct media time.
     *
     * @param serverStartTime
     * @return
     */
    @Override
    public long getPlayerMediaTimeMillis(long serverStartTime) {
        if (player == null) return 0;

        long time = player.getCurrentPosition();

        if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
            if (System.currentTimeMillis() - lastGetTime > 1000) {
                lastGetTime = System.currentTimeMillis();
                if (time + playerGetTimeOffset > PTS_ROLLOVER) {
                    log.debug("IJK: getMediaTime(): Tick Rollver; serverStart: {}, time: {}, real: {}", serverStartTime, time, time + playerGetTimeOffset - PTS_ROLLOVER);
                } else {
                    log.debug("IJK: getMediaTime(): Tick; serverStart: {}, time: {}", serverStartTime, time);
                }
            }
        }

        if (pushMode) {
            if (playerGetTimeOffset < 0) {
                // initial start/resume time that IJK will use as it's time base
                log.debug("IJK: getMediaTime(): Setting initial player offset {}", serverStartTime);
                playerGetTimeOffset = serverStartTime;
                if (serverStartTime < 500) {
                    // this is start from beginning
                    resumeMode = false;
                    playerGetTimeOffset = 0;
                } else {
                    log.debug("IJK: getMediaTime(): RESUME from {}", serverStartTime);
                    resumeMode = true;
                }
            }

            if (time < 0) {
                // push mode seeking, we are adjusting
                // log.debug("IJK: getMediaTime(): seeking/adjusting... {}, serverTime: {}", time, serverStartTime);
                lastTime = time;
                return time;
            }

            if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
                // if the last time <0 and we now have a time, then the seek adjusted
                if (time >= 0 && lastTime < 0) {
                    log.debug("IJK: getMediaTime(): After Seek;  off:{}, time: {}, startStart: {}", playerGetTimeOffset, time, serverStartTime);
                }
            }

            // when in resume mode, you go back before the start of the resume, player time
            // seems to do a PTS rollover of sorts
            if (resumeMode && time + playerGetTimeOffset > PTS_ROLLOVER) {
                // need to adjust the time
                time = time - PTS_ROLLOVER; // ofset will be added at the end
            }

            if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
                // this is just to capture when something possible goes wrong and we
                if (Math.abs(lastTime - time) > TWENTY_HOURS) {
                    log.debug("IJK: big jump getMediaTime(): off:{}, time:{}, serverStartTime: {}, lastTime: {}", playerGetTimeOffset, time, serverStartTime, lastTime);
                }
            }
            lastTime = time;
        }

        // return the time adjusted by the player's time offset
        return time + ((pushMode) ? playerGetTimeOffset : 0);
    }

    @Override
    public void stop() {
        if (player == null) return;
        if (player.isPlaying()) {
            player.stop();
        }
        super.stop();
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
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
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
        playerGetTimeOffset = -1;
        resumeMode = false;
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
            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-mpeg2", 1); // enable hardware acceleration
            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);

            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);

            // setting this to 0 removes the pixelization for mpeg2 videos
            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);

            // ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
            //((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "seekable", 0);
            //player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");

            player.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height, int i2, int i3) {
                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
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
                dataSource = new IJKPushMediaSource();
                ((IJKPushMediaSource) dataSource).open(sageTVurl);
                player.setDataSource(dataSource);
            } else {
                log.info("Playing URL Using DataSource: isPush:{}, sageTVUrl: {}", pushMode, sageTVurl);
                dataSource = new IJKPullMediaSource();
                ((IJKPullMediaSource) dataSource).open(sageTVurl);
                player.setDataSource(dataSource);
            }

            player.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer iMediaPlayer) {
                    if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("MEDIA COMPLETE");
                    stop();
                    state = EOS_STATE;
                }
            });

            player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {
                    playerReady = true;
                    player.start();
                    state = PLAY_STATE;
                    if (!pushMode) {
                        if (preSeekPos != -1) {
                            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                                log.debug("Resuming At Position: {}", preSeekPos);
                            player.seekTo(preSeekPos);
                            preSeekPos = -1;
                        } else {
                            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                                log.debug("No Resume");
                        }
                    }

                    if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
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
                    }

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
        if (player == null || state == NO_STATE || state == LOADED_STATE) {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
                log.debug("Setting Pre-Seek {}", timeInMS);
            }
            preSeekPos = timeInMS;
            return;
        }

        if (!pushMode) {
            if (player.isPlaying() || state == PAUSE_STATE || state == PLAY_STATE) {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
                    log.debug("Immediate Seek {}", timeInMS);
                }
                player.seekTo(timeInMS);
            } else {
                log.info("We Missed a Seek for {}: player.isPlaying {}; State: {}; playerReader: {}", timeInMS, player.isPlaying(), state, playerReady);
            }
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
