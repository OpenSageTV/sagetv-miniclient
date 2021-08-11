package sagex.miniclient.android.ui.settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

public class PreferenceUtils
{
    public static void setDefaultValue(Preference preference, String value)
    {
        if(preference != null)
        {
            if (preference instanceof ListPreference)
            {
                ListPreference listPreference = (ListPreference) preference;

                if (listPreference.getValue() == null || listPreference.getValue().equals(""))
                {
                    listPreference.setValue(value);
                }
            }
            else
            {
                //TODO: Log warning....
            }
        }
        else
        {
            //TODO: Log warning....
        }
    }
}
