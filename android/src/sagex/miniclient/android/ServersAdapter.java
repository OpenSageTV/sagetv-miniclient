package sagex.miniclient.android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import sagex.miniclient.ServerInfo;

/**
 * Created by seans on 20/09/15.
 */
public class ServersAdapter extends RecyclerView.Adapter<ServersAdapter.ViewHolder> {
    private static final Logger log = LoggerFactory.getLogger(ServersAdapter.class);

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        // each data item is just a string in this case
        public View item;
        public TextView serverName;
        public TextView serverAddress;
        public ServerInfo serverInfo;

        public ViewHolder(View v) {
            super(v);
            this.item = v;
            serverName = (TextView) v.findViewById(android.R.id.text1);
            serverAddress = (TextView) v.findViewById(android.R.id.text2);
            v.setFocusable(true);
            v.setClickable(true);
            v.setLongClickable(true);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
            v.setBackgroundResource(R.drawable.iconbutton_background);
        }

        @Override
        public void onClick(View v) {
            ServersActivity.connect(context, serverInfo);
        }

        @Override
        public boolean onLongClick(View v) {
            ((ServersActivity) context).deleteServer(serverInfo);
            return true;
        }
    }

    private final Context context;
    private final LayoutInflater layoutInflater;
    List<ServerInfo> items = new ArrayList<>();

    public ServersAdapter(Context ctx) {
        this.context = ctx;
        this.layoutInflater = LayoutInflater.from(context);

        // get the saved servers, and add them
        items.addAll(MiniclientApplication.get(ctx.getApplicationContext()).getClient().getServers().getSavedServers());
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ServerInfo si = getCastedItem(position);
        holder.serverAddress.setText(si.address);
        holder.serverName.setText(si.name);
        holder.serverInfo = si;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    public ServerInfo getCastedItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addServer(ServerInfo si) {
        if (!items.contains(si)) {
            log.debug("Adding Server to List, since it does not exist: {}", si);
            items.add(si);
        }
        notifyDataSetChanged();
    }
}
