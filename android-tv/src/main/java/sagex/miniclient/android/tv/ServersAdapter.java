package sagex.miniclient.android.tv;

import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;

/**
 * Created by seans on 28/02/16.
 */
public class ServersAdapter extends ArrayObjectAdapter {
    public ServersAdapter() {
    }

    public ServersAdapter(Presenter presenter) {
        super(presenter);
    }

    public ServersAdapter(PresenterSelector presenterSelector) {
        super(presenterSelector);
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public Object get(int index) {
        return super.get(index);
    }
}
