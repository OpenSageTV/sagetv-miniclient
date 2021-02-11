package sagex.miniclient.android.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;

import sagex.miniclient.SageCommand;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.R;
import sagex.miniclient.android.preferences.TouchPreferences;

public class TouchMappingsFragment extends PreferenceFragment {
    //PrefStore prefs
    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListPreference lp;
        addPreferencesFromResource(R.xml.touch_mappings_prefs);
        TouchPreferences prefs = new TouchPreferences(MiniclientApplication.get(this.getActivity().getApplicationContext()).getClient().properties());


        //One finger
        lp = (ListPreference) this.findPreference("swipe_left");

        if (lp != null) {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if (lp.getValue() == null) {
                lp.setValue(prefs.getSingleSwipeLeft().getKey());
            }
        }

        lp = (ListPreference) this.findPreference("swipe_right");

        if (lp != null) {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if (lp.getValue() == null) {
                lp.setValue(prefs.getSingleSwipeRight().getKey());
            }
        }

        lp = (ListPreference) this.findPreference("swipe_up");

        if (lp != null) {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if (lp.getValue() == null) {
                lp.setValue(prefs.getSingleSwipeUp().getKey());
            }
        }

        lp = (ListPreference) this.findPreference("swipe_down");

        if (lp != null) {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if (lp.getValue() == null) {
                lp.setValue(prefs.getSingleSwipeDown().getKey());
            }
        }

        //Two fingers
        lp = (ListPreference) this.findPreference("swipe_left_2");

        if (lp != null) {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if (lp.getValue() == null) {
                lp.setValue(prefs.getDoubleSwipeLeft().getKey());
            }
        }

        lp = (ListPreference) this.findPreference("swipe_right_2");

        if (lp != null) {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if (lp.getValue() == null) {
                lp.setValue(prefs.getDoubleSwipeRight().getKey());
            }
        }

        lp = (ListPreference) this.findPreference("swipe_up_2");

        if (lp != null) {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if (lp.getValue() == null) {
                lp.setValue(prefs.getDoubleSwipeUp().getKey());
            }
        }

        lp = (ListPreference) this.findPreference("swipe_down_2");

        if (lp != null) {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if (lp.getValue() == null) {
                lp.setValue(prefs.getDoubleSwipeDown().getKey());
            }
        }

        //Three fingers
        lp = (ListPreference) this.findPreference("swipe_left_3");

        if (lp != null) {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if (lp.getValue() == null) {
                lp.setValue(prefs.getTripleSwipeLeft().getKey());
            }
        }

        lp = (ListPreference) this.findPreference("swipe_right_3");

        if (lp != null) {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if (lp.getValue() == null) {
                lp.setValue(prefs.getTripleSwipeRight().getKey());
            }
        }

        lp = (ListPreference) this.findPreference("swipe_up_3");

        if (lp != null) {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if (lp.getValue() == null) {
                lp.setValue(prefs.getTrippleSwipeUp().getKey());
            }
        }

        lp = (ListPreference) this.findPreference("swipe_down_3");

        if (lp != null) {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if (lp.getValue() == null) {
                lp.setValue(prefs.getTripleSwipeDown().getKey());
            }
        }

        configureList(prefs, "double_tap", prefs.getOnDoubleTap());
        configureList(prefs, "double_tap_2", prefs.getOnDoubleTap2());
        configureList(prefs, "double_tap_3", prefs.getOnDoubleTap3());

        configureList(prefs, "edge_swipe_left", prefs.getEdgeSwipeLeft());
        configureList(prefs, "edge_swipe_right", prefs.getEdgeSwipeRight());
        configureList(prefs, "edge_swipe_top", prefs.getEdgeSwipeTop());
        configureList(prefs, "edge_swipe_bottom", prefs.getEdgeSwipeBottom());

        configureList(prefs, "hotspot_bottom_right", prefs.getHotspotBottomRight());
        configureList(prefs, "hotspot_bottom_left", prefs.getHotspotBottomLeft());
        configureList(prefs, "hotspot_top_right", prefs.getHotspotTopRight());
        configureList(prefs, "hotspot_top_left", prefs.getHotspotTopLeft());

        configureList(prefs, "long_press", prefs.getLongPress());
        configureList(prefs, "long_press_2", prefs.getDoubleLongPress());
        configureList(prefs, "long_press_3", prefs.getTripleLongPress());

        configureInt("edge_size", prefs.getEdgeSizePixels());
        configureInt("hotspot_size", prefs.getHotspotSizePixels());
    }

    void configureInt(String key, int val) {
        final EditTextPreference lp = (EditTextPreference) this.findPreference(key);

        if (lp != null) {
            lp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    lp.setSummary(String.valueOf(newValue));
                    return true;
                }
            });
            lp.setSummary(String.valueOf(val));
        } else {
            Log.d("KEYS", "Missing Preference In Xml for " + key);
        }
    }

    void configureList(TouchPreferences prefs, String key, SageCommand command) {
        ListPreference lp = (ListPreference) this.findPreference(key);

        if (lp != null) {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if (lp.getValue() == null) {
                lp.setValue(command.getKey());
            }
        } else {
            Log.d("KEYS", "Missing Preference In Xml for " + key);
        }
    }
}
