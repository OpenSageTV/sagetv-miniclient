package sagex.miniclient.android;

import android.app.Application;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by seans on 12/10/15.
 */
public class MiniclientApplication extends Application {
    static final Logger log = LoggerFactory.getLogger(MiniclientApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();

        AppUtil.initLogging(this);

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
}
