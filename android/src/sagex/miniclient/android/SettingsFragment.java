package sagex.miniclient.android;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.videolan.libvlc.util.VLCUtil;

/**
 * Created by seans on 24/10/15.
 */
public class SettingsFragment extends PreferenceFragment {
    Prefs prefs = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        try {
            prefs = MiniclientApplication.get(getActivity()).getPrefs();
            if (!VLCUtil.hasCompatibleCPU(getActivity()) || VLCUtil.getMachineSpecs().hasX86) {
                prefs.setBoolean(Prefs.Key.use_vlc, false);
                prefs.setEnabled(this, Prefs.Key.use_vlc, false);
            } else {
                prefs.setEnabled(this, Prefs.Key.use_vlc, true);
            }

            //prefs.setEnabled(this, Prefs.Key.use_log_to_sdcard, !getResources().getBoolean(R.bool.istv));

            Preference p = findPreference(Prefs.Key.use_log_to_sdcard);
            p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    AppUtil.initLogging(SettingsFragment.this.getActivity(), (Boolean) newValue);
                    return true;
                }
            });

            final Preference loglevel = findPreference(Prefs.Key.log_level);
            loglevel.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    AppUtil.setLogLevel((String) newValue);
                    updateLogLevelSummary(loglevel, (String) newValue);
                    return true;
                }
            });
            updateLogLevelSummary(loglevel, prefs.getString(Prefs.Key.log_level, "debug"));

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void updateLogLevelSummary(Preference loglevel, String value) {
        loglevel.setSummary(getResources().getString(R.string.summary_list_loglevels_preference) + " (" + value.toUpperCase() + ")");
    }
}
