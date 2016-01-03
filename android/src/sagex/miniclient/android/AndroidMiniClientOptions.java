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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sagex.miniclient.IBus;
import sagex.miniclient.MiniClient;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.MiniClientOptions;
import sagex.miniclient.android.prefs.AndroidPrefStore;
import sagex.miniclient.prefs.PrefStore;

/**
 * Created by seans on 08/11/15.
 */
public class AndroidMiniClientOptions implements MiniClientOptions {
    private static final Logger log = LoggerFactory.getLogger(AndroidMiniClientOptions.class);

    private final AndroidPrefStore prefs;
    private final File configDir;
    private final File cacheDir;
    private final IBus bus;

    AndroidMiniClientOptions(Application ctx) {
        this.prefs = new AndroidPrefStore(PreferenceManager.getDefaultSharedPreferences(ctx));
        this.configDir = ctx.getFilesDir();
        this.cacheDir = ctx.getCacheDir();
        this.bus = new OttoBusImpl(new Bus(ThreadEnforcer.ANY));
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

    static Map<String,String> ANDROID_TO_SAGETV_AUDIO_CODEC_MAP = new HashMap<>();
    static Map<String,String> ANDROID_TO_SAGETV_VIDEO_CODEC_MAP = new HashMap<>();
    static {
        ANDROID_TO_SAGETV_AUDIO_CODEC_MAP.put("3gpp", "");
        ANDROID_TO_SAGETV_AUDIO_CODEC_MAP.put("amr-wb", "");
        ANDROID_TO_SAGETV_AUDIO_CODEC_MAP.put("mp4a-latm", MiniClientConnection.AAC);
        ANDROID_TO_SAGETV_AUDIO_CODEC_MAP.put("g711-alaw", "");
        ANDROID_TO_SAGETV_AUDIO_CODEC_MAP.put("g711-mlaw", "");
        ANDROID_TO_SAGETV_AUDIO_CODEC_MAP.put("vorbis", MiniClientConnection.VORBIS);
        ANDROID_TO_SAGETV_AUDIO_CODEC_MAP.put("opus", "");
        ANDROID_TO_SAGETV_AUDIO_CODEC_MAP.put("raw", "");
        ANDROID_TO_SAGETV_AUDIO_CODEC_MAP.put("gsm", "");

        ANDROID_TO_SAGETV_VIDEO_CODEC_MAP.put("avc", MiniClientConnection.MPEG4_VIDEO);
        ANDROID_TO_SAGETV_VIDEO_CODEC_MAP.put("mp4v-es", MiniClientConnection.MPEG4_VIDEO);
        ANDROID_TO_SAGETV_VIDEO_CODEC_MAP.put("3gpp", "");
        ANDROID_TO_SAGETV_VIDEO_CODEC_MAP.put("x-vnd.on2.vp8", "");
        ANDROID_TO_SAGETV_VIDEO_CODEC_MAP.put("x-vnd.on2.vp9", "");
        ANDROID_TO_SAGETV_VIDEO_CODEC_MAP.put("hevc", "");
    }

    @Override
    public void prepareCodecs(List<String> videoCodecs, List<String> audioCodecs, List<String> pushFormats, List<String> pullFormats) {
//        OMX.google.aac.decoder; supported: [audio/mp4a-latm]
//        OMX.google.amrnb.decoder; supported: [audio/3gpp]
//        OMX.google.amrwb.decoder; supported: [audio/amr-wb]
//        OMX.google.g711.alaw.decoder; supported: [audio/g711-alaw]
//        OMX.google.g711.mlaw.decoder; supported: [audio/g711-mlaw]
//        OMX.google.gsm.decoder; supported: [audio/gsm]
//        OMX.google.mp3.decoder; supported: [audio/mpeg]
//        OMX.google.opus.decoder; supported: [audio/opus]
//        OMX.google.raw.decoder; supported: [audio/raw]
//        OMX.google.vorbis.decoder; supported: [audio/vorbis]
//        OMX.Nvidia.eaacp.decoder; supported: [audio/mp4a-latm]
//        OMX.Nvidia.mjpeg.decoder; supported: [video/mjpeg]
//        OMX.Nvidia.mp2.decoder; supported: [audio/mpeg-L2]
//        OMX.Nvidia.mp3.decoder; supported: [audio/mpeg]
//        OMX.Nvidia.wma.decoder; supported: [audio/x-ms-wma]
//
//        OMX.google.h263.decoder; supported: [video/3gpp]
//        OMX.google.h264.decoder; supported: [video/avc]
//        OMX.google.hevc.decoder; supported: [video/hevc]
//        OMX.google.mpeg4.decoder; supported: [video/mp4v-es]
//        OMX.google.vp8.decoder; supported: [video/x-vnd.on2.vp8]
//        OMX.google.vp9.decoder; supported: [video/x-vnd.on2.vp9]
//        OMX.google.vp9.decoder; supported: [video/x-vnd.on2.vp9]
//        OMX.Nvidia.h263.decode; supported: [video/3gpp]
//        OMX.Nvidia.h264.decode; supported: [video/avc]
//        OMX.Nvidia.h265.decode; supported: [video/hevc]
//        OMX.Nvidia.mp4.decode; supported: [video/mp4v-es]
//        OMX.Nvidia.mpeg2v.decode; supported: [video/mpeg2]
//        OMX.Nvidia.vc1.decode; supported: [video/wvc1, video/x-ms-wmv]
//        OMX.Nvidia.vp8.decode; supported: [video/x-vnd.on2.vp8]
//        OMX.Nvidia.vp9.decode; supported: [video/x-vnd.on2.vp9]
//        OMX.qcom.video.decoder.avc; supported: [video/avc]
//        OMX.qcom.video.decoder.h263; supported: [video/3gpp
//        OMX.qcom.video.decoder.mpeg4; supported: [video/mp4v-es]
//        OMX.qcom.video.decoder.vp8; supported: [video/x-vnd.on2.vp8]

        // log the media codec information
        int count = MediaCodecList.getCodecCount();
        log.debug("--------- DUMPING HARDWARE CODECS -----------");
        ArrayList<String> acodecs = new ArrayList<>();
        ArrayList<String> vcodecs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder()) {
                log.debug("[{}] {}; supported: {}", i, info.getName(), info.getSupportedTypes());
                for (String s: getAudioCodecs(info)) {
                    if (ANDROID_TO_SAGETV_AUDIO_CODEC_MAP.containsKey(s)) {
                        acodecs.add(s);
                    } else {
                        log.warn("Unmapped Hardware Audio Codec: [{}] for {}", s, info.getName());
                    }
                }
                for (String s: getVideoCodecs(info)) {
                    if (ANDROID_TO_SAGETV_VIDEO_CODEC_MAP.containsKey(s)) {
                        vcodecs.add(s);
                    } else {
                        log.warn("Unmapped Hardware Video Codec: [{}] for {}", s, info.getName());
                    }
                }
            }
        }
        log.debug("--------- END DUMPING HARDWARE CODECS -----------");

        // TODO: We now need to update the codec support list

        if (getPrefs().getBoolean(PrefStore.Keys.use_exoplayer)) {
            // no hardware support for mpeg2
//            if (!vcodecs.contains("mpeg2")) {
//                log.warn("Removing MPEG2 support");
//                videoCodecs.remove(MiniClientConnection.MPEG2_VIDEO);
//                videoCodecs.remove("MPEG2-VIDEO");
//                videoCodecs.remove("MPEG2-VIDEO@HL");
//            }
        }

    }

    private List<String> getAudioCodecs(MediaCodecInfo info) {
        if (info==null||info.getSupportedTypes()==null||info.getSupportedTypes().length==0) return Collections.emptyList();

        List<String> list = new ArrayList<>();
        String parts[]=null;
        for (String s: info.getSupportedTypes()) {
            if (s.startsWith("audio/")) {
                parts=s.split("/");
                for (String p: parts) {
                    if (!list.contains(p.trim())) {
                        list.add(p.trim());
                    }
                }
            }
        }
        return list;
    }

    private List<String> getVideoCodecs(MediaCodecInfo info) {
        if (info==null||info.getSupportedTypes()==null||info.getSupportedTypes().length==0) return Collections.emptyList();

        List<String> list = new ArrayList<>();
        String parts[]=null;
        for (String s: info.getSupportedTypes()) {
            if (s.startsWith("video/")) {
                parts=s.split("/");
                for (String p: parts) {
                    if (!list.contains(p.trim())) {
                        list.add(p.trim());
                    }
                }
            }
        }
        return list;
    }

    private boolean isAudioDecoder(MediaCodecInfo info) {
        if (info==null||info.getSupportedTypes()==null||info.getSupportedTypes().length==0) return false;

        for (String s: info.getSupportedTypes()) {
            if (s.startsWith("audio/")) {
                return true;
            }
        }
        return false;
    }

    private boolean isVideoDecoder(MediaCodecInfo info) {
        if (info==null||info.getSupportedTypes()==null||info.getSupportedTypes().length==0) return false;

        for (String s: info.getSupportedTypes()) {
            if (s.startsWith("video/")) {
                return true;
            }
        }
        return false;
    }
}
