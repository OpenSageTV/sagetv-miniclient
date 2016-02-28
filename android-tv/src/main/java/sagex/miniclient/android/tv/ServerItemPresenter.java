package sagex.miniclient.android.tv;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import sagex.miniclient.ServerInfo;

/**
 * Created by seans on 27/02/16.
 */
public class ServerItemPresenter extends Presenter {
    private final Context context;
    private TextView serverName;
    private TextView serverAddress;
    private TextView serverLocator;
    private TextView serverLastConnected;

    DateFormat dateFormat = DateFormat.getDateTimeInstance();

    class ServerViewHolder extends ViewHolder {
        public ServerViewHolder(View view) {
            super(view);
            serverName = (TextView) view.findViewById(R.id.server_name);
            serverAddress = (TextView) view.findViewById(R.id.server_address);
            serverLocator = (TextView) view.findViewById(R.id.server_locator_id);
            serverLastConnected = (TextView) view.findViewById(R.id.server_last_connect);
        }

        public void bind(ServerInfo si) {
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
//            if (si.isLocatorOnly()) {
//                icon.setIcon(GoogleMaterial.Icon.gmd_link);
//            } else {
//                icon.setIcon(GoogleMaterial.Icon.gmd_live_tv);
//            }
        }
    }

    public ServerItemPresenter(Context ctx) {
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
