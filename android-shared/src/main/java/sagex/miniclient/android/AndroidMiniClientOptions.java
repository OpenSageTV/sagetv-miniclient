package sagex.miniclient.android;

import android.app.Application;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.preference.PreferenceManager;

import com.google.android.exoplayer2.ext.ffmpeg.FfmpegLibrary;
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

    AndroidMiniClientOptions(Application ctx)
    {
        this.prefs=new AndroidPrefStore(PreferenceManager.getDefaultSharedPreferences(ctx));
        this.configDir = ctx.getFilesDir();
        this.cacheDir = ctx.getCacheDir();
        this.bus = new OttoBusImpl(new Bus(ThreadEnforcer.ANY));
        this.isTV = ctx.getResources().getBoolean(R.bool.istv);
        this.isTOUCH = !isTV;
        this.advancedAspects=true;
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
    public void prepareCodecs(List<String> videoCodecs, List<String> audioCodecs, List<String> pushFormats, List<String> pullFormats, Properties codecs)
    {

        Set<String> acodecs = new TreeSet<>();
        Set<String> vcodecs = new TreeSet<>();

        pushFormats.clear();
        pushFormats.addAll(getSupportedPushContainers());

        pullFormats.clear();
        pullFormats.addAll(getSupportedPullContainers());

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


        if(getPrefs().getString(PrefStore.Keys.default_player, "exoplayer").equalsIgnoreCase("exoplayer"))
        {
            /*
            videoCodecs.clear();
            
            for (String s: vcodecs)
            {
                //if(codecs.getProperty(s) != null
                //    && (prefs.getContainerSupport(codecs.getProperty(s).toUpperCase()).equalsIgnoreCase("automatic")
                //    || prefs.getContainerSupport(codecs.getProperty(s).toUpperCase()).equalsIgnoreCase("enabled")))
                //{
                    if (codecs.getProperty(s) != null)
                    {
                        log.debug("Codec Supported: Android Mime={}, SageTV Type={}", s, codecs.getProperty(s));
                        videoCodecs.add(codecs.getProperty(s));
                    }
                //}
                //else
                //{
                //    log.debug("Codec set to disabled by preference: Android Mime={}, SageTV Type={}", s, codecs.getProperty(s));
                //}
            }
            */


            //Check to see if MPEG2-VIDEO is supported.  If so add MPEG2-VIDEO@HL.  This appears to be a SageTV Specific setting
            /* Added MPEG2-VIDEO@HL to the codec.properties file as a MPEG2 codec
            if(videoCodecs.contains("MPEG2-VIDEO") && !videoCodecs.contains("MPEG2-VIDEO@HL"))
            {
                videoCodecs.add("MPEG2-VIDEO@HL");
            }
             */

            //SageTV sets the codec string 0X0000 if it does not know what it is.  This generally happens with HEVC.  Adding this if it does not exits
            //if(!videoCodecs.contains("0X0000"))
            //{
            //    videoCodecs.add("0X0000");
            //}

            //audioCodecs.clear();
            //for (String s: acodecs)
            //{
            //    if (codecs.getProperty(s)!=null)
            //    {
            //        audioCodecs.add(codecs.getProperty(s));
            //    }
            //}

            // exoplayer supports passthrough
            //audioCodecs.add("AC3");



        }
    }

    private List<String> getSupportedPullContainers()
    {
        List<String> supportedPullContainers = new ArrayList<String>();
        List<String> allPullContainers = stringToList(MiniClientConnection.DEFAULT_PULL_FORMATS);

        for(int i = 0; i < allPullContainers.size(); i++)
        {
            if(prefs.getContainerSupport(allPullContainers.get(i)).equalsIgnoreCase("automatic")
                    || prefs.getContainerSupport(allPullContainers.get(i)).equalsIgnoreCase("enabled"))
            {
                supportedPullContainers.add(allPullContainers.get(i));
            }
        }

        return supportedPullContainers;
    }

    private List<String> getSupportedPushContainers()
    {
        List<String> supportedPushContainers = new ArrayList<String>();
        List<String> allPushContainers = stringToList(MiniClientConnection.DEFAULT_PUSH_FORMATS);

        for(int i = 0; i < allPushContainers.size(); i++)
        {
            if(prefs.getContainerSupport(allPushContainers.get(i)).equalsIgnoreCase("automatic")
                    || prefs.getContainerSupport(allPushContainers.get(i)).equalsIgnoreCase("enabled"))
            {
                supportedPushContainers.add(allPushContainers.get(i));
            }
        }

        return supportedPushContainers;
    }

    private List<AudioCodec> getSupportedAudioCodecs()
    {
        List<AudioCodec> supportedCodecs = new ArrayList<AudioCodec>();
        AudioCodec[] allCodecs = AudioCodec.values();

        List<String> exoplayerCodecsMimeType = new ArrayList<>();

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
                    //If ffmpeg is available an enabled than check that first
                    if(FfmpegLibrary.isAvailable() && !getPrefs().getString(PrefStore.Keys.exoplayer_ffmpeg_extension_setting, "1").equalsIgnoreCase("0"))
                    {
                        for(int j = 0; j < allCodecs[i].getAndroidMimeTypes().length; j++)
                        {
                            if(FfmpegLibrary.supportsFormat(allCodecs[i].getAndroidMimeTypes()[j]))
                            {
                                log.debug("Audio codec added because it is supported by FFmpeg ext: " + allCodecs[i].getName());
                                supportedCodecs.add(allCodecs[i]);
                            }
                        }
                    }

                    for(int j = 0; j < exoplayerCodecsMimeType.size(); j++)
                    {
                        if(allCodecs[i].hasAndroidMimeType(exoplayerCodecsMimeType.get(j)))
                        {
                            log.debug("Audio codec supported by android device: " + allCodecs[i].getName());
                            supportedCodecs.add(allCodecs[i]);
                            break;
                        }
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
            }
        }

        return supportedCodecs;
    }




    private List<String> stringToList(String str)
    {
        ArrayList<String> list = new ArrayList<String>();
        if (str==null) return list;

        for (String s: str.split("\\s*,\\s*"))
        {
            list.add(s.trim());
        }
        return list;
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
