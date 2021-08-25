package sagex.miniclient.android.ui.settings;

import android.os.Bundle;
import sagex.miniclient.android.R;
import sagex.miniclient.android.prefs.AndroidPrefStore;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

public class FixedTranscodingFragment extends PreferenceFragmentCompat
{

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.transcoding_prefs, rootKey);

        PreferenceUtils.setDefaultValue(findPreference(AndroidPrefStore.FIXED_ENCODING_PREFERENCE), AndroidPrefStore.FIXED_ENCODING_PREFERENCE_DEFAULT);
        PreferenceUtils.setDefaultValue(findPreference(AndroidPrefStore.FIXED_ENCODING_FORMAT), AndroidPrefStore.FIXED_ENCODING_FORMAT_DEFAULT);
        PreferenceUtils.setDefaultValue(findPreference(AndroidPrefStore.FIXED_ENCODING_VIDEO_BITRATE_KBPS), AndroidPrefStore.FIXED_ENCODING_VIDEO_BITRATE_KBPS_DEFAULT + "");
        PreferenceUtils.setDefaultValue(findPreference(AndroidPrefStore.FIXED_ENCODING_FPS), AndroidPrefStore.FIXED_ENCODING_FPS_DEFAULT);
        PreferenceUtils.setDefaultValue(findPreference(AndroidPrefStore.FIXED_ENCODING_VIDEO_RESOLUTION), AndroidPrefStore.FIXED_ENCODING_VIDEO_RESOLUTION_DEFAULT);
        PreferenceUtils.setDefaultValue(findPreference(AndroidPrefStore.FIXED_ENCODING_AUDIO_CODEC),AndroidPrefStore.FIXED_ENCODING_AUDIO_CODEC_DEFAULT);
        PreferenceUtils.setDefaultValue(findPreference(AndroidPrefStore.FIXED_ENCODING_AUDIO_BITRATE_KBPS), AndroidPrefStore.FIXED_ENCODING_AUDIO_BITRATE_KBPS_DEFAULT + "");
        PreferenceUtils.setDefaultValue(findPreference(AndroidPrefStore.FIXED_ENCODING_AUDIO_CHANNELS), AndroidPrefStore.FIXED_ENCODING_AUDIO_CHANNELS_DEFAULT);

    }



}
