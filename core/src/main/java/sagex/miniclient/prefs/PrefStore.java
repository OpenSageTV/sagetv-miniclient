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

    boolean contains(String key);

    boolean canSet(String key);

    interface Keys {
        String image_cache_size_mb = "image_cache_size_mb";
        String disk_image_cache_size_mb = "disk_image_cache_size_mb";

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
         * Use remote buttons change depending on the state of the player
         */
        //String use_stateful_remote = "use_stateful_remote";

        /**
         * values: debug, info, warn, error
         */
        String log_level = "log_level";

        /**
         * if true, then aspect ratio debugging is enabled.
         */
        String debug_ar = "debug_ar";


        /**
         * if enabled the long press select will bring up OSD
         */
        //String long_press_select_for_osd = "long_press_select_for_osd";

        /**
         * Debug Settings
         */
        String debug_log_unmapped_keypresses = "debug_log_unmapped_keypresses";

        /**
         * If set to true, then when the app pauses, it will tear down
         */
        String app_destroy_on_pause = "app_destroy_on_pause";

        /**
         * Boolean: True when ExoPlayer is used
         */
        String use_exoplayer = "use_exoplayer";

        /**
         * Boolean: Announce when Software decoder is being used
         */
        String announce_software_decoder = "announce_software_decoder";

        /**
         * Boolean: use native software decoders over ffmpeg software decoders
         */
        String prefer_android_software_decoders = "prefer_android_software_decoders";

        /**
         * Boolean: if true, then only software decoders are used
         */
        String disable_hardware_decoders = "disable_hardware_decoders";

        /**
         * Boolean: if true, then only software decoders are used
         */
        String disable_audio_passthrough = "disable_audio_passthrough";

        /**
         * Boolean: default is true.  When enabled uses full screen resolution, when
         * disabled, it will report its screen size to be half native.
         */
        String use_native_resolution = "use_native_resolution";

        /**
         * Boolean: default is true.  When true, then when the SageTV exits, you go back to the
         * Android Launcher
         */
        String exit_to_home_screen = "exit_to_home_screen";

        /**
         * Boolean: default is false.  When true, then the Leanback Launcher will be used on a
         * Phone/Tablet
         */
        String use_tv_ui_on_tablet = "use_tv_ui_on_tablet";

        /**
         * How mas in MS the repeated keys will repeat during a key hold
         */
        String repeat_key_ms = "repeat_key_ms";

        /**
         * How long a key is held before repeats will happen
         */
        String repeat_key_delay_ms = "repeat_key_delay_ms";

        /**
         * Client ID
         */
        String client_id = "clientid";
    }
}
