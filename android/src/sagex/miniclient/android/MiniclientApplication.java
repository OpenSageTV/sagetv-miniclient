package sagex.miniclient.android;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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

        // by default don't use the sdcard
        try {
            AppUtil.initLogging(this, prefs.getBoolean(Prefs.Key.use_log_to_sdcard, false));
            AppUtil.setLogLevel(prefs.getString(Prefs.Key.log_level, "debug"));
        } catch (Throwable t) {
            log.warn("Failed to configure logging", t);
        }

        log.debug("Creating MiniClient");
        log.info("------ Begin CPU INFO -----");
        log.info(getInfo());
        log.info("------- End CPU INFO ------");
        try {
            Intent i = new Intent(getBaseContext(), MiniclientService.class);
            startService(i);
        } catch (Throwable t) {
            log.error("Failed to start MiniClient service", t);
        }
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

    private String getInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("abi: ").append(Build.CPU_ABI).append("\n");
        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {
                    sb.append(aLine + "\n");
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                log.warn("getInfo() failed", e);
            }
        }
        return sb.toString();
    }
}
