package sagex.miniclient.android;

import android.app.Instrumentation;
import android.content.Context;
import android.os.IBinder;
import android.view.KeyEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import sagex.miniclient.httpbridge.SageTVHttpMediaServerBridge;

/**
 * Created by seans on 17/11/15.
 */
public class AndroidMediaCommandHandler implements SageTVHttpMediaServerBridge.MediaCommandHandler {
    static final Logger log = LoggerFactory.getLogger(AndroidMediaCommandHandler.class);
    private final Context context;

    public AndroidMediaCommandHandler(Context context) {
        this.context=context;
    }

    @Override
    public boolean onMediaCommand(SageTVHttpMediaServerBridge.MediaCommand command) {
        Instrumentation inst = new Instrumentation();
        if (command.type == SageTVHttpMediaServerBridge.MediaCommand.ActionType.Command) {
            log.debug("Handling Command: " + command.command);
        } else if (command.type == SageTVHttpMediaServerBridge.MediaCommand.ActionType.Key) {
            if (command.modifiers!=0) {
                inst.sendKeySync(new KeyEvent(0, 0, KeyEvent.ACTION_UP, command.key, 0, command.modifiers));
            } else {
                inst.sendKeyDownUpSync(command.key);
            }

            log.debug("Handling Key: " + command.key);
        }

        return false;
    }
}
