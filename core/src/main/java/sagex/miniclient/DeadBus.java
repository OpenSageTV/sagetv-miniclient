package sagex.miniclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by seans on 05/12/15.
 */
public class DeadBus implements IBus {
    static final Logger log = LoggerFactory.getLogger(DeadBus.class);

    public static final IBus INSTANCE = new DeadBus();

    @Override
    public void register(Object object) {
        log.warn("Attempt to register events on a dead bus with object {}", object, new Exception("DeadBus.register()"));
    }

    @Override
    public void unregister(Object object) {

    }

    @Override
    public void post(Object event) {
        log.warn("Attempt to post event on a dead bus with event {}", event, new Exception("DeadBus.register()"));
    }
}
