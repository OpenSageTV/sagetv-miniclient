package sagex.miniclient.android.phone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.ServerDiscovery;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.AddServerFragment;
import sagex.miniclient.android.AddServerFragment.OnAddServerListener;
import sagex.miniclient.android.AppUtil;
import sagex.miniclient.android.AutoConnectDialog;
import sagex.miniclient.android.HelpDialogFragment;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.SettingsActivity;
import sagex.miniclient.android.tv.MainActivity;
import sagex.miniclient.android.tv.R;
import sagex.miniclient.android.util.AudioUtil;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.prefs.PrefStore.Keys;

/**
 * Created by seans on 20/09/15.
 */
public class ServersActivity extends Activity implements OnAddServerListener {
    private static final Logger log = LoggerFactory.getLogger(ServersActivity.class);

    RecyclerView list;
    View header;
    ImageView addServerButton;
    ImageView settingsButton;

    ServersAdapter adapter = null;
    boolean paused = true;

    public ServersActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppUtil.hideSystemUIOnTV(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.servers_layout);

        if (getResources().getBoolean(R.bool.istv)) {
            // server activity started on a TV
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return;
            }
        }

        if (MiniclientApplication.get().getClient().properties().getBoolean(Keys.use_tv_ui_on_tablet, false)) {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
            return;
        }

        list = findViewById(R.id.list);
        header = findViewById(R.id.header);
        addServerButton = findViewById(R.id.btn_add_server);
        settingsButton = findViewById(R.id.btn_settings);

        settingsButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoSettingsAction();
            }
        });

        findViewById(R.id.btn_help).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onhelp();
            }
        });

        addServerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addServerAction();
            }
        });

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
        AudioUtil.requestAudioFocus(this);
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
        // refresh the data in case last connected changed, etc
        adapter.notifyDataSetChanged();

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

    // @OnClick(R.id.btn_settings)
    public void gotoSettingsAction() {
        Intent i = new Intent(getBaseContext(), SettingsActivity.class);
        startActivity(i);
    }

    // @OnClick(R.id.btn_help)
    public void onhelp() {
        HelpDialogFragment.showDialog(this);
    }

    // @OnClick(R.id.btn_add_server)
    public void addServerAction() {
        // add new server
        AddServerFragment f = AddServerFragment.newInstance("My Server", "");

        f.setRetainInstance(true);
        f.show(getFragmentManager(), "addserver");
    }

    public void deleteServer(final ServerInfo serverInfo) {
        adapter.items.remove(serverInfo);
        adapter.notifyDataSetChanged();
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
