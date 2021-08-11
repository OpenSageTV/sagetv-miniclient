package sagex.miniclient.android.phone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;

import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.tv.R;
import sagex.miniclient.android.util.ServerInfoUtil;
import sagex.miniclient.android.util.ServerInfoUtil.OnAfterCommands;
import sagex.miniclient.util.Utils;

/**
 * Created by seans on 20/09/15.
 */
public class ServersAdapter extends RecyclerView.Adapter<ServersAdapter.ViewHolder> {
    private static final Logger log = LoggerFactory.getLogger(ServersAdapter.class);

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {
        // each data item is just a string in this case
        public View item;
        public TextView serverName;
        public TextView serverAddress;
        public TextView serverLocator;
        public ImageView icon;
        public TextView serverLastConnected;

        public ServerInfo serverInfo;

        DateFormat dateFormat = DateFormat.getDateTimeInstance();

        PopupMenu menu = null;

        OnAfterCommands afterDelete = new OnAfterCommands() {
            @Override
            public void onAfterDelete(ServerInfo serverInfo) {
                log.debug("After Server Delete: {}", serverInfo);
                ((ServersActivity) context).deleteServer(serverInfo);
            }

            @Override
            public void onAfterAdd(ServerInfo serverInfo) {
                log.debug("After Server Add: {}", serverInfo);
                addServer(serverInfo);
            }
        };

        public ViewHolder(View v) {
            super(v);
            this.item = v;

            serverName = (TextView) v.findViewById(R.id.server_name);
            serverAddress = (TextView) v.findViewById(R.id.server_address);
            serverLocator = (TextView) v.findViewById(R.id.server_locator_id);
            icon = (ImageView) v.findViewById(R.id.icon);
            serverLastConnected = (TextView) v.findViewById(R.id.server_last_connect);

            v.setFocusable(true);
            v.setClickable(true);
            v.setLongClickable(true);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
            v.setBackgroundResource(R.drawable.iconbutton_background);
        }

        @Override
        public void onClick(View v) {
            ServerInfoUtil.connect(context, serverInfo);
        }

        @Override
        public boolean onLongClick(View v) {
            if (menu == null) {
                menu = ServerInfoUtil.createContextMenu(context, v, this);
            }

            menu.show();

            return true;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return ServerInfoUtil.onMenuItemClick(context, item, serverInfo, afterDelete);
        }
    }

    private final Context context;
    private final LayoutInflater layoutInflater;
    // List<ServerInfo> items = new ArrayList<>();

    SortedList<ServerInfo> items;

    public ServersAdapter(Context ctx) {
        this.context = ctx;
        this.layoutInflater = LayoutInflater.from(context);

        // get the saved servers, and add them
        items = new SortedList<ServerInfo>(ServerInfo.class, new SortedListAdapterCallback<ServerInfo>(this) {
            @Override
            public int compare(ServerInfo o1, ServerInfo o2) {
                // sort by date accessed and then name
                int compare = 0;
                if (o1.lastConnectTime < o2.lastConnectTime) compare = 1;
                if (o1.lastConnectTime > o2.lastConnectTime) compare = -1;
                if (compare == 0) {
                    return o1.name.compareTo(o2.name);
                }
                return compare;
            }

            @Override
            public boolean areContentsTheSame(ServerInfo oldItem, ServerInfo newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(ServerInfo item1, ServerInfo item2) {
                return item1.equals(item2);
            }
        });

        log.debug("Begin Adding Saved Servers");
        addAll(MiniclientApplication.get(ctx.getApplicationContext()).getClient().getServers().getSavedServers());
        log.debug("End Adding Saved Servers");
    }

    public void addAll(Collection<ServerInfo> newItems) {
        items.beginBatchedUpdates();
        for (ServerInfo item : newItems) {
            items.add(item);
            log.debug("ADDED SERVER: {}", item);
        }
        items.endBatchedUpdates();
    }

    // Create new views (invoked by the codec_selection manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.servers_item, parent, false);
        // set the view's size, margins, paddings and codec_selection parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the codec_selection manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ServerInfo si = getCastedItem(position);
        holder.serverName.setText(si.name);
        if (!Utils.isEmpty(si.address)) {
            holder.serverAddress.setVisibility(View.VISIBLE);
            holder.serverAddress.setText(si.address);
        } else {
            holder.serverAddress.setVisibility(View.GONE);
        }

        if (!Utils.isEmpty(si.locatorID)) {
            holder.serverLocator.setVisibility(View.VISIBLE);
            holder.serverLocator.setText(si.locatorID);
        } else {
            holder.serverLocator.setVisibility(View.GONE);
        }

        if (si.lastConnectTime > 0) {
            holder.serverLastConnected.setText(holder.dateFormat.format(new Date(si.lastConnectTime)));
        } else {
            holder.serverLastConnected.setText("");
        }
        if (si.isLocatorOnly()) {
            holder.icon.setImageResource(R.drawable.ic_add_to_queue_white_60dp);
        } else {
            holder.icon.setImageResource(R.drawable.ic_tv_white_60dp);
        }
        holder.serverInfo = si;
    }

    // Return the size of your dataset (invoked by the codec_selection manager)
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
        log.debug("Attempting to add server: {}", si);
        int size = items.size();
        for (int i = 0; i < size; i++) {
            if (si.equals(items.get(i))) {
                log.debug("Skipping Server, since we already have it: {}", si);
                return;
            }
        }
        log.debug("Adding Server to List, since it does not exist: {}", si);
        items.add(si);
    }
}
