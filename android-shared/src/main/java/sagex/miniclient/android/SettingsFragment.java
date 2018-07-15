package sagex.miniclient.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.Display;
import android.view.WindowManager;

import sagex.miniclient.Version;
import sagex.miniclient.android.prefs.CodecDialogFragment;
import sagex.miniclient.android.util.NetUtil;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.prefs.PrefStore.Keys;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.util.ClientIDGenerator;
import sagex.miniclient.util.Utils;


/**
 * Created by seans on 24/10/15.
 */
public class SettingsFragment extends PreferenceFragment {
    PrefStore prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        try {
            prefs = MiniclientApplication.get(getActivity()).getClient().properties();

            //prefs.setEnabled(this, Prefs.Key.use_log_to_sdcard, !getResources().getBoolean(R.bool.istv));

            Preference touchPref = this.findPreference("touch_mappings");

            touchPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Intent i = new Intent(SettingsFragment.this.getActivity(), TouchMappingsActivity.class);
                    startActivity(i);

                    return true;
                }
            });

            Preference mediaKeyPref = this.findPreference("media_key_mappings");

            mediaKeyPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(SettingsFragment.this.getActivity(), MediaMappingsActivity.class);
                    startActivity(i);

                    return true;
                }
            });

            Preference p = findPreference(PrefStore.Keys.use_log_to_sdcard);
            p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    AppUtil.initLogging(SettingsFragment.this.getActivity(), (Boolean) newValue);
                    return true;
                }
            });

            final Preference loglevel = findPreference(PrefStore.Keys.log_level);
            loglevel.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    AppUtil.setLogLevel((String) newValue);
                    updateSummary(preference, R.string.summary_list_loglevels_preference, newValue);
                    return true;
                }
            });

            final Preference streammode = findPreference(PrefStore.Keys.log_level);
            streammode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updateSummary(preference, R.string.summary_list_streaming_mode_preference, newValue);
                    return true;
                }
            });

            final Preference memCache = findPreference(Keys.image_cache_size_mb);
            memCache.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (MiniclientApplication.get(getActivity()).getClient().getImageCache()!=null)
                        MiniclientApplication.get(getActivity()).getClient().getImageCache().reloadSettings();
                    return true;
                }
            });

            final Preference diskCache = findPreference(Keys.disk_image_cache_size_mb);
            diskCache.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (MiniclientApplication.get(getActivity()).getClient().getImageCache()!=null)
                        MiniclientApplication.get(getActivity()).getClient().getImageCache().reloadSettings();
                    return true;
                }
            });


            updateSummary(loglevel, R.string.summary_list_loglevels_preference, prefs.getString(PrefStore.Keys.log_level, "debug"));
            updateSummary(streammode, R.string.summary_list_streaming_mode_preference, prefs.getString(PrefStore.Keys.streaming_mode, "dynamic"));

            final Preference version = findPreference("version");
            version.setSummary(Version.VERSION);

            final Preference ipaddress = findPreference("ipaddress");
            ipaddress.setSummary(NetUtil.getIPAddress(true));

            Dimension size = getMaxScreenSize();
            final Preference screensize = findPreference("screensize");
            screensize.setSummary(size.getWidth()+"x"+size.getHeight());

            final Preference appmemory = findPreference("appmemory");
            appmemory.setSummary(Utils.toMB(Runtime.getRuntime().maxMemory())+"mb");

            final EditTextPreference clientid = (EditTextPreference) findPreference(Keys.client_id);
            final ClientIDGenerator gen = new ClientIDGenerator();
            if (prefs.getString(Keys.client_id) == null) {
                prefs.setString(Keys.client_id, gen.generateId());
            }
            //clientid.setSummary(prefs.getString(Keys.client_id) + " (" + gen.id2string(prefs.getString(Keys.client_id)) + ")");
            updateClientIDSummary(clientid, prefs.getString(Keys.client_id), gen);
            clientid.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue==null) return false;
                    String val = (String)newValue;
                    if (val.trim().length()==0) return false;
                    //System.out.println("ONCHANGE: " + val);
                    if (val.indexOf(':')<0) {
                        //System.out.println("ONCHANGE: CONVERT TO ID BEFORE " + val);
                        // user entered text, convert to mac address
                        val = gen.generateId(val);
                        //System.out.println("ONCHANGE: CONVERT TO ID AFTER " + val);
                        prefs.setString(Keys.client_id, val);
                        updateClientIDSummary(preference, val, gen);
                        // return false, so that this text value doesn't get persisted.
                        return false;
                    } else {
                        //System.out.println("ONCHANGE: VAL WAS ID " + val);
                        updateClientIDSummary(preference, val, gen);
                        return true;
                    }
                }
            });

            final Preference decoders = findPreference("decoders");
            decoders.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showHardwareDecoderInfo();
                    return true;
                }

            });


        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void updateClientIDSummary(Preference clientid, String value, ClientIDGenerator gen) {
        clientid.setSummary(value + " (" + gen.id2string(value) + ")");
    }

    private void showHardwareDecoderInfo() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(R.string.hardware_decoders);
//        StringBuilder sb = new StringBuilder();
//        sb.append("<B>").append("Video Decoders").append("</B><br/>\n");
//        for (String mi : AppUtil.getVideoDecoders()) {
//            sb.append(mi).append("<br/>\n");
//        }
//        sb.append("\n<br/><B>").append("Audio Decoders").append("</B><br/>\n");
//        for (String mi : AppUtil.getAudioDecoders()) {
//            sb.append(mi).append("<br/>\n");
//        }
//        builder.setMessage(Html.fromHtml(sb.toString()));
//        builder.setCancelable(true);
//        builder.show();

        CodecDialogFragment.showDialog(getFragmentManager());
    }

    private void updateSummary(Preference pref, int resId, Object value) {
        //pref.setSummary(getResources().getString(resId, value));
    }

    public Dimension getMaxScreenSize() {
        WindowManager wm = (WindowManager) MiniclientApplication.get().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return new Dimension(size.x, size.y);
    }

}
