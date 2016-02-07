package sagex.miniclient.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.iconics.view.IconicsImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sagex.miniclient.ServerDiscovery;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.prefs.PrefStore;

/**
 * Created by seans on 20/09/15.
 */
public class ServersActivity extends Activity implements AddServerFragment.OnAddServerListener {
    private static final Logger log = LoggerFactory.getLogger(ServersActivity.class);

    @Bind(R.id.list)
    RecyclerView list;

    @Bind(R.id.header)
    View header;

    @Bind(R.id.btn_add_server)
    IconicsImageView addServerButton;

    @Bind(R.id.btn_settings)
    IconicsImageView settingsButton;

    ServersAdapter adapter = null;
    boolean paused = true;

    public ServersActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppUtil.hideSystemUIOnTV(this);

        super.onCreate(savedInstanceState);


        setContentView(R.layout.servers_layout);

        ButterKnife.bind(this);

        // now show the server selector dialog
        adapter = new ServersAdapter(this);

        //list.setFocusable(true);
        //list.requestFocus();
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list.setHasFixedSize(true);
        list.setAdapter(adapter);

        paused = false;

        header.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {

            }
        });


//        Drawable addIcon = new IconicsDrawable(this)
//                .icon(GoogleMaterial.Icon.gmd_collection_add)
//                .color(Color.RED)
//                .sizeDp(24);
//        addServerButton.setImageDrawable(addIcon);
//
//        Drawable settingsIcon = new IconicsDrawable(this)
//                .icon(GoogleMaterial.Icon.gmd_settings)
//                .color(Color.RED)
//                .sizeDp(24);
//        settingsButton.setImageDrawable(settingsIcon);


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
        AppUtil.hideSystemUIOnTV(this);
        MiniclientApplication.get(this).getClient().eventbus().register(this);
    }

    @Override
    protected void onPause() {
        paused = true;
        MiniclientApplication.get(this).getClient().eventbus().unregister(this);
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
            si.lastConnectTime = System.currentTimeMillis();
            si.save(MiniclientApplication.get().getClient().properties());
            MiniclientApplication.get().getClient().getServers().setLastConnectedServer(si);

            // connect to server
            Intent i = new Intent(ctx, MiniClientGDXActivity.class);
            i.putExtra(MiniClientGDXActivity.ARG_SERVER_INFO, si);

            if (MiniclientApplication.get().getClient().properties().getBoolean(PrefStore.Keys.exit_to_home_screen, true)) {
                log.debug("Starting SageTV with Exit TO Home Screen option");
                i.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }

            ctx.startActivity(i);

        } catch (Throwable t) {
            log.error("Unabled to launch MiniClient Connection to Server {}", si, t);
            Toast.makeText(ctx, "Failed to connect to server: " + t, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_settings)
    public void gotoSettingsAction() {
        Intent i = new Intent(getBaseContext(), SettingsActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.btn_help)
    public void onhelp() {
        HelpDialogFragment.showDialog(this);
    }

    @OnClick(R.id.btn_add_server)
    public void addServerAction() {
        // add new server
        AddServerFragment f = AddServerFragment.newInstance("My Server", "");

        f.setRetainInstance(true);
        f.show(getFragmentManager(), "addserver");
    }

    public void deleteServer(final ServerInfo serverInfo, boolean prompt) {
        if (prompt) {
            AppUtil.confirmAction(this, getString(R.string.title_remove_server), getString(R.string.msg_remove_server), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MiniclientApplication.get(ServersActivity.this).getClient().getServers().deleteServer(serverInfo.name);
                    adapter.items.remove(serverInfo);
                    adapter.notifyDataSetChanged();
                }
            });
        } else {
            MiniclientApplication.get(ServersActivity.this).getClient().getServers().deleteServer(serverInfo.name);
            adapter.items.remove(serverInfo);
            adapter.notifyDataSetChanged();
        }
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
