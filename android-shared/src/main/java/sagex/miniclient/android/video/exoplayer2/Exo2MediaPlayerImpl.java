package sagex.miniclient.android.video.exoplayer2;

import android.net.Uri;
import android.os.Handler;
import android.view.SurfaceView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegLibrary;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.video.VideoListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.ui.AndroidUIController;
import sagex.miniclient.android.video.BaseMediaPlayerImpl;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.util.Utils;
import sagex.miniclient.util.VerboseLogging;

import static sagex.miniclient.util.Utils.toHHMMSS;

/**
 * Created by seans on 24/09/16.
 */

public class Exo2MediaPlayerImpl extends BaseMediaPlayerImpl<SimpleExoPlayer, DataSource>
{
    ExtractorMediaSource mediaSource;
    long playbackStartPosition = -1;
    int initialAudioTrackIndex = -1;
    long currentPlaybackPosition = 0;
    DefaultTrackSelector trackSelector;
    
    boolean showCaptions = false;
    Thread cueThread = null;
    
    private long cueHideTimer = 0;
    
    //Experiment variables
    long lastLogTime = System.currentTimeMillis();
    long timeAfterFlush = 0;
    boolean needTimeAfterFlush = false;
    
    ReentrantLock playbackTimeLock;
    
    public Exo2MediaPlayerImpl(AndroidUIController activity)
    {
        super(activity, true, false);
        playbackTimeLock = new ReentrantLock();
        
        
    }
    
    boolean ExoIsPlaying()
    {
        if (player == null)
        {
            return false;
        }
        return player.getPlayWhenReady();
    }
    
    void ExoPause()
    {
        if (player == null)
        {
            return;
        }
        player.setPlayWhenReady(false);
    }
    
    void ExoStart()
    {
        if (player == null)
        {
            return;
        }
        player.setPlayWhenReady(true);
    }
    
    protected void releasePlayer()
    {
        if (player == null)
        {
            return;
        }
        
        try
        {
            if (ExoIsPlaying())
            {
                ExoPause();
            }
            //player.reset();
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
            {
                log.debug("Player Is Stopped");
            }
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
        player = null;
        super.releasePlayer();
    }
    
    @Override
    public Dimension getVideoDimensions()
    {
        if (VerboseLogging.DETAILED_PLAYER_LOGGING)
        {
            log.debug("getVideoDimensions");
        }
        if (player != null)
        {
            if (player.getVideoFormat() != null)
            {
                Dimension d = new Dimension(player.getVideoFormat().width, player.getVideoFormat().height);
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                {
                    log.debug("getVideoSize(): {}", d);
                }
                return d;
            }
            else
            {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                {
                    log.debug("getVideoDimensions: player.getFormat is null");
                }
            }
        }
        else
        {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
            {
                log.debug("getVideoDimensions: player is null");
            }
        }
        return null;
    }
    
    public synchronized void storeCurrentPlaybackPosition()
    {
        try
        {
            playbackTimeLock.lock();
            if(player.isPlaying() && player.getCurrentPosition() > 0)
            {
                currentPlaybackPosition = player.getCurrentPosition();
                log.debug("Store Current playback Position: " + currentPlaybackPosition);
            }
        }
        finally
        {
            playbackTimeLock.unlock();
        }
    }
    
    public synchronized long getCurrentPlaybackPosition()
    {
        try
        {
            playbackTimeLock.lock();
            return currentPlaybackPosition;
        }
        finally
        {
            playbackTimeLock.unlock();
        }
    }
    
    @Override
    public long getPlayerMediaTimeMillis(long lastServerTime)
    {
        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                storeCurrentPlaybackPosition();
            }
        });
        
        if (getCurrentPlaybackPosition() < 0)
        {
            log.debug("Current playback < 0 servertime: " + lastServerTime);
            return lastServerTime;
            
        }
        else
        {
            log.debug("Returning playback time: " + (lastServerTime + this.getCurrentPlaybackPosition()));
            return lastServerTime + this.getCurrentPlaybackPosition();
        }
    }
    
    @Override
    public void stop()
    {
        super.stop();

        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (playerReady)
                {
                    if (player == null)
                    {
                        return;
                    }
                    player.setPlayWhenReady(false);
                }
            }
        });


    }
    
    @Override
    public void pause()
    {
        super.pause();

        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (playerReady)
                {
                    ExoPause();
                }
            }
        });



    }
    
    @Override
    public void play()
    {
        int temp;

        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                //super.play();

                if (playerReady)
                {
                    ExoStart();
                }
            }
        });

    }
    
    private void seekToImpl(long timeInMillis)
    {
        log.debug("JVL - Called seekToImpl - timeInMillis {}", timeInMillis);
        
        if(timeInMillis > 0)
        {
            context.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        playbackTimeLock.lock();
                        player.seekTo(timeInMillis);
        
                    }
                    catch(Exception ex)
                    {
        
                    }
                    finally
                    {
                        playbackTimeLock.unlock();
                    }
                }
            });
        }
    }
    
    @Override
    public void seek(long timeInMS)
    {
        log.debug("SEEK CALLED: pushmode {}, timeinMS {}, playerReady {}", pushMode, timeInMS, playerReady);
        super.seek(timeInMS);
        
        if (playerReady)
        {
            if (!pushMode)
            {
                if (player != null)
                {
                    seekToImpl(timeInMS);
                }
                else
                {
                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                    {
                        log.debug("Seek Resume(Player is Null) {}", timeInMS);
                    }
                    playbackStartPosition = timeInMS;
                }
            }
            else
            {
                if (player != null)
                {
                    seekToImpl(timeInMS);
                }
            }
        }
        else
        {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
            {
                log.debug("Seek Resume {}", timeInMS);
            }
            playbackStartPosition = timeInMS;
        }
    }
    
    @Override
    public void setSubtitleTrack(int streamPos)
    {
        if(streamPos == Exo2MediaPlayerImpl.DISABLE_TRACK)
        {
            this.showCaptions = false;
        }
        else
        {
            this.showCaptions = true;
        }
        
        changeTrack(C.TRACK_TYPE_TEXT, streamPos, 0);
    }
    
    @Override
    public void setAudioTrack(int streamPos)
    {
        if (!ExoIsPlaying())
        {
            initialAudioTrackIndex = streamPos;
        }
        else
        {
            initialAudioTrackIndex = -1;
            changeTrack(C.TRACK_TYPE_AUDIO, streamPos, 0);
        }
    }
    
    @Override
    public synchronized void flush()
    {
        log.debug("JVL - Flush called from Exo");
        super.flush();
        
        if (player == null)
        {
            return;
        }
        
        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    playbackTimeLock.lock();
                    player.prepare(mediaSource, true, false);
                    log.debug("JVL - Prepare player called");
                }
                catch(Exception ex) { }
                finally
                {
                    playbackTimeLock.unlock();
                }
            }
        });
    }
    
    @Override
    protected void setupPlayer(String sageTVurl)
    {
        initialAudioTrackIndex = -1;
        
        if (player != null)
        {
            releasePlayer();
        }
        
        // VerboseLogUtil.setEnableAllTags(true);
        
        //if (VerboseLogging.DETAILED_PLAYER_LOGGING)
        log.debug("Setting up the Exo2 media player for: {}", sageTVurl);
        
        if (pushMode)
        {
            log.debug("Creating Exo2PushDataSource datasource");
            dataSource = new Exo2PushDataSource();
        }
        else
        {
            if (!httpls)
            {
                log.debug("Creating Exo2PullDataSource datasource");
                dataSource = new Exo2PullDataSource(context.getClient().getConnectedServerInfo().address);
            }
            else
            {
                log.debug("Creating null datasource");
                dataSource = null;
            }
        }
    
        log.debug("Creating handler");
        Handler mainHandler = new Handler();

        CustomRenderersFactory customRenderersFactory = new CustomRenderersFactory(context.getContext());

        if(FfmpegLibrary.isAvailable())
        {
            final int preferExtensionDecoders = MiniclientApplication.get().getClient().properties().getInt(PrefStore.Keys.exoplayer_ffmpeg_extension_setting, 1);

            switch (preferExtensionDecoders)
            {
                case CustomRenderersFactory.EXTENSION_RENDERER_MODE_PREFER:
                    log.debug("Setting FFmpeg Extension to Prefer");
                    customRenderersFactory.setExtensionRendererMode(CustomRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
                    break;
                case CustomRenderersFactory.EXTENSION_RENDERER_MODE_ON:
                    log.debug("Setting FFmpeg Extension to On");
                    customRenderersFactory.setExtensionRendererMode(CustomRenderersFactory.EXTENSION_RENDERER_MODE_ON);
                    break;
                case CustomRenderersFactory.EXTENSION_RENDERER_MODE_OFF:
                    log.debug("Setting FFmpeg Extension to Off");
                    customRenderersFactory.setExtensionRendererMode(CustomRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
                    break;
                default:
                    log.debug("Defaulting FFmpeg Extension to On");
                    customRenderersFactory.setExtensionRendererMode(CustomRenderersFactory.EXTENSION_RENDERER_MODE_ON);
            }
        }
        
        CustomMediaCodecSelector mediaCodecSelector = new CustomMediaCodecSelector();
        customRenderersFactory.setMediaCodecSelector(mediaCodecSelector);
    
        

        trackSelector = new DefaultTrackSelector(context.getContext());
        //player = ExoPlayerFactory.newSimpleInstance(context.getContext(), customRenderersFactory, trackSelector);
        SimpleExoPlayer.Builder builder = new SimpleExoPlayer.Builder(context.getContext(), customRenderersFactory);
        builder.setTrackSelector(trackSelector);
        player = builder.build();
        
        player.addListener(new Player.EventListener()
        {
            
    
            @Override
            public void onPlayerError(ExoPlaybackException error)
            {
                log.debug("PLAYER ERROR: " + error.getMessage());
                context.showErrorMessage(error.getMessage(), "Exo2MediaPlayer");
                error.printStackTrace();
            }
    
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState)
            {
                
                if (playbackState == Player.STATE_ENDED)
                {
                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                    {
                        
                        log.debug("Player Has Ended, set EOS");
                    }
                    if (playWhenReady)
                    {
                        stop();
                    }
                    //notifySageTVStop();
                    eos = true;
                    Exo2MediaPlayerImpl.this.state = Exo2MediaPlayerImpl.EOS_STATE;
                }
                if (playbackState == Player.STATE_READY)
                {
                    //debugAvailableTracks();
                    
                    if (initialAudioTrackIndex != -1)
                    {
                        setAudioTrack(initialAudioTrackIndex);
                    }
                }
                
            }
            
            @Override
            public void onSeekProcessed()
            {
                seekPending = false;
            }
            
            @Override
            public void onPositionDiscontinuity(int reason)
            {
                log.warn("ExoPlayer: Continuity Error: {}", reason);
                seekPending = false;
            }
        });
        
        player.addVideoListener(new VideoListener()
        {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio)
            {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                {
                    log.debug("ExoPlayer.onVideoSizeChanged: {}x{}, pixel ratio: {}", width, height, pixelWidthHeightRatio);
                }
                
                // note if pixel ratio is != 0 then calc the ar and apply it.
                if (pixelWidthHeightRatio != 0f)
                {
                    setVideoSize(width, height, pixelWidthHeightRatio * ((float) width / (float) height));
                }
                else
                {
                    setVideoSize(width, height, 0);
                }
            }
            
            
            
            @Override
            public void onRenderedFirstFrame()
            {
            
            }
        });

        
        player.addTextOutput(new TextOutput()
        {
            @Override
            public void onCues(List<Cue> cues)
            {
                if(showCaptions)
                {
                    if (cues.isEmpty())
                    {
                        context.getCaptionsText().setText("");
                    }
                    else
                    {
                        String text = "";
                        for (int i = 0; i < cues.size(); i++)
                        {
                            text += cues.get(i).text;
                        }
            
                        context.getCaptionsText().setText(text);
                        //TODO: Remove method if this has been fixed in player core
                        //setCueDiplayTimer(5000);
                    }
                }
                else
                {
                    context.getCaptionsText().setText("");
                }
            }
        });
        
        // player.setBackgrounded(false);
        /*
        if (playbackStartPosition >= 0)
        {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
            {
                log.debug("Resume Seek Postion: {}", playbackStartPosition);
            }
            seekToImpl(playbackStartPosition);
            playbackStartPosition = -1;
        }
        else
        {
            //player.seekTo(0);
        }
        */
        
        final String sageTVurlFinal = sageTVurl;
        if (!httpls)
        {
            mediaSource = new ExtractorMediaSource(
                    Uri.parse(sageTVurl),
                    new DataSource.Factory()
                    {
                        @Override
                        public DataSource createDataSource()
                        {
                            return dataSource;
                        }
                    },
                    new DefaultExtractorsFactory(), mainHandler, new ExtractorMediaSource.EventListener()
            {
                @Override
                public void onLoadError(IOException e)
                {
                    log.error("FAILED to load: " + sageTVurlFinal);
                }
            });
    
            boolean haveStartPosition = (playbackStartPosition >= 0);
            
            if (haveStartPosition)
            {
                player.seekTo(playbackStartPosition);
            }
            
            player.prepare(mediaSource, !haveStartPosition, false);
            
            
        }
        /*
        else
        {
            String url = sageTVurl;
            log.info("Playing SageTV HTTPLS URL: {}", url);
            //ExtractorMediaSource mediaSource = new ExtractorMediaSource()
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context.getContext(), "sagetv/miniclient android");
            MediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
    
            boolean haveStartPosition = (playbackStartPosition >= 0);
            if (haveStartPosition)
            {
                player.seekTo(playbackStartPosition);
            }
        
            //player.prepare(videoSource);
            player.prepare(videoSource, !haveStartPosition, false);
            //player.prepare(mediaSource, !haveStartPosition, false);
        }
        */
        
        // start playing
        player.setVideoSurface(((SurfaceView) context.getVideoView()).getHolder().getSurface());
        player.setPlayWhenReady(true);
    
        if (VerboseLogging.DETAILED_PLAYER_LOGGING)
        {
            log.debug("Video Player is online");
        }
        
        this.playerReady = true;
        this.state = MiniPlayerPlugin.PLAY_STATE;
    }
    
    
    
    public void changeTrack(int trackType, int groupIndex, int trackIndex)
    {
        context.runOnUiThread(new Runnable()
        {
              @Override
              public void run()
              {
                  DefaultTrackSelector.SelectionOverride override;
                  DefaultTrackSelector.ParametersBuilder parametersBuilder;
    
                  if (trackSelector == null)
                  {
                      log.trace("JVL - Track Selector Null");
                      return;
                  }
    
                  MappingTrackSelector.MappedTrackInfo trackInfo = trackSelector.getCurrentMappedTrackInfo();
    
                  if (trackInfo == null)
                  {
                      log.trace("JVL - Track info null");
                      return;
                  }
    
                  try
                  {
                      TrackGroupArray trackGroup = trackInfo.getTrackGroups(trackType);
        
                      if(groupIndex == Exo2MediaPlayerImpl.DISABLE_TRACK) //Disable trackType from rendering
                      {
                          parametersBuilder = trackSelector.buildUponParameters();
                          parametersBuilder.setRendererDisabled(trackType, true); //This should set the track type to render true
            
                          trackSelector.setParameters(parametersBuilder);
                          log.debug("JVL - Track change executed for disable: TrackType={} TrackGroup={} TrackIndex={}", trackType, trackGroup, trackIndex);
                      }
                      else
                      {
                          if (trackInfo.getTrackSupport(trackType, groupIndex, trackIndex) == RendererCapabilities.FORMAT_HANDLED)
                          {
                              //Clear set new track selection
                              parametersBuilder = trackSelector.buildUponParameters();
                
                              override = new DefaultTrackSelector.SelectionOverride(groupIndex, trackIndex);
                              parametersBuilder.setRendererDisabled(trackType, false); //This should set the track type to render true
                              parametersBuilder.setSelectionOverride(trackType, trackGroup, override);
                
                              trackSelector.setParameters(parametersBuilder);
                              log.debug("JVL - Track change executed: TrackType={} TrackGroup={} TrackIndex={}", trackType, trackGroup, trackIndex);
                          }
                          else
                          {
                              log.debug("ExoPlayer is unable to render the track, TrackType {}, GroupIndex {}, TrackIndex {}", trackType, groupIndex, trackIndex);
                          }
                      }
                  }
                  catch (Exception ex)
                  {
                      log.debug("ExoPlayer error changing track, TrackType {}, GroupIndex {}, TrackIndex {}", trackType, groupIndex, trackIndex);
                      log.debug(ex.getMessage());
                      ex.printStackTrace();
                  }
              }
        });
    }
    
    public void debugAvailableTracks()
    {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        
        
        if (mappedTrackInfo == null)
        {
            log.debug("JVL - No Mapped Track Info");
            return;
        }
        
        for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++)
        {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            
            log.debug("JVL - Track Render Group {}", i);
            
            if (trackGroups.length != 0)
            {
                int label;
                
                switch (player.getRendererType(i))
                {
                    case C.TRACK_TYPE_AUDIO:
                        
                        log.debug("JVL - TRACK_TYPE_AUDIO");
                        break;
                    
                    case C.TRACK_TYPE_VIDEO:
                        
                        log.debug("JVL - TRACK_TYPE_VIDEO");
                        break;
                    
                    case C.TRACK_TYPE_TEXT:
                        
                        log.debug("JVL - TRACK_TYPE_TEXT");
                        break;
                    
                    default:
                        continue;
                }
                
                for (int j = 0; j < trackGroups.length; j++)
                {
                    log.debug("\t Track Group {}", j);
                    
                    for (int k = 0; k < trackGroups.get(j).length; k++)
                    {
                        log.debug("\t\t Track {}, Channels {}, Bitrate {}, Language {}", k, trackGroups.get(j).getFormat(k).channelCount, trackGroups.get(j).getFormat(k).bitrate, trackGroups.get(j).getFormat(k).language);
                        if (mappedTrackInfo.getTrackSupport(i, j, k) == RendererCapabilities.FORMAT_HANDLED)
                        {
                            //Add debug info
                            log.debug("\t\t Format is handled");
                        }
                        else
                        {
                            //Add debug info
                            log.debug("\t\t Format IS NOT HANDLED");
                        }
                    }
                    
                }
                
            }
            else
            {
                log.debug("JVL - Track Group Empty");
            }
        }
        
    }
    
    public synchronized void setCueDiplayTimer(long delay)
    {
        log.debug("setCueDisplayTimer: Delay=" + delay);
        
        if(cueThread == null || !cueThread.isAlive())
        {
            log.debug("Thream is null or dead, creating new thread.");
            
            //Create a Cues thread for hiding queues after set period of time
            cueThread = new Thread(new Runnable()
            {
                
                public void run()
                {
                    while (cueHideTimer > 0)
                    {
                        if (cueHideTimer > 0)
                        {
    
                            cueHideTimer -= 1000;
                            try
                            {
                                log.debug("Queue loop called... Sleep 1000ms");
                                Thread.sleep(1000);
                            }
                            catch (InterruptedException e)
                            {
                            }
                        }
                        
                    }
                    
                    context.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            log.debug("Queue loop called... Hiding text and exiting");
                            context.getCaptionsText().setText("");
                        }
                    });
    
                }
            });
    
            cueHideTimer = delay;
            cueThread.start();
        }
        else
        {
            log.debug("Thread is running...  Update delay");
            cueHideTimer = delay;
        }
    }
    
    
}
