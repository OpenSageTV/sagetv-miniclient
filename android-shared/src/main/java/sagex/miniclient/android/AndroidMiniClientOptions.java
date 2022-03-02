package sagex.miniclient.android;

import static sagex.miniclient.media.Container.*;

import android.app.Application;
import android.content.Context;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.preference.PreferenceManager;

import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegLibrary;
import com.google.android.exoplayer2.util.MimeTypes;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import sagex.miniclient.IBus;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.MiniClientOptions;
import sagex.miniclient.android.prefs.AndroidPrefStore;
//import sagex.miniclient.prefs.ConnectionPrefStore;
import sagex.miniclient.media.AudioCodec;
import sagex.miniclient.media.Container;
import sagex.miniclient.util.AspectModeManager;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.media.VideoCodec;

/**
 * Created by seans on 08/11/15.
 */
public class AndroidMiniClientOptions implements MiniClientOptions {
    private static final Logger log = LoggerFactory.getLogger(AndroidMiniClientOptions.class);

    private final AndroidPrefStore prefs;
    private final File configDir;
    private final File cacheDir;
    private final IBus bus;
    private boolean isTV=false;
    private boolean isTOUCH=false;
    private boolean advancedAspects=false;
    private Context context;

    AndroidMiniClientOptions(Application ctx)
    {
        this.prefs=new AndroidPrefStore(PreferenceManager.getDefaultSharedPreferences(ctx));
        this.configDir = ctx.getFilesDir();
        this.cacheDir = ctx.getCacheDir();
        this.bus = new OttoBusImpl(new Bus(ThreadEnforcer.ANY));
        this.isTV = ctx.getResources().getBoolean(R.bool.istv);
        this.isTOUCH = !isTV;
        this.advancedAspects=true;
        this.context = ctx;
    }

    @Override
    public PrefStore getPrefs() {
        return prefs;
    }

    @Override
    public File getConfigDir() {
        return configDir;
    }

    @Override
    public File getCacheDir() {
        return cacheDir;
    }

    @Override
    public IBus getBus() {
        return bus;
    }

    @Override
    public void prepareCodecs(List<String> videoCodecs, List<String> audioCodecs, List<String> pushFormats, List<String> pullFormats)
    {

        Set<String> acodecs = new TreeSet<>();
        Set<String> vcodecs = new TreeSet<>();

        pushFormats.clear();
        List<Container> supPushContainers = this.getSupportedPushContainers();

        for(int i = 0; i < supPushContainers.size(); i++)
        {
            for(int j = 0; j < supPushContainers.get(i).getSageTVNames().length; j++)
            {
                pushFormats.add(supPushContainers.get(i).getSageTVNames()[j]);
            }
        }

        pullFormats.clear();
        List<Container> supPullContainers = this.getSupportedPullContainers();

        for(int i = 0; i < supPullContainers.size(); i++)
        {
            for(int j = 0; j < supPullContainers.get(i).getSageTVNames().length; j++)
            {
                pullFormats.add(supPullContainers.get(i).getSageTVNames()[j]);
            }
        }

        videoCodecs.clear();
        List<VideoCodec> supVideoCodecs = this.getSupportedVideoCodecs();

        for(int i = 0; i < supVideoCodecs.size(); i++)
        {
            for(int j = 0; j < supVideoCodecs.get(i).sageTVNames().length; j++)
            {
                videoCodecs.add(supVideoCodecs.get(i).sageTVNames()[j]);
            }
        }

        audioCodecs.clear();
        List<AudioCodec> supAudioCodecs = this.getSupportedAudioCodecs();

        for(int i = 0; i < supAudioCodecs.size(); i++)
        {
            for(int j = 0; j < supAudioCodecs.get(i).getSageTVNames().length; j++)
            {
                audioCodecs.add(supAudioCodecs.get(i).getSageTVNames()[j]);
            }
        }

    }

    private List<Container> getSupportedPushContainers()
    {
        List<Container> supportedContainers = new ArrayList<Container>();
        Container [] allContainers = new Container[]{MPEG1PS, MPEG2PS, MPEG2TS};

        for(int i = 0; i < allContainers.length; i++)
        {
            if(prefs.getContainerSupport(allContainers[i].getName()).equalsIgnoreCase("enabled"))
            {
                log.debug("Push Container being added because it is set as enabled: " + allContainers[i].getName());
                supportedContainers.add(allContainers[i]);
            }
            else if(prefs.getContainerSupport(allContainers[i].getName()).equalsIgnoreCase("automatic"))
            {
                if(getPrefs().getString(PrefStore.Keys.default_player, "exoplayer").equalsIgnoreCase("exoplayer"))
                {
                    if(isSupportedExoPlayerContainer(allContainers[i]))
                    {
                        log.debug("Push Container being added because it is set as automatic and is ExoPlayer supported: " + allContainers[i].getName());
                        supportedContainers.add(allContainers[i]);
                    }
                }
                else
                {
                    log.debug("Push Container being added because it is set as automatic and player is IJKPlayer: " + allContainers[i].getName());
                    //IJK Player.  Adding all for now
                    supportedContainers.add(allContainers[i]);
                }
            }
            else
            {
                log.debug("Pull Container being NOT added because it is set as disabled: " + allContainers[i].getName());
            }
        }
        return supportedContainers;
    }

    private List<Container> getSupportedPullContainers()
    {
        List<Container> supportedContainers = new ArrayList<Container>();
        Container [] allContainers = Container.values();

        for(int i = 0; i < allContainers.length; i++)
        {
            if (allContainers[i] == MPEG1PS || allContainers[i] == MPEG2TS || allContainers[i] == MPEG2PS)
            {
                //These codecs are not support for pull at this time.  They are push only formats.
            }
            else
            {
                if (prefs.getContainerSupport(allContainers[i].getName()).equalsIgnoreCase("enabled")) {
                    log.debug("Pull Container being added because it is set as enabled: " + allContainers[i].getName());
                    supportedContainers.add(allContainers[i]);
                } else if (prefs.getContainerSupport(allContainers[i].getName()).equalsIgnoreCase("automatic")) {
                    if (getPrefs().getString(PrefStore.Keys.default_player, "exoplayer").equalsIgnoreCase("exoplayer")) {
                        if (isSupportedExoPlayerContainer(allContainers[i])) {
                            log.debug("Pull Container being added because it is set as automatic and is ExoPlayer supported: " + allContainers[i].getName());
                            supportedContainers.add(allContainers[i]);
                        }
                    } else {
                        log.debug("Pull Container being added because it is set as automatic and player is IJKPlayer: " + allContainers[i].getName());
                        //IJK Player.  Adding all for now
                        supportedContainers.add(allContainers[i]);
                    }
                } else {
                    log.debug("Pull Container being NOT added because it is set as disabled: " + allContainers[i].getName());
                }
            }
        }

        return supportedContainers;
    }

    private List<AudioCodec> getSupportedAudioCodecs()
    {
        List<AudioCodec> supportedCodecs = new ArrayList<AudioCodec>();
        AudioCodec[] allCodecs = AudioCodec.values();

        List<String> exoplayerCodecsMimeType = new ArrayList<>();
        AudioCapabilities capabilities = AudioCapabilities.getCapabilities(context);

        //Get all supported video mime types that exoplayer is reporting
        for (int i = 0; i < MediaCodecList.getCodecCount(); i++)
        {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);

            if (!info.isEncoder())
            {
                exoplayerCodecsMimeType.addAll(getAudioCodecs(info));
            }
        }

        for(int i = 0; i < allCodecs.length; i++)
        {
            if(prefs.getAudioCodecSupport(allCodecs[i].getName()).equalsIgnoreCase("enabled"))
            {
                log.debug("Audio codec marked enabled: " + allCodecs[i].getName());
                supportedCodecs.add(allCodecs[i]);
            }
            else if(prefs.getAudioCodecSupport(allCodecs[i].getName()).equalsIgnoreCase("automatic"))
            {
                if(getPrefs().getString(PrefStore.Keys.default_player, "exoplayer").equalsIgnoreCase("exoplayer"))
                {
                    boolean supported = false;

                    //If ffmpeg is available an enabled than check that first
                    if(FfmpegLibrary.isAvailable() && !getPrefs().getString(PrefStore.Keys.exoplayer_ffmpeg_extension_setting, "1").equalsIgnoreCase("0"))
                    {
                        if(FfmpegLibrary.supportsFormat(allCodecs[i].getAndroidMimeType()))
                        {
                            log.debug("Audio codec added because it is supported by FFmpeg ext: " + allCodecs[i].getName());
                            supportedCodecs.add(allCodecs[i]);
                            supported = true;
                        }
                    }

                    if(!supported)
                    {
                        for (int j = 0; j < exoplayerCodecsMimeType.size(); j++)
                        {
                            if (!supported && allCodecs[i].hasAndroidMimeType(exoplayerCodecsMimeType.get(j)))
                            {
                                log.debug("Audio codec supported by android device: " + allCodecs[i].getName());
                                supportedCodecs.add(allCodecs[i]);
                                supported = true;

                            }
                        }
                    }

                    if(!supported)
                    {
                        for (int j = 0; j < allCodecs[i].getAndroidAudioEncodings().length; j++)
                        {
                            if (!supported && capabilities.supportsEncoding(allCodecs[i].getAndroidAudioEncodings()[j]))
                            {
                                supportedCodecs.add(allCodecs[i]);
                                supported = true;
                            }
                        }
                    }

                    if(!supported)
                    {
                        log.debug("Audio codec set to automatic and is not supported: " + allCodecs[i].getName());
                    }

                }
                else
                {
                    log.debug("Audio codec added because it was set to auto and player is not ExoPlayer: " + allCodecs[i].getName());
                    supportedCodecs.add(allCodecs[i]);
                }
            }
            else
            {
                log.debug("Audio codec NOT SUPPORTED: " + allCodecs[i].getName());
            }
        }

        return supportedCodecs;
    }

    private List<VideoCodec> getSupportedVideoCodecs()
    {
        List<VideoCodec> supportedCodecs = new ArrayList<VideoCodec>();
        VideoCodec[] allCodecs = VideoCodec.values();

        List<String> exoplayerCodecsMimeType = new ArrayList<>();

        //Get all supported video mime types that exoplayer is reporting
        for (int i = 0; i < MediaCodecList.getCodecCount(); i++)
        {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);

            if (!info.isEncoder())
            {
                exoplayerCodecsMimeType.addAll(getVideoCodecs(info));
            }
        }

        for(int i = 0; i < allCodecs.length; i++)
        {
            if(prefs.getVideoCodecSupport(allCodecs[i].getName()).equalsIgnoreCase("enabled"))
            {
                log.debug("Video codec marked enabled: " + allCodecs[i].getName());

                supportedCodecs.add(allCodecs[i]);
            }
            else if(prefs.getVideoCodecSupport(allCodecs[i].getName()).equalsIgnoreCase("automatic"))
            {
                if(getPrefs().getString(PrefStore.Keys.default_player, "exoplayer").equalsIgnoreCase("exoplayer"))
                {
                    //Determine if ExoPlayer supports the codec
                    for(int j = 0; j < exoplayerCodecsMimeType.size(); j++)
                    {
                        if(allCodecs[i].hasAndroidMimeType(exoplayerCodecsMimeType.get(j)))
                        {
                            log.debug("Video codec marked automatic, and is supported: " + allCodecs[i]);

                            supportedCodecs.add(allCodecs[i]);
                            break;
                        }
                    }
                }
                else
                {
                    log.debug("Video codec marked automatic, and player is not exoplayer: " + allCodecs[i]);

                    //This is most likely IJKPlayer.  We assume everything is supported
                    supportedCodecs.add(allCodecs[i]);
                }
            }
            else
            {
                //Marked as disabled
                log.debug("Video codec marked disabled: " + allCodecs[i]);
            }
        }

        return supportedCodecs;
    }


    @Override
    public boolean isTouchUI()
    {
        return isTOUCH;
    }

    @Override
    public boolean isTVUI()
    {
        return isTV;
    }

    @Override
    public boolean isDesktopUI()
    {
        return false;
    }

    @Override
    public boolean isUsingAdvancedAspectModes()
    {
        return advancedAspects;
    }

    @Override
    public String getAdvancedApectModes()
    {
        return AspectModeManager.ASPECT_MODES;
    }

    @Override
    public String getDefaultAdvancedAspectMode()
    {
        return AspectModeManager.DEFAULT_ASPECT_MODE;
    }

    private Set<String> getAudioCodecs(MediaCodecInfo info)
    {
        if (info == null || info.getSupportedTypes() == null || info.getSupportedTypes().length == 0)
            return Collections.emptySet();

        Set<String> list = new TreeSet<>();

        for (String s : info.getSupportedTypes())
        {
            if (s.startsWith("audio/"))
            {
                list.add(s.trim());
            }
        }
        return list;
    }

    private Set<String> getVideoCodecs(MediaCodecInfo info)
    {
        if (info == null || info.getSupportedTypes() == null || info.getSupportedTypes().length == 0)
            return Collections.emptySet();

        Set<String> list = new TreeSet<>();
        for (String s : info.getSupportedTypes())
        {
            if (s.startsWith("video/"))
            {
                list.add(s.trim());
            }
        }
        return list;
    }

    private boolean isSupportedExoPlayerContainer(Container container)
    {
        switch(container)
        {
            case MATROSKA:
                return true;
            case MP4:
                return true;
            case MP3:
                return true;
            case OGG:
                return true;
            case WAV:
                return true;
            case MPEG1PS:
                return true;
            case MPEG2PS:
                return true;
            case MPEG2TS:
                return true;
            case FLASHVIDEO:
                return true;
            case AAC:
                return true;
            default:
                return false;

        }


    }

    private boolean isAudioDecoder(MediaCodecInfo info)
    {
        if (info == null || info.getSupportedTypes() == null || info.getSupportedTypes().length == 0)
            return false;

        for (String s : info.getSupportedTypes())
        {
            if (s.startsWith("audio/"))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isVideoDecoder(MediaCodecInfo info)
    {
        if (info == null || info.getSupportedTypes() == null || info.getSupportedTypes().length == 0)
            return false;

        for (String s : info.getSupportedTypes())
        {
            if (s.startsWith("video/"))
            {
                return true;
            }
        }
        return false;
    }



}
