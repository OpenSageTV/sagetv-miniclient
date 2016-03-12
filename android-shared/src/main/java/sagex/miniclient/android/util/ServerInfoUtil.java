package sagex.miniclient.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.AppUtil;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.R;
import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.util.Utils;

/**
 * Created by seans on 05/03/16.
 */
public class ServerInfoUtil {
    static Logger log = LoggerFactory.getLogger(ServerInfoUtil.class);

    public static PopupMenu createContextMenu(Context context, View item, OnMenuItemClickListener clickListener) {
        PopupMenu menu = new PopupMenu(context, item);
        Menu m = menu.getMenu();
        menu.inflate(R.menu.server_actions);
        menu.setOnMenuItemClickListener(clickListener);
        return menu;
    }

    public static boolean onMenuItemClick(Context context, MenuItem item, ServerInfo serverInfo, OnAfterCommands after) {
        if (item.getItemId() == R.id.menu_remove) {
            deleteServer(context, serverInfo, true, after);
        } else if (item.getItemId() == R.id.menu_change_name) {
            onChangeName(context, serverInfo, after);
        } else if (item.getItemId() == R.id.menu_connect) {
            connect(context, serverInfo);
        } else if (item.getItemId() == R.id.menu_connect_locator) {
            if (!Utils.isEmpty(serverInfo.locatorID)) {
                ServerInfo newSI = serverInfo.clone();
                newSI.forceLocator = true;
                connect(context, newSI);
            } else {
                Toast.makeText(context, context.getString(R.string.msg_no_locator), Toast.LENGTH_LONG).show();
            }
        }
        return true;
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
                //i.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // http://stackoverflow.com/questions/3473168/clear-the-entire-history-stack-and-start-a-new-activity-on-android
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }

            ctx.startActivity(i);

            if (MiniclientApplication.get().getClient().properties().getBoolean(PrefStore.Keys.exit_to_home_screen, true)) {
                if (ctx instanceof Activity) {
                    ((Activity) ctx).finish();
                }
            }
        } catch (Throwable t) {
            log.error("Unabled to launch MiniClient Connection to Server {}", si, t);
            Toast.makeText(ctx, "Failed to connect to server: " + t, Toast.LENGTH_LONG).show();
        }

    }

    public interface OnAfterCommands {
        void onAfterDelete(ServerInfo serverInfo);

        void onAfterAdd(ServerInfo serverInfo);
    }

    public static void deleteServer(Context context, final ServerInfo serverInfo, boolean prompt, final OnAfterCommands afterDelete) {
        if (prompt) {
            AppUtil.confirmAction(context, context.getString(R.string.title_remove_server), context.getString(R.string.msg_remove_server), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MiniclientApplication.get().getClient().getServers().deleteServer(serverInfo.name);
                    afterDelete.onAfterDelete(serverInfo);
                }
            });
        } else {
            MiniclientApplication.get().getClient().getServers().deleteServer(serverInfo.name);
            afterDelete.onAfterDelete(serverInfo);
        }
    }

    public static void onChangeName(final Context context, final ServerInfo serverInfo, final OnAfterCommands after) {
        AppUtil.prompt(context, "Change Server Name", "Enter new Server Name", serverInfo.name, new AppUtil.OnValueChangeListener() {
            @Override
            public void onValueChanged(String oldValue, String newValue) {
                ServerInfo newSI = serverInfo.clone();
                newSI.name = newValue;

                // remove the old server
                ServerInfoUtil.deleteServer(context, serverInfo, false, after);

                // save the new server
                newSI.save(MiniclientApplication.get().getClient().properties());

                after.onAfterAdd(newSI);
            }
        });
    }

}
