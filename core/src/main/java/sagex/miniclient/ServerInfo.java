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

    public String macAddress = null;
    public Boolean use_stateful_remote=null;

    public ServerInfo() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServerInfo{");
        sb.append("address='").append(address).append('\'');
        sb.append(", port=").append(port);
        sb.append(", name='").append(name).append('\'');
        sb.append(", locatorID='").append(locatorID).append('\'');
        sb.append(", macAddress='").append(macAddress).append('\'');
        sb.append(", use_stateful_remote='").append(use_stateful_remote).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerInfo that = (ServerInfo) o;

        // host:port and client makes a connection unique
        // this allows us to rename a connection that is discovered
        // and allows us to copy a connection to a new name, since clientid will change

        if (port != that.port) return false;
        if (!address.equals(that.address)) return false;

        // for mac address null and empty are the same
        if (macAddress == null || macAddress.trim().length() == 0) {
            if (that.macAddress == null || that.macAddress.trim().length() == 0) {
                return true;
            }
        }
        if (that.macAddress == null || that.macAddress.trim().length() == 0) {
            if (macAddress == null || macAddress.trim().length() == 0) {
                return true;
            }
        }
        if (macAddress != null) {
            macAddress.equals(that.macAddress);
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = address.hashCode();
        result = 31 * result + port;
        result = 31 * result + (macAddress != null ? macAddress.hashCode() : 0);
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
        if (macAddress!=null) {
            store.setString("servers/" + name + "/mac", macAddress);
        }
        if (use_stateful_remote!=null) {
            store.setBoolean("servers/" + name + "/use_stateful_remote", use_stateful_remote);
        }
    }

    public void load(String name, PrefStore store) {
        this.name = name;
        serverType = (int) store.getLong("servers/" + name + "/type", 0);
        address = store.getString("servers/" + name + "/address", "");
        locatorID = store.getString("servers/" + name + "/locator_id", "");
        lastConnectTime = store.getLong("servers/" + name + "/last_connect_time", 0);
        authBlock = store.getString("servers/" + name + "/auth_block", "");
        port = store.getInt("servers/" + name + "/port", 31099);
        macAddress = store.getString("servers/" + name + "/mac", "");
        if (store.contains("servers/" + name + "/use_stateful_remote")) {
            use_stateful_remote = store.getBoolean("servers/" + name + "/use_stateful_remote", true);
        }
    }

    public boolean isLocatorOnly() {
        return (!Utils.isEmpty(locatorID) && Utils.isEmpty(address))
                || (Utils.isEmpty(locatorID) && !Utils.isEmpty(address) && Utils.isGUID(address));
    }

    public static String getPrefKey(String name, String id) {
        return "servers/" + name + "/" + id;
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
