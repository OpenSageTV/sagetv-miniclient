/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package sagex.miniclient.android.tv;

import android.app.Activity;
import android.os.Bundle;

import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.AddServerFragment.OnAddServerListener;
import sagex.miniclient.android.AutoConnectDialog;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.util.AudioUtil;
import sagex.miniclient.prefs.PrefStore;

/*
 * MainActivity class that loads MainFragment
 */
public class MainActivity extends Activity implements OnAddServerListener {
    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    public void onAddServer(String name, String addr) {
        ((OnAddServerListener) getFragmentManager().findFragmentById(R.id.main_browse_fragment)).onAddServer(name, addr);
    }

    @Override
    protected void onResume() {
        AudioUtil.requestAudioFocus(this);
        super.onResume();
    }
}
