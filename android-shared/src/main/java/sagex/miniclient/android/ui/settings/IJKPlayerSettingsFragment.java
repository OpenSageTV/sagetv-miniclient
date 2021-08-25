package sagex.miniclient.android.ui.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;



import sagex.miniclient.android.R;

public class IJKPlayerSettingsFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ijkplayer_prefs);

        final Preference decoders = findPreference("decoders");
        decoders.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                showHardwareDecoderInfo();
                return true;
            }

        });


    }

    private void showHardwareDecoderInfo()
    {
        CodecDialogFragment.showDialog(getFragmentManager());
    }

}
