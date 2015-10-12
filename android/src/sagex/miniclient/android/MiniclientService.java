package sagex.miniclient.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.MiniClient;

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
        MiniClient client = MiniClient.get();
        client.init(getFilesDir(), getCacheDir());
        if (client.isUsingHttpBridge()) {
            // start the http bridge
            client.getHttpBridge();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.debug("Stopping MiniClient Service");
        MiniClient.get().shutdown();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // not used
        return null;
    }
}
