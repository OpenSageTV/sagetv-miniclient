package sagex.miniclient.android.ui.settings;

import android.os.Bundle;
import android.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import sagex.miniclient.android.R;

public class FixedRemuxingFragment extends PreferenceFragmentCompat
{

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        super.onCreate(savedInstanceState);
        setPreferencesFromResource(R.xml.remuxing_prefs, rootKey);
    }


}
