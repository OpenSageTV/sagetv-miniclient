package sagex.miniclient;

import java.io.Serializable;

import sagex.miniclient.prefs.PrefStore;

/**
 * Created by seans on 20/09/15.
 */
public class ServerInfo implements Serializable, Comparable<ServerInfo> {
    public static final int LOCAL_SERVER = 1;
    public static final int DIRECT_CONNECT_SERVER = 2;
    public static final int LOCATABLE_SERVER = 3;

    public String address;
    public int port;
    public String name;
    public String locatorID;
    public int serverType;
    public long lastConnectTime;
    public String authBlock;

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

        if (port != that.port) return false;
        return address.equals(that.address);

    }

    @Override
    public int hashCode() {
        int result = address.hashCode();
        result = 31 * result + port;
        return result;
    }

    @Override
    public int compareTo(ServerInfo o) {
        if (address == null && o.address == null) return 0;
        if (o.address == null && address != null) return -1;
        return address.compareTo(o.address);
    }

    public void setAuthBlock(String authBlock) {
        this.authBlock = authBlock;
    }

    public void save(PrefStore store) {
        if (name == null) {
            System.out.println("Can't save ServerInfo without a name: " + this);
        }
        store.setLong("servers/" + name + "/type", serverType);
        if (address != null)
            store.setString("servers/" + name + "/address", address);
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
    }

}
