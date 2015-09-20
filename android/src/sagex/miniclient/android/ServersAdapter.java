package sagex.miniclient.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sagex.miniclient.ServerInfo;

/**
 * Created by seans on 20/09/15.
 */
public class ServersAdapter extends BaseAdapter {
    private final Context context;
    private final LayoutInflater layoutInflater;
    List<ServerInfo> items = new ArrayList<>();

    public ServersAdapter(Context ctx) {
        this.context=ctx;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    public ServerInfo getCastedItem(int position) {
        return (ServerInfo) getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null) {
            convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
        }
        return bindView(convertView, getCastedItem(position));
    }

    private View bindView(View view, ServerInfo castedItem) {
        // not efficient but, we have so few views
        TextView tv1 = (TextView) view.findViewById(android.R.id.text1);
        TextView tv2 = (TextView) view.findViewById(android.R.id.text2);
        tv1.setText(castedItem.name);
        tv2.setText(castedItem.address);
        return view;
    }

    public void addServer(ServerInfo si) {
        if (!items.contains(si)) {
            items.add(si);
        }
        notifyDataSetChanged();
    }
}
