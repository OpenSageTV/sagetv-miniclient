package sagex.miniclient.desktop;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sagex.miniclient.IBus;

public class DesktopBus implements IBus {
    Logger log = LoggerFactory.getLogger(DesktopBus.class);
    EventBus bus = new EventBus();

    public DesktopBus() {
        bus.register(this);
    }

    @Override
    public void register(Object object) {
        bus.register(object);
    }

    @Override
    public void unregister(Object object) {
        bus.unregister(object);
    }

    @Override
    public void post(Object event) {
        bus.post(event);
    }

    public void handleDeadEvent(DeadEvent event) {
        log.warn("Unhandled Event: ", event);
    }
}
