package sagex.miniclient.android;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.ServerDiscovery;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.prefs.PrefStore;

/**
 * Created by seans on 20/09/15.
 */
public class ServersActivity extends Activity implements AdapterView.OnItemClickListener, AddServerFragment.OnAddServerListener, AdapterView.OnItemLongClickListener {
    private static final Logger log = LoggerFactory.getLogger(ServersActivity.class);

    ListView list = null;
    ServersAdapter adapter = null;
    boolean paused = true;

    public ServersActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
        );

        setContentView(R.layout.servers_layout);

        // now show the server selector dialog
        adapter = new ServersAdapter(this);

        list = (ListView) findViewById(R.id.list);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);
        list.setFocusable(true);
        list.requestFocus();
        list.setAdapter(adapter);

        paused = false;

        if (MiniclientApplication.get(this).getClient().properties().getBoolean(PrefStore.Keys.auto_connect_to_last_server, false)) {
            ServerInfo si = MiniclientApplication.get(this).getClient().getServers().getLastConnectedServer();
            if (si != null) {
                // show the connect dialog
                AutoConnectDialog dialog = new AutoConnectDialog();
                dialog.show(getFragmentManager(), "autoconnect");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        refreshServers();
    }

    @Override
    protected void onPause() {
        paused = true;
        MiniclientApplication.get(this).getClient().getServerDiscovery().close();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        paused = true;
        super.onDestroy();
    }

    public void refreshServers() {
        log.debug("Looking for Servers...");
        MiniclientApplication.get(this).getClient().getServerDiscovery().discoverServersAsync(10000, new ServerDiscovery.ServerDiscoverCallback() {
            @Override
            public void serverDiscovered(final ServerInfo si) {
                if (!paused) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.addServer(si);
                        }
                    });
                }
            }
        });
    }

    public static void connect(Context ctx, ServerInfo si) {
        try {
            // connect to server
            Intent i = new Intent(ctx, MiniClientGDXActivity.class);
            i.putExtra(MiniClientGDXActivity.ARG_SERVER_INFO, si);
            ctx.startActivity(i);

            si.lastConnectTime = System.currentTimeMillis();
            si.save(MiniclientApplication.get().getClient().properties());
            MiniclientApplication.get().getClient().getServers().setLastConnectedServer(si);
        } catch (Throwable t) {
            log.error("Unabled to launch MiniClient Connection to Server {}", si, t);
            Toast.makeText(ctx, "Failed to connect to server: " + t, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ServerInfo si = adapter.getCastedItem(position);
        if (ServersAdapter.NEW_SERVER_ID.equals(si.name)) {
            // add new server
            AddServerFragment f = AddServerFragment.newInstance("My Server", "");
            f.setRetainInstance(true);
            f.show(getFragmentManager(), "addserver");
        } else if (ServersAdapter.PREFERENCES_ID.equals(si.name)) {
            Intent i = new Intent(getBaseContext(), SettingsActivity.class);
            startActivity(i);
        } else {
            connect(this, si);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        if (ServersAdapter.NEW_SERVER_ID.equals(adapter.getCastedItem(position).name)) {
            // can't delete the "New Server" item
            return true;
        }

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog);
        builder.setTitle("Remove Server");
        builder.setMessage("Click OK to remove server");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MiniclientApplication.get(ServersActivity.this).getClient().getServers().deleteServer(adapter.getCastedItem(position).name);
                adapter.items.remove(position);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
        return true;
    }

    @Override
    public void onAddServer(String name, String addr) {
        if (addr != null && addr.trim().length() > 0) {
            ServerInfo si = new ServerInfo();
            si.name = name;
            si.address = addr;
            MiniclientApplication.get(this).getClient().getServers().saveServer(si);
            adapter.addServer(si);
            Toast.makeText(this, "Server Added", Toast.LENGTH_LONG).show();
        }
    }

}
