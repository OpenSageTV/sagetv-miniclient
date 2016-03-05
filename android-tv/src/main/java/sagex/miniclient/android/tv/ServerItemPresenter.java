package sagex.miniclient.android.tv;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.util.ServerInfoUtil;
import sagex.miniclient.android.util.ServerInfoUtil.OnAfterCommands;

/**
 * Created by seans on 27/02/16.
 */
public class ServerItemPresenter extends Presenter {
    private final Context context;
    private final OnAfterCommands after;

    DateFormat dateFormat = DateFormat.getDateTimeInstance();

    class ServerViewHolder extends ViewHolder {
        private ServerInfo serverInfo;
        private ImageView icon;
        private TextView serverName;
        private TextView serverAddress;
        private TextView serverLocator;
        private TextView serverLastConnected;

        public ServerViewHolder(View view) {
            super(view);
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    PopupMenu menu = ServerInfoUtil.createContextMenu(context, v, new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            return ServerInfoUtil.onMenuItemClick(context, item, serverInfo, after);
                        }
                    });
                    menu.show();
                    return true;
                }
            });
            icon = (ImageView) view.findViewById(R.id.icon);
            serverName = (TextView) view.findViewById(R.id.server_name);
            serverAddress = (TextView) view.findViewById(R.id.server_address);
            serverLocator = (TextView) view.findViewById(R.id.server_locator_id);
            serverLastConnected = (TextView) view.findViewById(R.id.server_last_connect);
        }

        public void bind(ServerInfo si) {
            serverInfo = si;
            serverName.setText(si.name);
            if (!sagex.miniclient.util.Utils.isEmpty(si.address)) {
                serverAddress.setVisibility(View.VISIBLE);
                serverAddress.setText(si.address);
            } else {
                serverAddress.setVisibility(View.GONE);
            }

            if (!sagex.miniclient.util.Utils.isEmpty(si.locatorID)) {
                serverLocator.setVisibility(View.VISIBLE);
                serverLocator.setText(si.locatorID);
            } else {
                serverLocator.setVisibility(View.GONE);
            }

            if (si.lastConnectTime > 0) {
                serverLastConnected.setText(dateFormat.format(new Date(si.lastConnectTime)));
            } else {
                serverLastConnected.setText("");
            }
            if (si.isLocatorOnly()) {
                icon.setImageResource(R.drawable.ic_add_to_queue_white_60dp);
            } else {
                icon.setImageResource(R.drawable.ic_tv_white_60dp);
            }
        }
    }

    public ServerItemPresenter(Context ctx, OnAfterCommands after) {
        this.after = after;
        this.context = ctx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        LayoutInflater li = LayoutInflater.from(context);
        View view = li.inflate(R.layout.server_card, parent, false);
        return new ServerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ((ServerViewHolder) viewHolder).bind((ServerInfo) item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
