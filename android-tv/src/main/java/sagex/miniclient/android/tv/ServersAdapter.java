package sagex.miniclient.android.tv;

import android.support.v17.leanback.widget.Presenter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.tv.widgets.SortedObjectAdapter;
import sagex.miniclient.android.util.ServerInfoComparator;

/**
 * Created by seans on 28/02/16.
 */
public class ServersAdapter extends SortedObjectAdapter {
    Logger log = LoggerFactory.getLogger(ServersAdapter.class);

    public ServersAdapter(Presenter presenter) {
        super(ServerInfoComparator.INSTANCE, presenter);
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public Object get(int index) {
        return super.get(index);
    }

    public void addServer(ServerInfo si) {
        int size = size();
        for (int i = 0; i < size; i++) {
            if (si.equals(get(i))) {
                log.debug("Skipping Server, since we already have it: {}", si);
                return;
            }
        }
        log.debug("Adding Server to List, since it does not exist: {}", si);
        add(si);
    }
}
