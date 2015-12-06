package sagex.miniclient.android;

import android.app.Instrumentation;
import android.content.Context;
import android.view.KeyEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sagex.miniclient.httpbridge.SageTVHttpMediaServerBridge;

/**
 * Created by seans on 17/11/15.
 */
public class AndroidMediaCommandHandler implements SageTVHttpMediaServerBridge.MediaCommandHandler {
    static final Logger log = LoggerFactory.getLogger(AndroidMediaCommandHandler.class);
    private final Context context;
    private final ExecutorService service;
    Instrumentation inst;

    public AndroidMediaCommandHandler(Context context) {
        this.context=context;
        inst = new Instrumentation();
        service = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public boolean onMediaCommand(final SageTVHttpMediaServerBridge.MediaCommand command) {
        if (command.type == SageTVHttpMediaServerBridge.MediaCommand.ActionType.Command) {
            log.debug("Handling Command: " + command.command);
        } else if (command.type == SageTVHttpMediaServerBridge.MediaCommand.ActionType.Key) {
            if (command.modifiers!=0) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        inst.sendKeySync(new KeyEvent(0, 0, KeyEvent.ACTION_UP, command.key, 0, command.modifiers));
                    }
                };
                service.submit(r);
            } else {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        inst.sendKeyDownUpSync(command.key);
                    }
                };
                service.submit(r);
            }

            log.debug("Handling Key: " + command.key);
        }

        return false;
    }
}
