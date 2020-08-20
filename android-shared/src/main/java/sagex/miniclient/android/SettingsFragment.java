package sagex.miniclient.android;

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
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.Format;


import com.google.android.exoplayer2.ext.ffmpeg.FfmpegLibrary;

import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.util.MimeTypes;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

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
    
            Preference fixedTranscoding = this.findPreference("fixed_transcoding");
    
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
            
            final Preference streammode = findPreference(Keys.streaming_mode);
            streammode.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    updateSummary(preference, R.string.summary_list_streaming_mode_preference, newValue);
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
            updateSummary(streammode, R.string.summary_list_streaming_mode_preference, prefs.getString(Keys.streaming_mode, "dynamic"));
            
            final Preference version = findPreference("version");
            version.setSummary(Version.VERSION);
            
            final Preference exoversion = findPreference("exoversion");
            exoversion.setSummary(ExoPlayerLibraryInfo.VERSION);
            
            
            final Preference ipaddress = findPreference("ipaddress");
            ipaddress.setSummary(NetUtil.getIPAddress(true));
            
            Dimension size = getMaxScreenSize();
            final Preference screensize = findPreference("screensize");
            screensize.setSummary(size.getWidth() + "x" + size.getHeight());
            
            final Preference appmemory = findPreference("appmemory");
            appmemory.setSummary(Utils.toMB(Runtime.getRuntime().maxMemory()) + "mb");
            
            final EditTextPreference clientid = (EditTextPreference) findPreference(Keys.client_id);
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
            
            final Preference decoders = findPreference("decoders");
            decoders.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    showHardwareDecoderInfo();
                    return true;
                }
                
            });
    
            final Preference exodecoders = findPreference("show_exo_decoders");
            exodecoders.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    showExoPlayerCodecInfo();
                    return true;
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
    
    private void showExoPlayerCodecInfo()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.exo_decoders);
        StringBuilder sb = new StringBuilder();
        ArrayList<Pair> videoCodecs = new ArrayList<Pair>();
        ArrayList<Pair> audioCodecs = new ArrayList<Pair>();
        MediaCodecInfo mediaCodecInfo;
    
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_H263, "H.263"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_H264, "H.264"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_H265, "H.265"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_MP4, "MP4"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_MP4V, "MP4V"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_MPEG, "MPEG"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_MPEG2, "MPEG2"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_VC1, "VC1"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_VP8, "VP8"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_VP9, "VP9"));
        
        sb.append("<B>").append("Video Decoders").append("</B><br/>\n");
        
        for(int i = 0; i < videoCodecs.size(); i++)
        {
            try
            {
                mediaCodecInfo = MediaCodecUtil.getDecoderInfo((String)videoCodecs.get(i).first, false, false);
                
                
            }
            catch (Exception ex)
            {
                mediaCodecInfo = null;
            }
    
            if (mediaCodecInfo != null)
            {
                sb.append("").append((String)videoCodecs.get(i).second).append(": ").append(mediaCodecInfo.name).append("<br/>\n");
            }
            else
            {
                sb.append("").append((String)videoCodecs.get(i).second).append(": ").append("<i>Unsupported</i>").append("<br/>\n");
            }
        }
    
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_AAC, "AAC"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_AC3, "AC3"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_DTS, "DTS"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_DTS_EXPRESS, "DTS Express"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_DTS_HD, "DTS HD"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_E_AC3, "EAC3"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_E_AC3_JOC, "EAC3 JOC"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_MP4, "MP4"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_FLAC, "FLAC"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_MPEG, "MPEG"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_MPEG_L1, "MPEG L1"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_MPEG_L2, "MPEG_L2"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_VORBIS, "VORBIS"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_RAW, "EAC3"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_TRUEHD, "TRUEHD"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_RAW, "RAW"));
        
        sb.append("<B>").append("Audio Decoders").append("</B><br/>\n");
    
        for(int i = 0; i < audioCodecs.size(); i++)
        {
            try
            {
                mediaCodecInfo = MediaCodecUtil.getDecoderInfo((String)audioCodecs.get(i).first, false, false);
            }
            catch (Exception ex)
            {
                mediaCodecInfo = null;
            }
        
            if (mediaCodecInfo != null)
            {
                sb.append("").append((String)audioCodecs.get(i).second).append(": ").append(mediaCodecInfo.name).append("<br/>\n");
            }
            else
            {
                sb.append("").append((String)audioCodecs.get(i).second).append(": ").append("<i>Unsupported</i>").append("<br/>\n");
            }
        }
    
        sb.append("<B>").append("Audio Decoders (FFMPEG)").append("</B><br/>\n");
        
        
        if(FfmpegLibrary.isAvailable())
        {

            sb.append("DTS: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_DTS)).append("<br/>");
            sb.append("DTS HD: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_DTS_HD)).append("<br/>");
            sb.append("DTS Express: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_DTS_EXPRESS)).append("<br/>");
            sb.append("AC3: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_AC3)).append("<br/>");
            sb.append("EAC3: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_E_AC3)).append("<br/>");
            sb.append("EAC3 JOC: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_E_AC3_JOC)).append("<br/>");
            sb.append("TRUEHD: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_TRUEHD)).append("<br/>");
            sb.append("FLAC: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_FLAC)).append("<br/>");
            sb.append("VORBIS: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_VORBIS)).append("<br/>");
        }
        else
        {
            sb.append("Library is not available");
        }
        
        
        builder.setMessage(Html.fromHtml(sb.toString()));
        builder.setCancelable(true);
        builder.show();
    }
    
    private void showHardwareDecoderInfo()
    {
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
