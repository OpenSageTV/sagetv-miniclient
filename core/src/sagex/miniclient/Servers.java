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
        si.save(client.properties());
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
        si.load(serverName, client.properties());

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
