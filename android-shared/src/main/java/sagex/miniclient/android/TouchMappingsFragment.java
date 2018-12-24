package sagex.miniclient.android;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import sagex.miniclient.SageCommand;
import sagex.miniclient.android.preferences.TouchPreferences;

public class TouchMappingsFragment extends PreferenceFragment
{
    //PrefStore prefs
    private Context context;

    @Override
    public void onAttach (Context context)
    {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ListPreference lp;
        addPreferencesFromResource(R.xml.touch_mappings_prefs);
        TouchPreferences prefs = new TouchPreferences(MiniclientApplication.get(this.getActivity().getApplicationContext()).getClient().properties());


        //One finger
        lp = (ListPreference)this.findPreference("swipe_left");

        if(lp != null)
        {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if(lp.getValue() == null)
            {
                lp.setValue(prefs.getSingleSwipeLeft().getKey());
            }
        }

        lp = (ListPreference)this.findPreference("swipe_right");

        if(lp != null)
        {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if(lp.getValue() == null)
            {
                lp.setValue(prefs.getSingleSwipeRight().getKey());
            }
        }

        lp = (ListPreference)this.findPreference("swipe_up");

        if(lp != null)
        {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if(lp.getValue() == null)
            {
                lp.setValue(prefs.getSingleSwipeUp().getKey());
            }
        }

        lp = (ListPreference)this.findPreference("swipe_down");

        if(lp != null)
        {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if(lp.getValue() == null)
            {
                lp.setValue(prefs.getSingleSwipeDown().getKey());
            }
        }

        //Two fingers
        lp = (ListPreference)this.findPreference("swipe_left_2");

        if(lp != null)
        {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if(lp.getValue() == null)
            {
                lp.setValue(prefs.getDoubleSwipeLeft().getKey());
            }
        }

        lp = (ListPreference)this.findPreference("swipe_right_2");

        if(lp != null)
        {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if(lp.getValue() == null)
            {
                lp.setValue(prefs.getDoubleSwipeRight().getKey());
            }
        }

        lp = (ListPreference)this.findPreference("swipe_up_2");

        if(lp != null)
        {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if(lp.getValue() == null)
            {
                lp.setValue(prefs.getDoubleSwipeUp().getKey());
            }
        }

        lp = (ListPreference)this.findPreference("swipe_down_2");

        if(lp != null)
        {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if(lp.getValue() == null)
            {
                lp.setValue(prefs.getDoubleSwipeDown().getKey());
            }
        }

        //Three fingers
        lp = (ListPreference)this.findPreference("swipe_left_3");

        if(lp != null)
        {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if(lp.getValue() == null)
            {
                lp.setValue(prefs.getTripleSwipeLeft().getKey());
            }
        }

        lp = (ListPreference)this.findPreference("swipe_right_3");

        if(lp != null)
        {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if(lp.getValue() == null)
            {
                lp.setValue(prefs.getTripleSwipeRight().getKey());
            }
        }

        lp = (ListPreference)this.findPreference("swipe_up_3");

        if(lp != null)
        {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if(lp.getValue() == null)
            {
                lp.setValue(prefs.getTrippleSwipeUp().getKey());
            }
        }

        lp = (ListPreference)this.findPreference("swipe_down_3");

        if(lp != null)
        {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            if(lp.getValue() == null)
            {
                lp.setValue(prefs.getTripleSwipeDown().getKey());
            }
        }

    }
}
