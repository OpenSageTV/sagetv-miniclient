package sagex.miniclient.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by seans on 12/10/15.
 */
public class MiniclientService extends Service {
    private static final Logger log = LoggerFactory.getLogger(MiniclientService.class);

    public MiniclientService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log.debug("Starting MiniClient Service");
        try {
            MiniclientApplication.get(this).getClient();
        } catch (Throwable t) {
            log.error("Failed to start miniclient service", t);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.debug("Stopping MiniClient Service");
        try {
            MiniclientApplication.get(this).getClient().shutdown();
        } catch (Throwable t) {
            log.warn("Failed to shutdown MiniClient service", t);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // not used
        return null;
    }
}
