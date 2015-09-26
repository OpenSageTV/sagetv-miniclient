package sagex.miniclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility methods for dealing with Server Connection Data
 */
public class Servers {
    public static void saveServer(ServerInfo si) {
        MiniClient.myProperties.setProperty("servers/" + si.name + "/type", String.valueOf(si.serverType));
        if (si.address!=null) MiniClient.myProperties.setProperty("servers/" + si.name + "/address", si.address);
        if (si.locatorID!=null) MiniClient.myProperties.setProperty("servers/" + si.name + "/locator_id", si.locatorID);
        MiniClient.myProperties.setProperty("servers/" + si.name + "/last_connect_time", String.valueOf(si.lastConnectTime));
        if (si.authBlock!=null) MiniClient.myProperties.setProperty("servers/" + si.name + "/auth_block", si.authBlock);
        MiniClient.saveConfig();
    }

    public static void deleteServer(String serverName) {
        String parts[]=null;
        ArrayList list = new ArrayList(MiniClient.myProperties.keySet());
        Collections.sort(list);
        for (Object k: list) {
            if (k.toString().startsWith("servers/")) {
                // process server
                parts=k.toString().split("/");
                if (parts.length>1 && parts[1].equals(serverName)) {
                    MiniClient.myProperties.remove(k);
                }
            }
        }
    }

    public static ServerInfo getServer(String serverName) {
        ServerInfo si = new ServerInfo();
        si.serverType = Integer.parseInt(MiniClient.myProperties.getProperty("servers/" + serverName + "/type", ""));
        si.address = MiniClient.myProperties.getProperty("servers/" + serverName + "/address", "");
        si.locatorID = MiniClient.myProperties.getProperty("servers/" + serverName + "/locator_id", "");
        si.lastConnectTime = Long
                .parseLong(MiniClient.myProperties.getProperty("servers/" + serverName + "/last_connect_time", "0"));
        si.authBlock = MiniClient.myProperties.getProperty("servers/" + serverName + "/auth_block", "");
        si.name=serverName;

        // not found
        if ( (si.address==null||si.address.trim().length()==0) && (si.locatorID==null || si.locatorID.trim().length()==0)) {
            return null;
        }
        return si;
    }

    public static List<ServerInfo> getSavedServers() {
        List<ServerInfo> servers = new ArrayList<ServerInfo>();
        String lastServer=null;
        String thisServer=null;
        String parts[]=null;
        ArrayList list = new ArrayList(MiniClient.myProperties.keySet());
        Collections.sort(list);
        for (Object k: list) {
            if (k.toString().startsWith("servers/")) {
                // process server
                parts=k.toString().split("/");
                if (parts.length>1) {
                    thisServer = parts[1];
                    if (thisServer.equals(lastServer)) continue;
                    lastServer=thisServer;

                    // add Server
                    servers.add(getServer(parts[1]));
                }
            }
        }
        return servers;
    }
}
