package sagex.miniclient;

import java.util.ArrayList;
import java.util.List;

/**
 * Server discovery enables the auto discovery of SageTV servers on the same network.  When a server's a discovered they are passed to the
 * ServerDiscoveryCallback callback.
 * 
 */
public class ServerDiscovery {

	public static class ServerInfo {
		public String address;
		public int port;
		public String name;
		public String locatorID;

		public String toString() {
			return name + " " + address;
		}
	}

	public interface ServerDiscoverCallback {
		public void serverDiscovered(ServerInfo si);
	}

	ServerDiscovery() {
	}

	public static ServerInfo[] discoverServers(int discoveryTimeout, ServerDiscoverCallback callback) {
		List<ServerInfo> servers = (callback == null) ? new ArrayList<ServerInfo>() : null;
		System.out.println("Sending out discovery packets to find SageTVPlaceshifter Servers...");
		java.net.DatagramSocket sock = null;
		try {
			// Try on the encoder discovery port which is less likely to be in
			// use
			try {
				sock = new java.net.DatagramSocket(8271);
			} catch (java.net.BindException be2) {
				// Just create it wherever
				sock = new java.net.DatagramSocket();
			}
			java.net.DatagramPacket pack = new java.net.DatagramPacket(new byte[512], 512);
			byte[] data = pack.getData();
			data[0] = 'S';
			data[1] = 'T';
			data[2] = 'V';
			data[3] = 1;
			pack.setLength(32);
			sock.setBroadcast(true);
			// Find the broadcast address for this subnet.
			// String myIP = SageTV.api("GetLocalIPAddress", new
			// Object[0]).toString();
			// int lastIdx = myIP.lastIndexOf('.');
			// myIP = myIP.substring(0, lastIdx) + ".255";
			pack.setAddress(java.net.InetAddress.getByName("255.255.255.255"));
			pack.setPort(31100);
			sock.send(pack);
			long startTime = System.currentTimeMillis();
			do {
				int currTimeout = (int) Math.max(1, (startTime + discoveryTimeout) - System.currentTimeMillis());
				sock.setSoTimeout(currTimeout);
				sock.receive(pack);
				if (pack.getLength() >= 4) {
					System.out.println("Discovery packet received:" + pack);
					ServerInfo si = new ServerInfo();
					if (data[0] == 'S' && data[1] == 'T' && data[2] == 'V' && data[3] == 2) {
						si.name = pack.getAddress().getHostName();
						si.address = pack.getAddress().getHostAddress();
						if (pack.getLength() >= 13) // it also has locator ID in
													// it
						{
							int locatorID1 = ((data[5] & 0xFF) << 24) | ((data[6] & 0xFF) << 16) | ((data[7] & 0xFF) << 8)
									| (data[8] & 0xFF);
							int locatorID2 = ((data[9] & 0xFF) << 24) | ((data[10] & 0xFF) << 16) | ((data[11] & 0xFF) << 8)
									| (data[12] & 0xFF);
							long locatorID = (((long) locatorID1) << 32) | locatorID2;
							String prettyGuid = "";
							for (int i = 0; i < 4; i++) {
								String subGuid = Long.toString((locatorID >> ((3 - i) * 16)) & 0xFFFF, 16);
								while (subGuid.length() < 4)
									subGuid = "0" + subGuid;
								if (i != 0)
									prettyGuid += "-";
								prettyGuid += subGuid;
							}
							si.locatorID = prettyGuid.toUpperCase();
						}
						if (pack.getLength() >= 15) // it also has the port in
													// it
						{
							si.port = ((data[13] & 0xFF) << 8) | (data[14] & 0xFF);
						}
						System.out.println("Added server info:" + si);
						if (callback != null)
							callback.serverDiscovered(si);
						else
							servers.add(si);
					}
				}
			} while (true);
		} catch (Exception e) {
			// System.out.println("Error discovering servers:" + e);
		} finally {
			if (sock != null) {
				try {
					sock.close();
				} catch (Exception e) {
				}
				sock = null;
			}
		}
		return (servers == null) ? null : (ServerInfo[]) servers.toArray(new ServerInfo[0]);
	}

}
