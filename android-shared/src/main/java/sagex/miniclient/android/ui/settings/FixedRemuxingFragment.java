package sagex.miniclient.android.ui.settings;

import android.os.Bundle;
import android.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import sagex.miniclient.android.R;
import sagex.miniclient.android.prefs.AndroidPrefStore;

public class FixedRemuxingFragment extends PreferenceFragmentCompat
{

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {

        setPreferencesFromResource(R.xml.remuxing_prefs, rootKey);

        PreferenceUtils.setDefaultValue(findPreference(AndroidPrefStore.FIXED_REMUXING_PREFERENCE), AndroidPrefStore.FIXED_REMUXING_PREFERENCE_DEFAULT);
        PreferenceUtils.setDefaultValue(findPreference(AndroidPrefStore.FIXED_REMUXING_FORMAT), AndroidPrefStore.FIXED_REMUXING_FORMAT_DEFAULT);
    }


}
