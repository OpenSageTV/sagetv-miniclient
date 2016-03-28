package sagex.miniclient;

import java.io.Serializable;

import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.util.Utils;

/**
 * Created by seans on 20/09/15.
 */
public class ServerInfo implements Serializable, Comparable<ServerInfo>, Cloneable {
    public static final int LOCAL_SERVER = 1;
    public static final int DIRECT_CONNECT_SERVER = 2;
    public static final int LOCATABLE_SERVER = 3;

    public String address;
    public int port = 31099;
    public String name;
    public String locatorID;
    public int serverType;
    public long lastConnectTime;
    public String authBlock;
    public boolean forceLocator = false;

    public ServerInfo() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServerInfo{");
        sb.append("address='").append(address).append('\'');
        sb.append(", port=").append(port);
        sb.append(", name='").append(name).append('\'');
        sb.append(", locatorID='").append(locatorID).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerInfo that = (ServerInfo) o;

        // 2 servers with same locator is same server
        if (locatorID != null) {
            return locatorID.equals(that.locatorID);
        }

        // 2 servers with same host and port
        if (port != that.port) return false;
        if (address != null) {
            return address.equals(that.address);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (locatorID != null ? locatorID.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(ServerInfo o) {
        if (locatorID != null) {
            return locatorID.compareTo(o.locatorID);
        }

        if (address == null && o.address == null) return 0;
        if (o.address == null) return -1;
        if (address == null) return 1;
        int compare = address.compareTo(o.address);
        if (compare == 0) {
            if (port < o.port) return -1;
            if (port > o.port) return 1;
        }
        return compare;
    }

    public void setAuthBlock(String authBlock) {
        this.authBlock = authBlock;
    }

    public void save(PrefStore store) {
        if (name == null) {
            System.out.println("Can't save ServerInfo without a name: " + this);
        }
        store.setLong("servers/" + name + "/type", serverType);
        if (Utils.isGUID(address)) {
            locatorID = address;
            address = null;
        }
        if (address != null) {
            store.setString("servers/" + name + "/address", address);
            store.setInt("servers/" + name + "/port", port);
        }
        if (locatorID != null)
            store.setString("servers/" + name + "/locator_id", locatorID);
        store.setLong("servers/" + name + "/last_connect_time", lastConnectTime);
        if (authBlock != null)
            store.setString("servers/" + name + "/auth_block", authBlock);
    }

    public void load(String name, PrefStore store) {
        this.name = name;
        serverType = (int) store.getLong("servers/" + name + "/type", 0);
        address = store.getString("servers/" + name + "/address", "");
        locatorID = store.getString("servers/" + name + "/locator_id", "");
        lastConnectTime = store.getLong("servers/" + name + "/last_connect_time", 0);
        authBlock = store.getString("servers/" + name + "/auth_block", "");
        port = store.getInt("servers/" + name + "/port", 31099);
    }

    public boolean isLocatorOnly() {
        return (!Utils.isEmpty(locatorID) && Utils.isEmpty(address))
                || (Utils.isEmpty(locatorID) && !Utils.isEmpty(address) && Utils.isGUID(address));

    }

    @Override
    public ServerInfo clone() {
        try {
            return (ServerInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
