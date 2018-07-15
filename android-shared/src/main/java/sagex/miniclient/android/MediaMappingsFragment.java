package sagex.miniclient.android;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import sagex.miniclient.SageCommand;
import sagex.miniclient.android.preferences.MediaMappingPreferences;
import sagex.miniclient.android.preferences.TouchPreferences;

public class MediaMappingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
{
    protected Logger log;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        log = LoggerFactory.getLogger(this.getClass());

        SwitchPreference sp;
        ListPreference lp;

        addPreferencesFromResource(R.xml.media_mappings_prefs);
        MediaMappingPreferences prefs = new MediaMappingPreferences(this.getActivity().getApplicationContext());

        //DPAD
        bindSageCommandListPreference("default_select", prefs.getSelect().getKey());
        bindSageCommandListPreference("default_left", prefs.getLeft().getKey());
        bindSageCommandListPreference("default_right", prefs.getRight().getKey());
        bindSageCommandListPreference("default_up", prefs.getUp().getKey());
        bindSageCommandListPreference("default_down", prefs.getDown().getKey());

        //Media Playback
        bindSageCommandListPreference("default_play", prefs.getPlay().getKey());
        bindSageCommandListPreference("default_pause", prefs.getPause().getKey());
        bindSageCommandListPreference("default_playpause", prefs.getPlayPause().getKey());
        bindSageCommandListPreference("default_stop", prefs.getStop().getKey());
        bindSageCommandListPreference("default_fastforward", prefs.getFastForward().getKey());
        bindSageCommandListPreference("default_rewind", prefs.getRewind().getKey());
        bindSageCommandListPreference("default_next_track", prefs.getNextTrack().getKey());
        bindSageCommandListPreference("default_previous_track", prefs.getPreviousTrack().getKey());
        bindSageCommandListPreference("default_volume_up", prefs.getVolumeUp().getKey());
        bindSageCommandListPreference("default_volume_down", prefs.getVolumeDown().getKey());
        bindSageCommandListPreference("default_mute", prefs.getMute().getKey());
        bindSageCommandListPreference("default_channel_up", prefs.getChannelUp().getKey());
        bindSageCommandListPreference("default_channel_down", prefs.getChannelDown().getKey());

        bindSageCommandListPreference("default_menu", prefs.getMenu().getKey());
        bindSageCommandListPreference("default_guide", prefs.getGuide().getKey());
        bindSageCommandListPreference("default_info", prefs.getInfo().getKey());
        bindSageCommandListPreference("default_search", prefs.getSearch().getKey());
        bindSageCommandListPreference("default_delete", prefs.getDelete().getKey());

        //Disable long press select if long press select shows OSD Nav
        sp = (SwitchPreference)this.findPreference("long_press_select_for_osd_nav");
        sp.setOnPreferenceChangeListener(this);
        sp.setDefaultValue(prefs.isLongPressSelectShowOSDNav());
        lp = (ListPreference)this.findPreference("default_select_long_press");
        lp.setEnabled(!sp.isChecked());

        //Long Press DPAD
        bindSageCommandListPreference("default_select_long_press", prefs.getSelect().getKey());
        bindSageCommandListPreference("default_left_long_press", prefs.getLeftLongPress().getKey());
        bindSageCommandListPreference("default_right_long_press", prefs.getRightLongPress().getKey());
        bindSageCommandListPreference("default_up_long_press", prefs.getUpLongPress().getKey());
        bindSageCommandListPreference("default_down_long_press", prefs.getDownLongPress().getKey());

        //Number Keys
        bindSageCommandListPreference("default_num_0", prefs.getNum0().getKey());
        bindSageCommandListPreference("default_num_1", prefs.getNum1().getKey());
        bindSageCommandListPreference("default_num_2", prefs.getNum2().getKey());
        bindSageCommandListPreference("default_num_3", prefs.getNum3().getKey());
        bindSageCommandListPreference("default_num_4", prefs.getNum4().getKey());
        bindSageCommandListPreference("default_num_5", prefs.getNum5().getKey());
        bindSageCommandListPreference("default_num_6", prefs.getNum6().getKey());
        bindSageCommandListPreference("default_num_7", prefs.getNum7().getKey());
        bindSageCommandListPreference("default_num_8", prefs.getNum8().getKey());
        bindSageCommandListPreference("default_num_9", prefs.getNum9().getKey());

        //default_yellow
        bindSageCommandListPreference("default_yellow", prefs.getYellow().getKey());
        bindSageCommandListPreference("default_blue", prefs.getBlue().getKey());
        bindSageCommandListPreference("default_red", prefs.getRed().getKey());
        bindSageCommandListPreference("default_green", prefs.getGreen().getKey());

        //default game pad
        bindSageCommandListPreference("default_A", prefs.getA().getKey());
        bindSageCommandListPreference("default_B", prefs.getB().getKey());
        bindSageCommandListPreference("default_X", prefs.getX().getKey());
        bindSageCommandListPreference("default_Y", prefs.getY().getKey());
        bindSageCommandListPreference("default_R1", prefs.getR1().getKey());
        bindSageCommandListPreference("default_R2", prefs.getR2().getKey());
        bindSageCommandListPreference("default_L1", prefs.getL1().getKey());
        bindSageCommandListPreference("default_L2", prefs.getL2().getKey());
        bindSageCommandListPreference("default_gp_start", prefs.getGamepadStart().getKey());
        bindSageCommandListPreference("default_gp_select", prefs.getGamepadSelect().getKey());

    }


    private void bindSageCommandListPreference(String key, String value)
    {
        ListPreference lp = (ListPreference)this.findPreference(key);

        if(lp != null)
        {
            lp.setEntryValues(SageCommand.getKeys(false));
            lp.setEntries(SageCommand.getDisplayNames(false));

            lp.setValue(value);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
        if(preference.getKey().equals("long_press_select_for_osd_nav"))
        {
            ListPreference lp = (ListPreference)this.findPreference("default_select_long_press");
            lp.setEnabled(!(Boolean)newValue);
        }

        return true;
    }
}
