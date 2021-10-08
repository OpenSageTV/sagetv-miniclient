package sagex.miniclient.android;

import android.app.Application;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.preference.PreferenceManager;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import sagex.miniclient.IBus;
import sagex.miniclient.MiniClientOptions;
import sagex.miniclient.android.prefs.AndroidPrefStore;
//import sagex.miniclient.prefs.ConnectionPrefStore;
import sagex.miniclient.util.AspectModeManager;
import sagex.miniclient.prefs.PrefStore;

/**
 * Created by seans on 08/11/15.
 */
public class AndroidMiniClientOptions implements MiniClientOptions {
    private static final Logger log = LoggerFactory.getLogger(AndroidMiniClientOptions.class);

    private final PrefStore prefs;
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

        log.debug("--------- DUMPING HARDWARE CODECS -----------");
        for (int i = 0; i < MediaCodecList.getCodecCount(); i++)
        {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);

            if (!info.isEncoder())
            {

                log.debug("[{}] {}; supported: {}", i, info.getName(), info.getSupportedTypes());
            
                for (String s : getAudioCodecs(info))
                {
                    log.debug("\t" + s);
                    acodecs.add(s);
                }
                for (String s : getVideoCodecs(info))
                {
                    log.debug("\t" + s);
                    vcodecs.add(s);
                }
            }
        }

        log.debug("--------- END DUMPING HARDWARE CODECS -----------");

        // update the supported hardware codecs for SageTV
        // SageTV is crashing when we are enabling formats, so we are doing something wrong
        // could be that we need to send MPEG2-VIDEO@HL to tell sagetv that we are a
        // media extender

        if(getPrefs().getString(PrefStore.Keys.default_player, "exoplayer").equalsIgnoreCase("exoplayer"))
        {
            videoCodecs.clear();
            
            for (String s: vcodecs)
            {
                if (codecs.getProperty(s)!=null)
                {
                    log.debug("Codec Supported: Android Mime={}, SageTV Type={}", s, codecs.getProperty(s));
                    videoCodecs.add(codecs.getProperty(s));
                }
            }

            //Check to see if MPEG2-VIDEO is supported.  If so add MPEG2-VIDEO@HL.  This appears to be a SageTV Specific setting
            /* Added MPEG2-VIDEO@HL to the codec.properties file as a MPEG2 codec
            if(videoCodecs.contains("MPEG2-VIDEO") && !videoCodecs.contains("MPEG2-VIDEO@HL"))
            {
                videoCodecs.add("MPEG2-VIDEO@HL");
            }
             */

            //SageTV sets the codec string 0X0000 if it does not know what it is.  This generally happens with HEVC.  Adding this if it does not exits
            if(!videoCodecs.contains("0X0000"))
            {
                videoCodecs.add("0X0000");
            }

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
