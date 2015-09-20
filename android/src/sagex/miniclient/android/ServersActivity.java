package sagex.miniclient.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import sagex.miniclient.MiniClient;
import sagex.miniclient.ServerDiscovery;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.canvas.MiniClientActivity;
import sagex.miniclient.ui.ServerWidget;

import static sagex.miniclient.MiniClientMain.requestRendering;

/**
 * Created by seans on 20/09/15.
 */
public class ServersActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = "SERVERS";
    ListView list = null;
    ServersAdapter adapter = null;
    boolean paused = true;
    public ServersActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.servers_layout);

        adapter = new ServersAdapter(this);

        list = (ListView) findViewById(R.id.list);
        list.setOnItemClickListener(this);
        list.setFocusable(true);
        list.requestFocus();
        list.setAdapter(adapter);

        paused=false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused=false;
        refreshServers();
    }

    @Override
    protected void onPause() {
        paused=true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        paused=true;
        super.onDestroy();
    }

    public void refreshServers() {
        Log.d(TAG, "Looking for Servers...");
        ServerDiscovery.discoverServersAsync(10000, new ServerDiscovery.ServerDiscoverCallback() {
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ServerInfo si = adapter.getCastedItem(position);
        Intent i = new Intent(getBaseContext(), MiniClientActivity.class);
        i.putExtra(MiniClientActivity.ARG_SERVER_INFO, si);
        startActivity(i);
    }
}
