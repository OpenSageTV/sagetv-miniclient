package sagex.miniclient.prefs;

import java.util.Set;

/**
 * Simple Abstract way of handling Preferences
 */
public interface PrefStore {
    String getString(String key);

    String getString(String key, String defValue);

    void setString(String key, String value);

    long getLong(String key);

    long getLong(String key, long defValue);

    void setLong(String key, long value);

    int getInt(String key);

    int getInt(String key, int defValue);

    void setInt(String key, int value);

    double getDouble(String key);

    double getDouble(String key, double defValue);

    void setDouble(String key, double value);

    boolean getBoolean(String key);

    boolean getBoolean(String key, boolean defValue);

    void setBoolean(String key, boolean value);

    Set<Object> keys();

    void remove(String key);

    interface Keys {
        String image_cache_size = "image_cache_size";
        String disk_image_cache_size = "disk_image_cache_size";
        String cache_images_on_disk = "cache_images_on_disk";
        String use_bitmap_images = "use_bitmap_images";

        /**
         * values: high, med, low
         */
        String local_fs_security = "local_fs_security";
        String mplayer_extra_video_codecs = "mplayer/extra_video_codecs";
        String mplayer_extra_audio_codecs = "mplayer/extra_audio_codecs";

        /**
         * values: dynamic, fixed, pull
         */
        String streaming_mode = "streaming_mode";

        /**
         * 000 will be added to this value, so we only set, 64 to mean 64,000
         */
        String fixed_encoding_video_bitrate_kbps = "fixed_encoding/video_bitrate_kbps";
        /**
         * 000 will be added to this value, so we only set, 64 to mean 64,000
         */
        String fixed_encoding_audio_bitrate_kbps = "fixed_encoding/audio_bitrate_kbps";

        String fixed_encoding_fps = "fixed_encoding/fps";
        String fixed_encoding_key_frame_interval = "fixed_encoding/key_frame_interval";
        String fixed_encoding_use_b_frames = "fixed_encoding/use_b_frames";
        String fixed_encoding_video_resolution = "fixed_encoding/video_resolution";
        String video_buffer_size = "video_buffer_size";
        String audio_buffer_size = "audio_buffer_size";

        // auto connect settings
        String auto_connect_to_last_server = "auto_connect_to_last_server";
        String auto_connect_delay = "auto_connect_delay";
        String last_connected_server = "last_connected_server";

        /**
         * Log to file
         */
        String use_log_to_sdcard = "use_log_to_sdcard";

        /**
         * values: debug, info, warn, error
         */
        String log_level = "log_level";

        /**
         * if enabled the long press select will bring up OSD
         */
        String long_press_select_for_osd = "long_press_select_for_osd";

        String use_hardware_acceleration = "use_hardware_acceleration";

        /**
         * Debug Settings
         */
        String debug_log_unmapped_keypresses = "debug_log_unmapped_keypresses";

        /**
         * If set to true, then when the app pauses, it will tear down
         */
        String app_destroy_on_pause = "app_destroy_on_pause";

        /**
         * SageTV GetProperty Keys
         */
        java.lang.String GFX_SURFACES = "GFX_SURFACES";
    }
}
