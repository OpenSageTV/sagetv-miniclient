package sagex.miniclient.android;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;

public class FixedTranscodingFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.transcoding_prefs);
    
        final Preference encodingFormat = this.findPreference("fixed_encoding/format");
        
        /*
        encodingFormat.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValye)
            {
                updateSummary(preference, R.string.summary_list_transcoding_formats_preference, newValue);
            }
        });
         */
    }
    
}
