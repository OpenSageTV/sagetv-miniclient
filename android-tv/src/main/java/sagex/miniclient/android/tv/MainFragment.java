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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import sagex.miniclient.ServerDiscovery;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.AddServerFragment;
import sagex.miniclient.android.AddServerFragment.OnAddServerListener;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.SettingsActivity;
import sagex.miniclient.android.tv.actions.Action;
import sagex.miniclient.android.tv.actions.ActionPresenter;
import sagex.miniclient.android.util.ServerInfoUtil;
import sagex.miniclient.android.util.ServerInfoUtil.OnAfterCommands;

public class MainFragment extends BrowseFragment implements OnAddServerListener {
    Logger log = LoggerFactory.getLogger(MainFragment.class);

    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;

    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private Object mBackgroundURI;
    private Object mDefaultBackgroundNoBackground = R.drawable.back_dark_knight;
    private BackgroundManager mBackgroundManager;

    ServersAdapter serversAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        setupUIElements();

        loadRows();

        setupEventListeners();

        refreshServers();
    }

    public void refreshServers() {
        // refresh the data in case last connected changed, etc
        serversAdapter.notifyArrayItemRangeChanged(0, serversAdapter.size());

        log.debug("Looking for Servers...");
        MiniclientApplication.get(getActivity()).getClient().getServerDiscovery().discoverServersAsync(10000, new ServerDiscovery.ServerDiscoverCallback() {
            @Override
            public void serverDiscovered(final ServerInfo si) {
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log.debug("Adding Server {}", si);
                            serversAdapter.addServer(si);
                        }
                    });

                } catch (Throwable t) {

                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
        MiniclientApplication.get().getClient().getServerDiscovery().close();
    }

    private void loadRows() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        ServerItemPresenter serverPresenter = new ServerItemPresenter(getActivity(), new OnAfterCommands() {
            @Override
            public void onAfterDelete(ServerInfo serverInfo) {
                log.info("loadRows: AfterDelete: {}", serverInfo);
                serversAdapter.remove(serverInfo);
            }

            @Override
            public void onAfterAdd(ServerInfo serverInfo) {
                log.info("loadRows: AfterAdd: {}", serverInfo);
                serversAdapter.addServer(serverInfo);
            }
        });

        int i = 0;

        // add in Servers
        HeaderItem serversHeader = new HeaderItem(i++, getString(R.string.servers));
        serversAdapter = new ServersAdapter(serverPresenter);
        addAll(MiniclientApplication.get(getActivity().getApplicationContext()).getClient().getServers().getSavedServers());
        mRowsAdapter.add(new ListRow(serversHeader, serversAdapter));

        HeaderItem actionsHeader = new HeaderItem(i, getString(R.string.configure));
        ActionPresenter actionPresenter = new ActionPresenter(getActivity());
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(actionPresenter);
        Action action = new Action(R.id.preferences, getString(R.string.preferences));
        action.setBackground(R.drawable.back_tools);
        gridRowAdapter.add(action);
        action = new Action(R.id.btn_add_server, getString(R.string.add_server));
        action.setBackground(R.drawable.back_film_roll);
        gridRowAdapter.add(action);
        mRowsAdapter.add(new ListRow(actionsHeader, gridRowAdapter));

        setAdapter(mRowsAdapter);

    }

    public void addAll(Collection<ServerInfo> newItems) {
        for (ServerInfo item : newItems) {
            serversAdapter.add(item);
            log.debug("ADDED SERVER: {}", item);
        }
    }

    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setBadgeDrawable(getActivity().getResources().getDrawable(
                R.drawable.sage_logo_256));

        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background));

        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                        .show();
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private Object lastImage = null;

    protected void updateBackground(Object image) {
        if (lastImage != null && lastImage.equals(image)) return; // same image, do nothing
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        if (image instanceof Integer) {
            Glide.with(getActivity())
                    .load((Integer) image)
                    .centerCrop()
                    .error(mDefaultBackground)
                    .into(new SimpleTarget<GlideDrawable>(width, height) {
                        @Override
                        public void onResourceReady(GlideDrawable resource,
                                                    GlideAnimation<? super GlideDrawable>
                                                            glideAnimation) {
                            mBackgroundManager.setDrawable(resource);
                        }
                    });
        }
        mBackgroundTimer.cancel();
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    @Override
    public void onAddServer(String name, String addr) {
        if (addr != null && addr.trim().length() > 0) {
            ServerInfo si = new ServerInfo();
            si.name = name;
            si.address = addr;
            MiniclientApplication.get().getClient().getServers().saveServer(si);
            serversAdapter.add(si);
            Toast.makeText(getActivity(), "Server Added", Toast.LENGTH_LONG).show();
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof ServerInfo) {
                ServerInfoUtil.connect(getActivity(), (ServerInfo) item);
            } else if (item instanceof Action) {
                Action action = (Action) item;
                if (action.getActionId() == R.id.preferences) {
                    Intent i = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(i);
                } else if (action.getActionId() == R.id.btn_add_server) {
                    // add new server
                    AddServerFragment f = AddServerFragment.newInstance("My Server", "");
                    f.setRetainInstance(true);
                    f.show(getFragmentManager(), "addserver");
                } else {
                    Toast.makeText(getActivity(), item.toString(), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Action) {
                mBackgroundURI = ((Action) item).getBackground();
                if (mBackgroundURI == null) mBackgroundURI = mDefaultBackgroundNoBackground;
                startBackgroundTimer();
            } else if (item instanceof ServerInfo) {
                if (((ServerInfo) item).isLocatorOnly()) {
                    mBackgroundURI = R.drawable.back_film_wall;
                    startBackgroundTimer();
                } else {
                    mBackgroundURI = R.drawable.back_dark_knight;
                    startBackgroundTimer();
                }
            } else {
                mBackgroundURI = mDefaultBackgroundNoBackground;
                startBackgroundTimer();
            }

        }
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBackgroundURI != null) {
                        updateBackground(mBackgroundURI);
                    }
                }
            });

        }
    }
}
