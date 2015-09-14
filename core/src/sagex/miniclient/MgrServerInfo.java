package sagex.miniclient;

public class MgrServerInfo {
	public static final int LOCAL_SERVER = 1;
	public static final int DIRECT_CONNECT_SERVER = 2;
	public static final int LOCATABLE_SERVER = 3;

	public String serverName = "";
	public String serverDirectAddress = "";
	public String serverLookupID = "";
	public String lookupIP;
	public int port;
	public long lastConnectTime;
	public int serverType;
	public String authBlock = "";
	private SageLocatorService locatorServer;

	public MgrServerInfo(SageLocatorService locator) {
		this.locatorServer = locator;
	}

	public MgrServerInfo(String serverName) throws IllegalArgumentException // thrown
																			// if
																			// the
																			// properties
																			// values
																			// are
																			// bad
	{
		this.serverName = serverName;
		try {
			serverType = Integer.parseInt(MiniClient.myProperties.getProperty("servers/" + serverName + "/type", ""));
			serverDirectAddress = MiniClient.myProperties.getProperty("servers/" + serverName + "/address", "");
			serverLookupID = MiniClient.myProperties.getProperty("servers/" + serverName + "/locator_id", "");
			lastConnectTime = Long
					.parseLong(MiniClient.myProperties.getProperty("servers/" + serverName + "/last_connect_time", "0"));
			authBlock = MiniClient.myProperties.getProperty("servers/" + serverName + "/auth_block", "");
		} catch (Exception e) {
			throw new IllegalArgumentException("Bad server info properties");
		}
		doIPLookupNow();
	}

	public MgrServerInfo(String name, int port, String locatorId) {
		serverDirectAddress = serverName = name;
		serverType = LOCAL_SERVER;
		serverLookupID = locatorId;
		this.port = port;
		try {
			lastConnectTime = Long.parseLong(MiniClient.myProperties
					.getProperty("servers/local/" + serverName + "/last_connect_time", Long.toString(System.currentTimeMillis())));
		} catch (NumberFormatException e) {
		}
	}

	public String doIPLookupNow() {
		if (serverType == LOCATABLE_SERVER) {
			// Attempt a lookup right now
			try {
				lookupIP = locatorServer.lookupIPForGuid(serverLookupID);
			} catch (Exception e1) {
			}
		}
		return lookupIP;
	}

	public void setAuthBlock(String newAuth) {
		authBlock = newAuth;
		MiniClient.myProperties.setProperty("servers/" + serverName + "/auth_block", newAuth);
		MiniClient.saveConfig();
	}

	public String toString() {
		return serverName;
	}
}
