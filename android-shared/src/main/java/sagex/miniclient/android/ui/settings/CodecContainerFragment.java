package sagex.miniclient.android.ui.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import sagex.miniclient.android.R;

public class CodecContainerFragment extends PreferenceFragmentCompat
{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.codec_container_prefs, rootKey);
    }
}
