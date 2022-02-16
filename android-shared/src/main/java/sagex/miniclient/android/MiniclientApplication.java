package sagex.miniclient.android;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import sagex.miniclient.MiniClient;
import sagex.miniclient.prefs.PrefStore;

/**
 * Created by seans on 12/10/15.
 */
public class MiniclientApplication extends Application
{
    MiniClient client = null;
    static final Logger log = LoggerFactory.getLogger(MiniclientApplication.class);
    private static MiniclientApplication INSTANCE = null;
    private int versionCode;
    private String versionName;

    public static MiniclientApplication get() {
        return INSTANCE;
    }

    public static MiniclientApplication get(Context ctx)
    {
        if (ctx == null) return get();
        return (MiniclientApplication) ctx.getApplicationContext();
    }

    public MiniClient getClient()
    {
        return client;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        MiniclientApplication.INSTANCE = this;
        AndroidMiniClientOptions options = new AndroidMiniClientOptions(this);
        PackageManager manager = this.getPackageManager();

        try
        {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
            versionCode = info.versionCode;
            versionName = info.versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            versionCode = -1;
            versionName = "";
            log.warn("Unable to get Version Vode or Version Vame: " + e.getMessage());
        }

        // by default don't use the sdcard
        try
        {
            AppUtil.initLogging(this, options.getPrefs().getBoolean(PrefStore.Keys.use_log_to_sdcard, false));
            AppUtil.setLogLevel(options.getPrefs().getString(PrefStore.Keys.log_level, "warn"));
        }
        catch (Throwable t)
        {
            log.warn("Failed to configureList logging", t);
        }

        // start the client instance
        client = new MiniClient(options);

        try
        {
            Intent i = new Intent(getBaseContext(), MiniclientService.class);
            startService(i);
        }
        catch (Throwable t)
        {
            log.error("Failed to start MiniClient service", t);
        }

        log.debug("-------- LAYOUT: {} ---------", getResources().getString(R.string.layout));
    }

    @Override
    public void onTerminate()
    {
        log.debug("Destroying MiniClient");
        Intent i = new Intent(getBaseContext(), MiniclientService.class);
        stopService(i);
        super.onTerminate();
        MiniclientApplication.INSTANCE = null;
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
    }

    public int getVersionCode()
    {
        return versionCode;
    }

    public String getVersionName()
    {
        return versionName;
    }

    private String getInfo()
    {
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
