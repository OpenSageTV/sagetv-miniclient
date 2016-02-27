package sagex.miniclient.android;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;

import sagex.miniclient.ServerInfo;
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
        public IconicsImageView icon;
        public TextView serverLastConnected;

        public ServerInfo serverInfo;

        DateFormat dateFormat = DateFormat.getDateTimeInstance();

        PopupMenu menu = null;

        public ViewHolder(View v) {
            super(v);
            this.item = v;

            serverName = (TextView) v.findViewById(R.id.server_name);
            serverAddress = (TextView) v.findViewById(R.id.server_address);
            serverLocator = (TextView) v.findViewById(R.id.server_locator_id);
            icon = (IconicsImageView) v.findViewById(R.id.icon);
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
            ServersActivity.connect(context, serverInfo);
        }

        @Override
        public boolean onLongClick(View v) {
            if (menu == null) {
                menu = new PopupMenu(context, item);
                Menu m = menu.getMenu();
                menu.inflate(R.menu.server_actions);
                menu.setOnMenuItemClickListener(this);
            }

            menu.show();

            return true;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.menu_remove) {
                ((ServersActivity) context).deleteServer(serverInfo, true);
            } else if (item.getItemId() == R.id.menu_change_name) {
                onChangeName();
            } else if (item.getItemId() == R.id.menu_connect) {
                ServersActivity.connect(context, serverInfo);
            } else if (item.getItemId() == R.id.menu_connect_locator) {
                if (!Utils.isEmpty(serverInfo.locatorID)) {
                    ServerInfo newSI = serverInfo.clone();
                    newSI.forceLocator = true;
                    ServersActivity.connect(context, newSI);
                } else {
                    Toast.makeText(context, context.getString(R.string.msg_no_locator), Toast.LENGTH_LONG).show();
                }
            }
            return true;
        }

        private void onChangeName() {
            AppUtil.prompt(context, "Change Server Name", "Enter new Server Name", serverInfo.name, new AppUtil.OnValueChangeListener() {
                @Override
                public void onValueChanged(String oldValue, String newValue) {
                    ServerInfo newSI = serverInfo.clone();
                    newSI.name = newValue;

                    // remove the old server
                    ((ServersActivity) context).deleteServer(serverInfo, false);

                    // save the new server
                    newSI.save(MiniclientApplication.get().getClient().properties());

                    addServer(newSI);
                }
            });
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

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.servers_item, parent, false);
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
            holder.icon.setIcon(GoogleMaterial.Icon.gmd_link);
        } else {
            holder.icon.setIcon(GoogleMaterial.Icon.gmd_live_tv);
        }
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
