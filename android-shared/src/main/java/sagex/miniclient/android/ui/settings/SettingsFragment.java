package sagex.miniclient.android.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;


import java.io.File;
import java.io.FilenameFilter;

import sagex.miniclient.Version;
import sagex.miniclient.android.AppUtil;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.R;
import sagex.miniclient.android.prefs.AndroidPrefStore;
import sagex.miniclient.android.util.NetUtil;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.prefs.PrefStore.Keys;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.util.ClientIDGenerator;
import sagex.miniclient.util.Utils;


/**
 * Created by seans on 24/10/15.
 */
public class SettingsFragment extends PreferenceFragment
{
    PrefStore prefs;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        
        try
        {
            prefs = MiniclientApplication.get(getActivity()).getClient().properties();
            
            //prefs.setEnabled(this, Prefs.Key.use_log_to_sdcard, !getResources().getBoolean(R.bool.istv));
            
            Preference p = this.findPreference(Keys.exit_on_standby);
            if (p != null)
            {
                p.setDefaultValue(true);
            }
            
            p = this.findPreference(Keys.app_destroy_on_pause);
            if (p != null)
            {
                p.setDefaultValue(true);
            }

            p = this.findPreference(Keys.disable_sleep);
            if (p != null)
            {
                p.setDefaultValue(true);
            }
            
            p = this.findPreference("reset_to_defaults");
            if (p != null)
            {
                p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                {
                    @Override
                    public boolean onPreferenceClick(Preference preference)
                    {
                        clearAllPreferences();
                        return true;
                    }
                });
            }
            
            
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
            
            mediaKeyPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Intent i = new Intent(SettingsFragment.this.getActivity(), MediaMappingsActivity.class);
                    startActivity(i);
                    
                    return true;
                }
            });
    
            
            
            p = findPreference(Keys.use_log_to_sdcard);
            p.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    AppUtil.initLogging(SettingsFragment.this.getActivity(), (Boolean) newValue);
                    return true;
                }
            });
            
            Preference share_log = findPreference("share_log");
            share_log.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    shareLog();
                    return true;
                }
            });
            
            final Preference loglevel = findPreference(Keys.log_level);
            loglevel.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    AppUtil.setLogLevel((String) newValue);
                    updateSummary(preference, R.string.summary_list_loglevels_preference, newValue);
                    return true;
                }
            });



            final Preference fixedTranscoding = this.findPreference("fixed_transcoding");
            final Preference streammode = findPreference(AndroidPrefStore.STREAMING_MODE);
            fixedTranscoding.setEnabled(prefs.getStreamingMode().equals("fixed"));
            
            streammode.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    updateSummary(preference, R.string.summary_list_streaming_mode_preference, newValue);
                    fixedTranscoding.setEnabled(newValue.equals("fixed"));
                    return true;
                }
            });
    
            
    
            fixedTranscoding.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Intent i = new Intent(SettingsFragment.this.getActivity(), FixedTranscodingActivity.class);
                    startActivity(i);
            
                    return true;
                }
            });

            final Preference fixedRemuxing = this.findPreference("fixed_remuxing");

            fixedRemuxing.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Intent i = new Intent(SettingsFragment.this.getActivity(), FixedRemuxingActivity.class);
                    startActivity(i);

                    return true;
                }
            });

            final Preference containerCodec = this.findPreference("container_codec_settings");

            containerCodec.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Intent i = new Intent(SettingsFragment.this.getActivity(), CodecContainerActivity.class);
                    startActivity(i);

                    return true;
                }
            });

            Preference exoplayerSettingsPref = this.findPreference("exoplayer_settings");

            exoplayerSettingsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Intent i = new Intent(SettingsFragment.this.getActivity(), ExoPlayerSettingsActivity.class);
                    startActivity(i);

                    return true;
                }
            });

            Preference ijkplayerSettingsPref = this.findPreference("ijkplayer_settings");

            ijkplayerSettingsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Intent i = new Intent(SettingsFragment.this.getActivity(), IJKPlayerSettingsActivity.class);
                    startActivity(i);

                    return true;
                }
            });

            final Preference memCache = findPreference(Keys.image_cache_size_mb);
            memCache.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    if (MiniclientApplication.get(getActivity()).getClient().getImageCache() != null)
                    {
                        MiniclientApplication.get(getActivity()).getClient().getImageCache().reloadSettings();
                    }
                    return true;
                }
            });
            
            final Preference diskCache = findPreference(Keys.disk_image_cache_size_mb);
            diskCache.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    if (MiniclientApplication.get(getActivity()).getClient().getImageCache() != null)
                    {
                        MiniclientApplication.get(getActivity()).getClient().getImageCache().reloadSettings();
                    }
                    return true;
                }
            });
            
            
            updateSummary(loglevel, R.string.summary_list_loglevels_preference, prefs.getString(Keys.log_level, "debug"));
            updateSummary(streammode, R.string.summary_list_streaming_mode_preference, prefs.getStreamingMode());
            
            final Preference version = findPreference("version");
            version.setSummary(Version.VERSION);
            
            final Preference ipaddress = findPreference("ipaddress");
            ipaddress.setSummary(NetUtil.getIPAddress(true));
            
            Dimension size = getMaxScreenSize();
            final Preference screensize = findPreference("screensize");
            screensize.setSummary(size.getWidth() + "x" + size.getHeight());
            
            final Preference appmemory = findPreference("appmemory");
            appmemory.setSummary(Utils.toMB(Runtime.getRuntime().maxMemory()) + "mb");



            final Preference clientid = (Preference) findPreference(Keys.client_id);


            final ClientIDGenerator gen = new ClientIDGenerator();

            if (prefs.getString(Keys.client_id) == null)
            {
                prefs.setString(Keys.client_id, gen.generateId());
            }



            //clientid.setSummary(prefs.getString(Keys.client_id) + " (" + gen.id2string(prefs.getString(Keys.client_id)) + ")");
            updateClientIDSummary(clientid, prefs.getString(Keys.client_id), gen);
            clientid.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    if (newValue == null)
                    {
                        return false;
                    }
                    String val = (String) newValue;
                    if (val.trim().length() == 0)
                    {
                        return false;
                    }
                    //System.out.println("ONCHANGE: " + val);
                    if (val.indexOf(':') < 0)
                    {
                        //System.out.println("ONCHANGE: CONVERT TO ID BEFORE " + val);
                        // user entered text, convert to mac address
                        val = gen.generateId(val);
                        //System.out.println("ONCHANGE: CONVERT TO ID AFTER " + val);
                        prefs.setString(Keys.client_id, val);
                        updateClientIDSummary(preference, val, gen);
                        // return false, so that this text value doesn't get persisted.
                        return false;
                    }
                    else
                    {
                        //System.out.println("ONCHANGE: VAL WAS ID " + val);
                        updateClientIDSummary(preference, val, gen);
                        return true;
                    }
                }
            });

        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
    
    void updateClientIDSummary(Preference clientid, String value, ClientIDGenerator gen)
    {
        clientid.setSummary(value + " (" + gen.id2string(value) + ")");
    }


    
    private void updateSummary(Preference pref, int resId, Object value)
    {
        //pref.setSummary(getResources().getString(resId, value));
    }
    
    public Dimension getMaxScreenSize()
    {
        WindowManager wm = (WindowManager) MiniclientApplication.get().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return new Dimension(size.x, size.y);
    }
    
    private void shareLog()
    {
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        File logDir = AppUtil.getLogDir();
        
        File[] files = logDir.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return (name.endsWith(".txt"));
            }
        });
        
        if (files == null || files.length == 0)
        {
            Log.i("MINICLIENT_LOG", "No Files to share in " + logDir.getAbsolutePath());
            return;
        }
        
        File fileToShare = files[0];
        Log.i("MINICLIENT_LOG", "Sharing " + fileToShare.getAbsolutePath());
        
        if (fileToShare.exists())
        {
            intentShareFile.setType("application/text");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse(fileToShare.toURI().toString()));
            
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                    "Sharing MiniClient Log File...");
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing MiniClient Log File...");
            
            startActivity(Intent.createChooser(intentShareFile, "Share MiniClient Log File"));
        }
    }
    
    void clearAllPreferences()
    {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().apply();
        Toast.makeText(getActivity(), "Preferences have been reset to defaults", Toast.LENGTH_LONG).show();
    }
}
