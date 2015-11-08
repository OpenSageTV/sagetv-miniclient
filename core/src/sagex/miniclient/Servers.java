package sagex.miniclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility methods for dealing with Server Connection Data
 */
public class Servers {
    MiniClient client;

    public Servers(MiniClient client) {
        this.client = client;
    }

    public void saveServer(ServerInfo si) {
        client.properties().setLong("servers/" + si.name + "/type", si.serverType);
        if (si.address != null)
            client.properties().setString("servers/" + si.name + "/address", si.address);
        if (si.locatorID != null)
            client.properties().setString("servers/" + si.name + "/locator_id", si.locatorID);
        client.properties().setLong("servers/" + si.name + "/last_connect_time", si.lastConnectTime);
        if (si.authBlock != null)
            client.properties().setString("servers/" + si.name + "/auth_block", si.authBlock);
        //client.saveConfig();
    }

    public void deleteServer(String serverName) {
        String parts[] = null;
        ArrayList list = new ArrayList(client.properties().keys());
        Collections.sort(list);
        for (Object k : list) {
            if (k.toString().startsWith("servers/")) {
                // process server
                parts = k.toString().split("/");
                if (parts.length > 1 && parts[1].equals(serverName)) {
                    client.properties().remove((String) k);
                }
            }
        }
    }

    public ServerInfo getServer(String serverName) {
        ServerInfo si = new ServerInfo();
        si.serverType = (int) client.properties().getLong("servers/" + serverName + "/type", 0);
        si.address = client.properties().getString("servers/" + serverName + "/address", "");
        si.locatorID = client.properties().getString("servers/" + serverName + "/locator_id", "");
        si.lastConnectTime = client.properties().getLong("servers/" + serverName + "/last_connect_time", 0);
        si.authBlock = client.properties().getString("servers/" + serverName + "/auth_block", "");
        si.name = serverName;

        // not found
        if ((si.address == null || si.address.trim().length() == 0) && (si.locatorID == null || si.locatorID.trim().length() == 0)) {
            return null;
        }
        return si;
    }

    public List<ServerInfo> getSavedServers() {
        List<ServerInfo> servers = new ArrayList<ServerInfo>();
        String lastServer = null;
        String thisServer = null;
        String parts[] = null;
        ArrayList list = new ArrayList(client.properties().keys());
        Collections.sort(list);
        for (Object k : list) {
            if (k.toString().startsWith("servers/")) {
                // process server
                parts = k.toString().split("/");
                if (parts.length > 1) {
                    thisServer = parts[1];
                    if (thisServer.equals(lastServer)) continue;
                    lastServer = thisServer;

                    // add Server
                    servers.add(getServer(parts[1]));
                }
            }
        }
        return servers;
    }
}
