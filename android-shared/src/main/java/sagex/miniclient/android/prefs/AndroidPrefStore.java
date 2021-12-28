package sagex.miniclient.android.prefs;

import android.content.SharedPreferences;

import java.util.Set;
import java.util.TreeSet;

import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.prefs.PrefStore;

/**
 * Created by seans on 08/11/15.
 */
public class AndroidPrefStore implements PrefStore
{
    //<editor-fold name="Constants">

    public static final String STREAMING_MODE = "streaming_mode";
    public static final String STREAMING_MODE_DEFAULT = "fixed";

    public static final String FIXED_ENCODING_PREFERENCE = "fixed_encoding/preference";
    public static final String FIXED_ENCODING_PREFERENCE_DEFAULT = "needed";

    public static final String FIXED_ENCODING_FORMAT = "fixed_encoding/format";
    public static final String FIXED_ENCODING_FORMAT_DEFAULT = "matroska";

    public static final String FIXED_ENCODING_AUDIO_CODEC = "fixed_encoding/audio_codec";
    public static final String FIXED_ENCODING_AUDIO_CODEC_DEFAULT = "ac3";

    public static final String FIXED_ENCODING_AUDIO_CHANNELS = "fixed_encoding/audio_channels";
    public static final String FIXED_ENCODING_AUDIO_CHANNELS_DEFAULT = "";

    public static final String FIXED_ENCODING_VIDEO_BITRATE_KBPS = "fixed_encoding/video_bitrate_kbps";
    public static final int FIXED_ENCODING_VIDEO_BITRATE_KBPS_DEFAULT = 4000;

    public static final String FIXED_ENCODING_AUDIO_BITRATE_KBPS = "fixed_encoding/audio_bitrate_kbps";
    public static final int FIXED_ENCODING_AUDIO_BITRATE_KBPS_DEFAULT = 128;

    public static final String FIXED_ENCODING_FPS = "fixed_encoding/video_fps";
    public static final String FIXED_ENCODING_FPS_DEFAULT = "SOURCE";

    public static final String FIXED_ENCODING_KEY_FRAME_INTERVAL = "fixed_encoding/key_frame_interval";
    public static final int FIXED_ENCODING_KEY_FRAME_INTERVAL_DEFAULT = 10;

    public static final String FIXED_ENCODING_USE_B_FRAMES = "fixed_encoding/use_b_frames";
    public static final boolean FIXED_ENCODING_USE_B_FRAMES_DEFAULT = true;

    public static final String FIXED_ENCODING_VIDEO_RESOLUTION = "fixed_encoding/video_resolution";
    public static final String FIXED_ENCODING_VIDEO_RESOLUTION_DEFAULT = "SOURCE";

    public static final String FIXED_REMUXING_PREFERENCE = "fixed_remuxing/preference";
    public static final String FIXED_REMUXING_PREFERENCE_DEFAULT = "needed";

    public static final String FIXED_REMUXING_FORMAT = "fixed_remuxing/format";
    public static final String FIXED_REMUXING_FORMAT_DEFAULT = "matroska";

    public static final String CONTAINER_SUPPORT_DEFAULT = "automatic";


    //</editor-fold>

    private final SharedPreferences prefs;

    public AndroidPrefStore(SharedPreferences prefs)
    {
        this.prefs = prefs;
    }

    //<editor-fold name="Preference Getter/Setter Methods">

    @Override
    public String getString(String key)
    {
        return getString(key, null);
    }

    @Override
    public String getString(String key, String defValue)
    {
        return prefs.getString(key, defValue);
    }

    @Override
    public void setString(String key, String value)
    {
        prefs.edit().putString(key, value).apply();
    }

    @Override
    public long getLong(String key)
    {
        return getLong(key, 0);
    }

    @Override
    public long getLong(String key, long defValue)
    {
        try
        {
            return Long.parseLong(prefs.getString(key, String.valueOf(defValue)));
        }
        catch (ClassCastException cce)
        {
            this.setLong(key, defValue);
            return defValue;
        }


    }

    @Override
    public void setLong(String key, long value)
    {
        prefs.edit().putLong(key, value).apply();
    }

    @Override
    public int getInt(String key)
    {
        return getInt(key, 0);
    }

    @Override
    public int getInt(String key, int defValue)
    {
        try
        {
            return Integer.parseInt(prefs.getString(key, String.valueOf(defValue)));
        }
        catch (ClassCastException cce)
        {
            this.setInt(key, defValue);
            return defValue;
        }
    }

    @Override
    public void setInt(String key, int value)
    {
        prefs.edit().putInt(key, value).apply();
    }

    @Override
    public double getDouble(String key)
    {
        return getDouble(key, 0);
    }

    @Override
    public double getDouble(String key, double defValue)
    {
        try
        {
            return Double.parseDouble(prefs.getString(key, defValue + ""));
        }
        catch (ClassCastException cce)
        {
            this.setDouble(key, defValue);
            return defValue;
        }
    }

    @Override
    public void setDouble(String key, double value)
    {
        prefs.edit().putFloat(key, (float) value).commit();
    }

    @Override
    public boolean getBoolean(String key)
    {
        return getBoolean(key, false);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue)
    {
        try
        {
            return prefs.getBoolean(key, defValue);
        }
        catch (ClassCastException cce)
        {
            this.setBoolean(key, defValue);
            return defValue;
        }
    }

    @Override
    public void setBoolean(String key, boolean value)
    {
        prefs.edit().putBoolean(key, value).commit();
    }

    @Override
    public Set<Object> keys()
    {
        return new TreeSet<Object>(prefs.getAll().keySet());
    }

    @Override
    public void remove(String key)
    {
        prefs.edit().remove(key).commit();
    }

    @Override
    public boolean contains(String key)
    {
        return prefs.contains(key);
    }

    @Override
    public boolean canSet(String key)
    {
        return true;
    }

    //</editor-fold>

    //<editor-fold name="Fixed Encoding Prefs/Settings">

    @Override
    public String getStreamingMode()
    {
        return prefs.getString(AndroidPrefStore.STREAMING_MODE, AndroidPrefStore.STREAMING_MODE_DEFAULT);
    }

    @Override
    public String getFixedEncodingPreference()
    {
        return prefs.getString(AndroidPrefStore.FIXED_ENCODING_PREFERENCE, AndroidPrefStore.FIXED_ENCODING_PREFERENCE_DEFAULT);
    }

    @Override
    public String getFixedEncodingContainerFormat()
    {
        return this.getString(AndroidPrefStore.FIXED_ENCODING_FORMAT, AndroidPrefStore.FIXED_ENCODING_FORMAT_DEFAULT);
    }

    @Override
    public int getFixedEncodingVideoBitrateKBPS()
    {
        return this.getInt(AndroidPrefStore.FIXED_ENCODING_VIDEO_BITRATE_KBPS, AndroidPrefStore.FIXED_ENCODING_VIDEO_BITRATE_KBPS_DEFAULT);
    }

    @Override
    public String getFixedEncodingAudioCodec()
    {
        return this.getString(AndroidPrefStore.FIXED_ENCODING_AUDIO_CODEC, AndroidPrefStore.FIXED_ENCODING_AUDIO_CODEC_DEFAULT);
    }

    @Override
    public String getFixedEncodingAudioChannels()
    {
        return this.getString(AndroidPrefStore.FIXED_ENCODING_AUDIO_CHANNELS, AndroidPrefStore.FIXED_ENCODING_AUDIO_CHANNELS_DEFAULT);
    }


    @Override
    public int getFixedEncodingAudioBitrateKBPS()
    {
        return this.getInt(AndroidPrefStore.FIXED_ENCODING_AUDIO_BITRATE_KBPS, AndroidPrefStore.FIXED_ENCODING_AUDIO_BITRATE_KBPS_DEFAULT);
    }

    @Override
    public String getFixedEncodingFPS()
    {
        return this.getString(AndroidPrefStore.FIXED_ENCODING_FPS, AndroidPrefStore.FIXED_ENCODING_FPS_DEFAULT);
    }

    @Override
    public int getFixedEncodingKeyFrameInterval()
    {
        return this.getInt(AndroidPrefStore.FIXED_ENCODING_KEY_FRAME_INTERVAL, AndroidPrefStore.FIXED_ENCODING_KEY_FRAME_INTERVAL_DEFAULT);
    }

    @Override
    public boolean getFixedEncodingUseBFrames()
    {
        return this.getBoolean(AndroidPrefStore.FIXED_ENCODING_USE_B_FRAMES, AndroidPrefStore.FIXED_ENCODING_USE_B_FRAMES_DEFAULT);
    }

    @Override
    public String getFixedEncodingVideoResolution()
    {
        return this.getString(AndroidPrefStore.FIXED_ENCODING_VIDEO_RESOLUTION, AndroidPrefStore.FIXED_ENCODING_VIDEO_RESOLUTION_DEFAULT);
    }

    @Override
    public String getFixedRemuxingPreference()
    {
        return this.getString(AndroidPrefStore.FIXED_REMUXING_PREFERENCE, AndroidPrefStore.FIXED_REMUXING_PREFERENCE_DEFAULT);
    }

    @Override
    public String getFixedRemuxingFormat()
    {
        return this.getString(AndroidPrefStore.FIXED_REMUXING_FORMAT, AndroidPrefStore.FIXED_REMUXING_FORMAT_DEFAULT);
    }

    public String getContainerSupport(String container)
    {
        return this.getString("container/" + container + "/support", AndroidPrefStore.CONTAINER_SUPPORT_DEFAULT);
    }

    public String getVideoCodecSupport(String codec)
    {
        return this.getString("codec/video/" + codec + "/support", AndroidPrefStore.CONTAINER_SUPPORT_DEFAULT);
    }

    /***
     * Gets the mime types that Android associates with these different video codecs.  This is utilized
     * to map between SageTV codecs identifiers and Android codec identifiers
     * @param codec The SageTV codec identifier we are looking for a mime type
     * @return A list of mime types seperated by a comma that are associated with the SageTV codec identifier
     */
    public String getVideoCodecMimeTypes(String codec)
    {
        switch (codec)
        {
            case MiniClientConnection.MPEG1_VIDEO:
                return MiniClientConnection.MPEG1_VIDEO_MIME_TYPES;
            case MiniClientConnection.MPEG2_VIDEO:
                return MiniClientConnection.MPEG2_VIDEO_MIME_TYPES;
            case MiniClientConnection.MPEG2_VIDEO_HL:
                return MiniClientConnection.MPEG2_VIDEO_HL_MIME_TYPES;
            case MiniClientConnection.H263_VIDEO:
                return  MiniClientConnection.H263_VIDEO_MIME_TYPES;
            case MiniClientConnection.MPEG4_VIDEO:
                return MiniClientConnection.MPEG4_VIDEO_MIME_TYPES;
            case MiniClientConnection.MSMPEG4_VIDEO:
                return MiniClientConnection.MSMPEG4_VIDEO_MIME_TYPES;
            case MiniClientConnection.H264_VIDEO:
                return MiniClientConnection.H264_VIDEO_MIME_TYPES;
            case MiniClientConnection.VC1_VIDEO:
                return MiniClientConnection.VC1_VIDEO_MIME_TYPES;
            case MiniClientConnection.HEVC_VIDEO:
                return MiniClientConnection.HEVC_VIDEO_MIME_TYPES;
            case MiniClientConnection.MJPEG_VIDEO:
                return MiniClientConnection.MJPEG_VIDEO_MIME_TYPES;
            case MiniClientConnection.VP8_VIDEO:
                return MiniClientConnection.VP8_VIDEO_MIME_TYPES;
            case MiniClientConnection.VP9_VIDEO:
                return MiniClientConnection.VP9_VIDEO_MIME_TYPES;


            default:
                return "";

        }
    }

    //</editor-fold>
}
