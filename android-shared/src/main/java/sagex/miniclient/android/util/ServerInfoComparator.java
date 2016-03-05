package sagex.miniclient.android.util;

import java.util.Comparator;

import sagex.miniclient.ServerInfo;

/**
 * Created by seans on 01/03/16.
 */
public class ServerInfoComparator implements Comparator<ServerInfo> {
    public static final ServerInfoComparator INSTANCE = new ServerInfoComparator();

    @Override
    public int compare(ServerInfo o1, ServerInfo o2) {
        // sort by date accessed and then name
        int compare = 0;
        if (o1.lastConnectTime < o2.lastConnectTime) compare = 1;
        if (o1.lastConnectTime > o2.lastConnectTime) compare = -1;
        if (compare == 0) {
            return o1.name.compareTo(o2.name);
        }
        return compare;
    }
}
