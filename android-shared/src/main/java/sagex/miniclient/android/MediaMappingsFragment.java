package sagex.miniclient.android;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.SageCommand;
import sagex.miniclient.android.preferences.MediaMappingPreferences;
import sagex.miniclient.prefs.PrefStore;

public class MediaMappingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
{
    protected Logger log;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        log = LoggerFactory.getLogger(this.getClass());

        SwitchPreference spLongPressOSD;
        SwitchPreference spSmartRemote;
        ListPreference lp;

        addPreferencesFromResource(R.xml.media_mappings_prefs);
        PrefStore store = MiniclientApplication.get(getActivity().getApplicationContext()).getClient().properties();
        MediaMappingPreferences prefs = new MediaMappingPreferences(store);
        MediaMappingPreferences prefsVideoPlaying = new MediaMappingPreferences("videoplaying", store);
        MediaMappingPreferences prefsVideoPaused = new MediaMappingPreferences("videopaused", store);

        /*
        --------------------------------------------------------------------------------------------------------
        DEFAULT MAPPINGS
        --------------------------------------------------------------------------------------------------------
         */

        //DPAD
        bindSageCommandListPreference("default_select", prefs.getSelect().getKey());
        bindSageCommandListPreference("default_left", prefs.getLeft().getKey());
        bindSageCommandListPreference("default_right", prefs.getRight().getKey());
        bindSageCommandListPreference("default_up", prefs.getUp().getKey());
        bindSageCommandListPreference("default_down", prefs.getDown().getKey());

        //Long Press DPAD
        bindSageCommandListPreference("default_select_long_press", prefs.getSelect().getKey());
        bindSageCommandListPreference("default_left_long_press", prefs.getLeftLongPress().getKey());
        bindSageCommandListPreference("default_right_long_press", prefs.getRightLongPress().getKey());
        bindSageCommandListPreference("default_up_long_press", prefs.getUpLongPress().getKey());
        bindSageCommandListPreference("default_down_long_press", prefs.getDownLongPress().getKey());

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
        spLongPressOSD = (SwitchPreference)this.findPreference("long_press_select_for_osd_nav");
        spLongPressOSD.setOnPreferenceChangeListener(this);
        spLongPressOSD.setDefaultValue(prefs.isLongPressSelectShowOSDNav());
        lp = (ListPreference)this.findPreference("default_select_long_press");
        lp.setEnabled(!spLongPressOSD.isChecked());



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


        /*
        --------------------------------------------------------------------------------------------------------
        SMART REMOTE MAPPINGS
        --------------------------------------------------------------------------------------------------------
        */

        spSmartRemote = (SwitchPreference)this.findPreference("smart_remote_mappings");
        spSmartRemote.setOnPreferenceChangeListener(this);
        spSmartRemote.setDefaultValue(prefs.isSmartRemoteEnabled());

        //Video Playing
        PreferenceCategory pc = (PreferenceCategory) this.findPreference("videoplaying");
        pc.setEnabled(spSmartRemote.isChecked());

        //DPAD
        bindSageCommandListPreference("videoplaying_select", prefsVideoPlaying.getSelect().getKey());
        bindSageCommandListPreference("videoplaying_left", prefsVideoPlaying.getLeft().getKey());
        bindSageCommandListPreference("videoplaying_right", prefsVideoPlaying.getRight().getKey());
        bindSageCommandListPreference("videoplaying_up", prefsVideoPlaying.getUp().getKey());
        bindSageCommandListPreference("videoplaying_down", prefsVideoPlaying.getDown().getKey());

        //Long Press DPAD
        bindSageCommandListPreference("videoplaying_select_long_press", prefsVideoPlaying.getSelectLongPress().getKey());

        lp = (ListPreference)this.findPreference("videoplaying_select_long_press");
        lp.setEnabled(!spLongPressOSD.isChecked());

        bindSageCommandListPreference("videoplaying_left_long_press", prefsVideoPlaying.getLeftLongPress().getKey());
        bindSageCommandListPreference("videoplaying_right_long_press", prefsVideoPlaying.getRightLongPress().getKey());
        bindSageCommandListPreference("videoplaying_up_long_press", prefsVideoPlaying.getUpLongPress().getKey());
        bindSageCommandListPreference("videoplaying_down_long_press", prefsVideoPlaying.getDownLongPress().getKey());

        //Video Paused
        pc = (PreferenceCategory) this.findPreference("videopaused");
        pc.setEnabled(spSmartRemote.isChecked());


        //DPAD
        bindSageCommandListPreference("videopaused_select", prefsVideoPaused.getSelect().getKey());
        bindSageCommandListPreference("videopaused_left", prefsVideoPaused.getLeft().getKey());
        bindSageCommandListPreference("videopaused_right", prefsVideoPaused.getRight().getKey());
        bindSageCommandListPreference("videopaused_up", prefsVideoPaused.getUp().getKey());
        bindSageCommandListPreference("videopaused_down", prefsVideoPaused.getDown().getKey());

        //Long Press DPAD
        bindSageCommandListPreference("videopaused_select_long_press", prefsVideoPaused.getSelectLongPress().getKey());

        lp = (ListPreference)this.findPreference("videopaused_select_long_press");
        lp.setEnabled(!spLongPressOSD.isChecked());

        bindSageCommandListPreference("videopaused_left_long_press", prefsVideoPaused.getLeftLongPress().getKey());
        bindSageCommandListPreference("videopaused_right_long_press", prefsVideoPaused.getRightLongPress().getKey());
        bindSageCommandListPreference("videopaused_up_long_press", prefsVideoPaused.getUpLongPress().getKey());
        bindSageCommandListPreference("videopaused_down_long_press", prefsVideoPaused.getDownLongPress().getKey());

        /*
        --------------------------------------------------------------------------------------------------------
        GAME PAD MAPPINGS
        --------------------------------------------------------------------------------------------------------
         */

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

        //smart_remote_mappings
        if(preference.getKey().equals("long_press_select_for_osd_nav"))
        {
            ListPreference lp = (ListPreference)this.findPreference("default_select_long_press");
            lp.setEnabled(!(Boolean)newValue);

            lp = (ListPreference)this.findPreference("videoplaying_select_long_press");
            lp.setEnabled(!(Boolean)newValue);

            lp = (ListPreference)this.findPreference("videopaused_select_long_press");
            lp.setEnabled(!(Boolean)newValue);
        }
        else if(preference.getKey().equals("smart_remote_mappings"))
        {
            //videoplaying
            PreferenceCategory pc = (PreferenceCategory) this.findPreference("videoplaying");
            pc.setEnabled((Boolean)newValue);

            pc = (PreferenceCategory) this.findPreference("videopaused");
            pc.setEnabled((Boolean)newValue);
        }

        return true;
    }
}
