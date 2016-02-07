package sagex.miniclient.android;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
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
        @Bind(R.id.server_name)
        public TextView serverName;
        @Bind(R.id.server_address)
        public TextView serverAddress;
        @Bind(R.id.server_locator_id)
        public TextView serverLocator;
        @Bind(R.id.icon)
        public IconicsImageView icon;
        @Bind(R.id.server_last_connect)
        public TextView serverLastConnected;

        public ServerInfo serverInfo;

        DateFormat dateFormat = DateFormat.getDateTimeInstance();

        PopupMenu menu = null;

        public ViewHolder(View v) {
            super(v);
            this.item = v;
            ButterKnife.bind(this, v);
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
            switch (item.getItemId()) {
                case R.id.menu_remove:
                    ((ServersActivity) context).deleteServer(serverInfo, true);
                    break;
                case R.id.menu_change_name:
                    onChangeName();
                    break;
                case R.id.menu_connect:
                    ServersActivity.connect(context, serverInfo);
                    break;
                case R.id.menu_connect_locator:
                    if (!Utils.isEmpty(serverInfo.locatorID)) {
                        ServerInfo newSI = serverInfo.clone();
                        // clear the address to force a locator connection
                        newSI.address = null;
                        ServersActivity.connect(context, newSI);
                    } else {
                        Toast.makeText(context, context.getString(R.string.msg_no_locator), Toast.LENGTH_LONG).show();
                    }
                    break;
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
    List<ServerInfo> items = new ArrayList<>();

    public ServersAdapter(Context ctx) {
        this.context = ctx;
        this.layoutInflater = LayoutInflater.from(context);

        // get the saved servers, and add them
        items.addAll(MiniclientApplication.get(ctx.getApplicationContext()).getClient().getServers().getSavedServers());
        for (ServerInfo si : items) {
            log.debug("SAVED SERVER: {}", si);
        }
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
        if (!items.contains(si)) {
            log.debug("Adding Server to List, since it does not exist: {}", si);
            items.add(si);
        }
        notifyDataSetChanged();
    }
}
