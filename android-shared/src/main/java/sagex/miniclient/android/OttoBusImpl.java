package sagex.miniclient.android;

import com.squareup.otto.Bus;

import sagex.miniclient.IBus;

/**
 * Created by seans on 05/12/15.
 */
public class OttoBusImpl implements IBus {
    private final Bus bus;

    public OttoBusImpl() {
        this(new Bus());
    }

    public OttoBusImpl(Bus bus) {
        this.bus = bus;
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
}
