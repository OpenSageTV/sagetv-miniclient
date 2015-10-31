package sagex.miniclient.android;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.videolan.libvlc.util.VLCUtil;

/**
 * Created by seans on 12/10/15.
 */
public class MiniclientApplication extends Application {
    static final Logger log = LoggerFactory.getLogger(MiniclientApplication.class);

    Prefs prefs = null;

    public static MiniclientApplication get(Activity ctx) {
        return (MiniclientApplication) ctx.getApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = new Prefs(getPreferences());
        if (!VLCUtil.hasCompatibleCPU(this) || VLCUtil.getMachineSpecs().hasX86) {
            // we don't have x86 for vlc, yet.
            prefs.setBoolean(Prefs.Key.use_vlc, false);
        }

        if (getResources().getBoolean(R.bool.istv)) {
            // log to sdcard on TV
            prefs.setBoolean(Prefs.Key.use_log_to_sdcard, true);
        }

        // by default don't use the sdcard
        AppUtil.initLogging(this, prefs.getBoolean(Prefs.Key.use_log_to_sdcard, false));
        AppUtil.setLogLevel(prefs.getString(Prefs.Key.log_level, "debug"));

        log.debug("Creating MiniClient");
        Intent i = new Intent(getBaseContext(), MiniclientService.class);
        startService(i);
    }

    @Override
    public void onTerminate() {
        log.debug("Destroying MiniClient");
        Intent i = new Intent(getBaseContext(), MiniclientService.class);
        stopService(i);
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public Prefs getPrefs() {
        return prefs;
    }
}
