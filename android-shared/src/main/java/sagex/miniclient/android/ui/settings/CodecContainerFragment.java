package sagex.miniclient.android.ui.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import sagex.miniclient.android.R;
import sagex.miniclient.media.AudioCodec;
import sagex.miniclient.media.Container;
import sagex.miniclient.media.VideoCodec;

public class CodecContainerFragment extends PreferenceFragmentCompat
{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.codec_container_prefs, rootKey);

        PreferenceCategory container = findPreference("containers");
        ListPreference containerPref;

        Container [] containers = Container.values();

        for(int i = 0; i < containers.length; i++)
        {
            containerPref = new ListPreference(this.getContext());

            containerPref.setEntryValues(R.array.entryvalues_list_container_preference);
            containerPref.setEntries(R.array.entries_list_container_preference);
            containerPref.setSummary("%s");
            containerPref.setTitle(containers[i].getDescription());
            containerPref.setDialogTitle(containers[i].getDescription());
            containerPref.setKey("container/" + containers[i].getName() + "/support");
            containerPref.setDefaultValue("automatic");

            container.addPreference(containerPref);
        }


        PreferenceCategory video = findPreference("video_codecs");
        ListPreference videoPref;

        VideoCodec [] videoCodecs = VideoCodec.values();

        for(int i = 0; i < videoCodecs.length; i++)
        {
            videoPref = new ListPreference(this.getContext());

            videoPref.setEntryValues(R.array.entryvalues_list_container_preference);
            videoPref.setEntries(R.array.entries_list_container_preference);
            videoPref.setSummary("%s");
            videoPref.setTitle(videoCodecs[i].getDescription());
            videoPref.setDialogTitle(videoCodecs[i].getDescription());
            videoPref.setKey("codec/video/" + videoCodecs[i].getName() + "/support");
            videoPref.setDefaultValue("automatic");

            video.addPreference(videoPref);
        }

        PreferenceCategory audio = findPreference("audio_codecs");
        ListPreference audioPref;

        AudioCodec [] audioCodecs = AudioCodec.values();

        for(int i = 0; i < audioCodecs.length; i++)
        {
            audioPref = new ListPreference(this.getContext());

            audioPref.setEntryValues(R.array.entryvalues_list_container_preference);
            audioPref.setEntries(R.array.entries_list_container_preference);
            audioPref.setSummary("%s");
            audioPref.setTitle(audioCodecs[i].getDescription());
            audioPref.setDialogTitle(audioCodecs[i].getDescription());
            audioPref.setKey("codec/audio/" + audioCodecs[i].getName() + "/support");
            audioPref.setDefaultValue("automatic");

            audio.addPreference(audioPref);
        }


    }
}
