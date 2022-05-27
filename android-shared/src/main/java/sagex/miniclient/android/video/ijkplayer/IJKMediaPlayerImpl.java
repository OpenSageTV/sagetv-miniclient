package sagex.miniclient.android.video.ijkplayer;

import android.media.session.PlaybackState;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.SurfaceView;

import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.ui.AndroidUIController;
import sagex.miniclient.android.util.AudioUtil;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.android.video.MediaSessionCallbackHandler;
import sagex.miniclient.media.SubtitleTrack;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.util.VerboseLogging;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.MediaInfo;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;


import static sagex.miniclient.util.Utils.toHHMMSS;

/**
 * Created by seans on 06/10/15.
 */
public class IJKMediaPlayerImpl extends BaseMediaPlayerImpl<IMediaPlayer, IMediaDataSource>
{
    long preSeekPos = -1;
    long resumeTimeOffset = -1;
    boolean resumeMode = false;
    int initialAudioStreamPos = -1;
    int initialTextStreamPos = -1;
    long logTime = -1;
    MediaSessionCompat mediaSession;

    public IJKMediaPlayerImpl(AndroidUIController activity)
    {
        super(activity, true, true);
    }

    /**
     * @param serverStartTime
     * @return
     */
    @Override
    public long getPlayerMediaTimeMillis(long serverStartTime)
    {
        long time = player.getCurrentPosition();

        if (pushMode)
        {
            // we haven't determined the "resume" time yet
            if (resumeTimeOffset < 0)
            {
                if (serverStartTime < 500)
                {
                    // this is start from beginning
                    resumeMode = false;
                    resumeTimeOffset = 0;
                }
                else
                {
                    // IJK's getMediaTime() is a little quirky for PS/TS streams.  Basically, when you start
                    // from 0, then seeking works fine.  But, when you resume from some other time, the
                    // players internal clock is reset to 0.  So, the stream plays at the right position, but
                    // the timescale is reset to 0.  For that reason, when pushMode is enabled, you can follow
                    // the logic to see what has to happen to ensure the correct media time.
                    resumeMode = true;
                    resumeTimeOffset = serverStartTime;
                    log.debug("IJK: getMediaTime(): RESUME from {}", resumeTimeOffset);
                }
            }

            if (time < 0)
            {
                // player is adjusting, after a push/seek.
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                {
                    log.debug("IJK: getPlayerMediaTimeMillis(): player adjusting using 0 but time was {}", toHHMMSS(time, true));
                }

                return time;
            }

            if (resumeMode)
            {
                // when in resume mode, you go back before the start of the resume, player time
                // seems to do a PTS rollover of sorts
                long realTime = time;

                if (time + resumeTimeOffset > PTS_ROLLOVER)
                {
                    // need to adjust the time
                    time = time - PTS_ROLLOVER;
                }
                time = time + resumeTimeOffset;

                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                {
                    if (logTime != realTime / 1000)
                    {
                        logTime = realTime / 1000;
                        log.debug("IJK: getPlayerMediaTimeMillis(): resume: {}, player time: {}, total: {}", toHHMMSS(resumeTimeOffset, true), toHHMMSS(realTime, true), toHHMMSS(time, true));
                    }
                }
            }
            else
            {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                {
                    if (logTime != time / 1000)
                    {
                        logTime = time / 1000;
                        log.debug("IJK: getPlayerMediaTimeMillis(): time: {}", toHHMMSS(time, true));
                    }
                }
            }
        }


        return time;
    }

    @Override
    public void stop()
    {
        if(mediaSession != null)
        {
            mediaSession.setActive(false);
        }

        if (player == null) return;

        if (player.isPlaying())
        {
            player.stop();
            //this.releasePlayer();
        }
        super.stop();
    }

    @Override
    public void pause()
    {


        if (state == PAUSE_STATE && !pushMode)
        {
            log.debug("In pause state.  Seek frame instead...");
            //TODO: Could not find the framerate in IJKPlayer.  Going to assume 30fps for now.
            this.seek(player.getCurrentPosition() + 1000);
            return;
        }

        super.pause();

        if (player != null && player.isPlaying())
        {
            player.pause();
        }

        updateMediaSessionPlaybackState(player.getCurrentPosition());
    }

    @Override
    public void play()
    {
        super.play();

        if (player != null && !player.isPlaying())
        {
            player.start();
            updateMediaSessionPlaybackState(player.getCurrentPosition());
        }
    }

    @Override
    public void flush()
    {
        super.flush();

        if (player != null)
        {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
            {
                log.debug("Flush Will force a seek to clear buffers");
            }

            seekToImpl(Long.MAX_VALUE);
        }
    }

    @Override
    public Dimension getVideoDimensions()
    {
        if (player != null)
        {
            Dimension d = new Dimension(player.getVideoWidth(), player.getVideoHeight());
            if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("getVideoSize(): {}", d);
            return d;
        }
        return null;
    }

    protected void setupPlayer(String sageTVurl)
    {
        log.debug("Creating Player");

        preSeekPos = -1;
        resumeTimeOffset = -1;
        resumeMode = false;
        initialAudioStreamPos = -1;
        initialTextStreamPos = -1;
        logTime = -1;

        releasePlayer();
        try
        {
            if (player == null)
            {
                player = new IjkMediaPlayer();
                ((IjkMediaPlayer) player).setOnMediaCodecSelectListener(CodecSelector.sInstance);
            }
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_ERROR);

            player.setDisplay(((SurfaceView) context.getVideoView()).getHolder());

            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-avc", 1); // enable hardware acceleration
            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1); // enable hardware acceleration
            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-mpeg2", 1); // enable hardware acceleration
            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);

            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");

            // setting this to 0 removes the pixelization for mpeg2 videos
            ((IjkMediaPlayer) player).setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);

            player.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener()
            {
                @Override
                public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height, int sarNum, int sarDen)
                {
                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                    {
                        log.debug("IJKPlayer.onVideoSizeChanged: {}x{}, {},{}", width, height, sarNum, sarDen);
                    }
                    setVideoSize(width, height, sarNum, sarDen);
                }
            });


            player.setOnErrorListener(new IMediaPlayer.OnErrorListener()
            {
                @Override
                public boolean onError(IMediaPlayer mp, int what, int extra)
                {
                    log.error("IjkPlayer onERROR: {}, {}", what, extra);
                    playerFailed();
                    return false;
                }
            });


            log.debug("Sending {} to mediaplayer", sageTVurl);

            if (pushMode)
            {
                log.info("Playing URL {} PUSH mode", sageTVurl);
                dataSource = new IJKPushMediaSource();
                ((IJKPushMediaSource) dataSource).open(sageTVurl);
                player.setDataSource(dataSource);
            }
            else
            {
                if (httpls)
                {
                    log.info("Playing URL Using HTTPL: isPush:{}, sageTVUrl: {}", pushMode, sageTVurl);
                    player.setDataSource(sageTVurl);
                }
                else
                {
                    log.info("Playing URL Using DataSource: isPush:{}, sageTVUrl: {}", pushMode, sageTVurl);
                    dataSource = new IJKPullMediaSource(MiniclientApplication.get().getClient().getConnectedServerInfo().address);
                    ((IJKPullMediaSource) dataSource).open(sageTVurl);
                    player.setDataSource(dataSource);
                }
            }

            player.setOnCompletionListener(new IMediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(IMediaPlayer iMediaPlayer)
                {
                    if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("MEDIA COMPLETE");
                    log.debug("OnCompletionListener fired.  Stoping playback and setting state to EOS");
                    stop();
                    state = EOS_STATE;
                    eos = true;
                }
            });

            player.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener()
            {
                @Override
                public void onSeekComplete(IMediaPlayer iMediaPlayer)
                {
                    seekPending = false;
                    updateMediaSessionPlaybackState(player.getCurrentPosition());
                }
            });

            player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener()
            {
                @Override
                public void onPrepared(IMediaPlayer mp)
                {
                    playerReady = true;
                    player.start();
                    state = PLAY_STATE;

                    if (!pushMode)
                    {
                        if (preSeekPos != -1)
                        {
                            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                                log.debug("Resuming At Position: {}", preSeekPos);
                            seekToImpl(preSeekPos);
                            preSeekPos = -1;
                        }
                        else
                        {
                            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                                log.debug("No Resume");
                        }
                    }

                    if (MiniclientApplication.get().getClient().properties().getBoolean(PrefStore.Keys.announce_software_decoder, false))
                    {
                        MediaInfo mi = player.getMediaInfo();
                        if (mi != null)
                        {
                            log.info("MEDIAINFO: video: {},{}", mi.mVideoDecoder, mi.mVideoDecoderImpl);
                            if (!"mediacodec".equalsIgnoreCase(mi.mVideoDecoder))
                            {
                                message("Using Software Decoder (" + (pushMode ? "PUSH MODE" : "PULL MODE") + ")");
                            }
                        }
                    }

                    //Create Media Session
                    IJKMediaPlayerImpl.this.mediaSession = new MediaSessionCompat(IJKMediaPlayerImpl.this.context.getContext(), "SageTV Android TV Client");
                    mediaSession.setCallback(new MediaSessionCallbackHandler(IJKMediaPlayerImpl.this, context.getClient(), context.getContext()));

                    mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);

                    long duration;

                    if(player.getDuration() < 0)
                    {
                        duration = -1;
                    }
                    else
                    {
                        duration = player.getDuration();
                    }
                    //Library files start with stc:// but do not have push in it
                    //Live TV has push: with a lot of other data in it

                    setMediaSessionMetadata(sageTVurl, duration);
                    mediaSession.setActive(true);

                    updateMediaSessionPlaybackState(player.getCurrentPosition());

                    if (initialAudioStreamPos != -1)
                    {
                        setAudioTrack(initialAudioStreamPos);
                    }
                }
            });

            player.prepareAsync();
            log.debug("mediaplayer has our URL");
        }
        catch (Exception e)
        {
            log.error("Failed to create player", e);
            playerFailed();
        }
    }

    protected void seekToImpl(long timeInMillis)
    {
        seekPending = true;
        player.seekTo(timeInMillis);
    }

    @Override
    public void seek(long timeInMS)
    {
        super.seek(timeInMS);
        if (player == null || state == NO_STATE || state == LOADED_STATE)
        {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
            {
                log.debug("Setting Pre-Seek {}", timeInMS);
            }
            preSeekPos = timeInMS;
            return;
        }

        if (!pushMode)
        {
            if (player.isPlaying() || state == PAUSE_STATE || state == PLAY_STATE)
            {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                {
                    log.debug("Immediate Seek {}", timeInMS);
                }
                seekToImpl(timeInMS);
            }
            else
            {
                log.info("We Missed a Seek for {}: player.isPlaying {}; State: {}; playerReader: {}", timeInMS, player.isPlaying(), state, playerReady);
            }
        }
    }

    /**
     * This finds the correct track position in the IJKPlayer track list.  SageTV gives us a zero based index of all audio tracks.  IJKPlayer
     * uses a track position of all tracks in the file.  Video is is track 0.  If it was an audio only file, I assume track 0 would be the audio
     *
     * @param sageTVPosition Track position sage has requested us to change to
     * @return IJKPlayer track position
     */
    private int getAudioTrackPosition(int sageTVPosition)
    {
        if (player == null) return -1;

        ITrackInfo info[] = player.getTrackInfo();
        if (info == null || info.length == 0) return -1;

        int audioTrackCount = 0;

        for (int i = 0; i < info.length; i++)
        {
            if (info[i] != null && info[i].getTrackType() == IjkTrackInfo.MEDIA_TRACK_TYPE_AUDIO)
            {
                if (audioTrackCount == sageTVPosition)
                {
                    return i;
                }

                audioTrackCount++;
            }
        }

        return -1;
    }

    /**
     * This finds the correct track position in the IJKPlayer track list.  SageTV gives us a zero based index of all aubtitle tracks.  IJKPlayer
     * uses a track position of all tracks in the file.  Video is is track 0.  If it was an audio only file, I assume track 0 would be the audio
     *
     * @param sageTVPosition Track position sage has requested us to change to
     * @return IJKPlayer track position
     */
    private int getSubtitleTrackPosition(int sageTVPosition)
    {
        if (player == null) return -1;

        ITrackInfo info[] = player.getTrackInfo();
        if (info == null || info.length == 0) return -1;

        int subtitleTrackCount = 0;

        for (int i = 0; i < info.length; i++)
        {
            if (info[i] == null) continue;

            log.debug("Track Pos {}, TrackType {}, Track Info {}, Track Lang ", i, info[i].getTrackType(), info[i].getInfoInline(), info[i].getLanguage());

            if (info[i].getTrackType() == IjkTrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT)
            {
                if (subtitleTrackCount == sageTVPosition)
                {
                    return i;
                }

                subtitleTrackCount++;
            }
        }

        return -1;
    }

    @Override
    public void setAudioTrack(int streamPos)
    {
        //NOTE: Do not try to set the track position to a currently selected audio track. IJKPlayer really does not like that, and will crash.

        log.debug("setAudioTrack Called StreamPosition: {}", streamPos);

        if (playerReady)
        {
            int currentTrack = ((IjkMediaPlayer) player).getSelectedTrack(IjkTrackInfo.MEDIA_TRACK_TYPE_AUDIO);
            int setTrackTo = this.getAudioTrackPosition(streamPos);

            log.debug("Selected Audio Track Pos: {}", currentTrack);

            if (setTrackTo == -1)
            {
                log.warn("Unable to find audio track postion in IJKPlayer!");
                return;
            }

            if (currentTrack != setTrackTo)
            {
                log.debug("Setting audio track to IJKPlayer Track Position: {}", setTrackTo);
                ((IjkMediaPlayer) player).selectTrack(setTrackTo);
            }

        }
        else
        {
            log.debug("setAudioTrack player not ready.  Storing values for setting when player is initialized");

            this.initialAudioStreamPos = streamPos;
        }
    }

    @Override
    public int getSubtitleTrackCount()
    {
        return 0;
    }

    @Override
    public SubtitleTrack[] getSubtitleTracks()
    {
        return new SubtitleTrack[0];
    }

    @Override
    public void setSubtitleTrack(int streamPos)
    {
        //Displaying subtitle/timedtext does not appear to be supported at this time.
        log.debug("TODO: setSubtitleTrack Called StreamPosition: {}", streamPos);

        if (player == null)
        {
            this.initialTextStreamPos = streamPos;
        }
        else
        {
            // NOT IMPLEMENTED YET, let's comment out the code until it is
            // since it's causing problems in the playback.
//            int currentTrack = ((IjkMediaPlayer) player).getSelectedTrack(IjkTrackInfo.MEDIA_TRACK_TYPE_SUBTITLE);
//            int trackPos = this.getSubtitleTrackPosition(streamPos);
//
//            if (playerReady && currentTrack != trackPos && trackPos != -1) {
//                log.debug("FUNCTION NOT SUPPORTED (Setting subtitle to IJKPosition): {}", trackPos);
//                //((IjkMediaPlayer) player).selectTrack(trackPos);
//            }
        }
    }

    @Override
    public int getSelectedSubtitleTrack()
    {
        return DISABLE_TRACK;
    }

    protected void releasePlayer()
    {
        if(mediaSession != null)
        {
            log.debug("Media Session RELEASE");
            mediaSession.setActive(false);
            mediaSession.release();
            mediaSession = null;
        }

        if (player == null)
            return;
        log.debug("Releasing Player");

        try
        {
            try
            {
                if (player.isPlaying())
                {
                    try
                    {
                        player.pause();
                    }
                    catch (Throwable t)
                    {
                    }
                    try
                    {
                        player.stop();
                    }
                    catch (Throwable t)
                    {
                    }
                }
            }
            catch (Throwable t)
            {
            }

            // now release the datasource
            // https://github.com/OpenSageTV/sagetv-miniclient/issues/54
            try
            {
                releaseDataSource();
            }
            catch (Throwable t)
            {
            }

            try
            {
                player.reset();
            }
            catch (Throwable t)
            {
            }
            log.debug("Player Is Stopped");
        }
        catch (Throwable t)
        {
        }


        try
        {
            player.release();
        }
        catch (Throwable t)
        {
        }

        try
        {
            clearSurface();
        }
        catch (Throwable t)
        {
        }
        player = null;

        super.releasePlayer();
    }

    private void updateMediaSessionPlaybackState(long playbackPostion)
    {
        if(mediaSession != null)
        {
            PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
            stateBuilder.setActions(this.getMediaSessionActions());

            if (player != null && getState() == PLAY_STATE)
            {
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, playbackPostion, 1.0f);
            }
            else
            {
                stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
            }


            mediaSession.setPlaybackState(stateBuilder.build());
        }
    }

    private long getMediaSessionActions()
    {
        long actions = 0;

        if(player != null)
        {
            if (getState() == PLAY_STATE)
            {
                actions = PlaybackState.ACTION_STOP;
                actions |= PlaybackState.ACTION_PAUSE;
                actions |= PlaybackState.ACTION_FAST_FORWARD;
                actions |= PlaybackState.ACTION_REWIND;
                actions |= PlaybackState.ACTION_SEEK_TO;
                actions |= PlaybackState.ACTION_SKIP_TO_NEXT;
                actions |= PlaybackState.ACTION_SKIP_TO_PREVIOUS;
            }
            else
            {
                actions = PlaybackState.ACTION_PLAY;
            }

        }

        return actions;
    }

    private void setMediaSessionMetadata(String displayTitle, long duration)
    {
        MediaMetadataCompat.Builder metaDataBuilder = new MediaMetadataCompat.Builder();

        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, displayTitle);
        metaDataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);
        mediaSession.setMetadata(metaDataBuilder.build());
    }
}

