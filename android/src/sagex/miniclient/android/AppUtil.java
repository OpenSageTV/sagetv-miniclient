package sagex.miniclient.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by seans on 27/09/15.
 */
public class AppUtil {
    public static String getMACAddress(Context ctx) {
        try {
            WifiManager manager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            String mac = info.getMacAddress();
            if (mac == null) throw new Exception("No WIFI, Will Try eth0");
            return mac;
        } catch (Throwable t) {
            return getMacAddressForEth0();
        }
    }

    static String loadFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    /*
     * Get the STB MacAddress
     */
    static String getMacAddressForEth0() {
        try {
            return loadFileAsString("/sys/class/net/eth0/address")
                    .toUpperCase().substring(0, 17);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void confirmExit(final Activity act) {
        new AlertDialog.Builder(act)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing MiniClient")
                .setMessage("Are you sure you want to close the SageTV MiniClient?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        act.finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    // This snippet hides the system bars.
    public static void hideSystemUI(Activity act) {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        act.getWindow().getDecorView().setSystemUiVisibility(
                //View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        //| View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    public static void showSystemUI(Activity act) {
        act.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public static void setLoggingLevel(ch.qos.logback.classic.Level level) {
//        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
//        root.setLevel(level);
    }

    public static void initLogging(Context ctx) {
//        BasicLogcatConfigurator.configureDefaultContext();
//        setLoggingLevel(Level.DEBUG);

        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
//        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//        lc.reset();
//
//        JoranConfigurator config = new JoranConfigurator();
//        config.setContext(lc);
//
//        try {
//            config.doConfigure(ctx.getAssets().open("logback.xml"));
//            //config.doConfigure("/path/to/config.xml");
//        } catch (JoranException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
