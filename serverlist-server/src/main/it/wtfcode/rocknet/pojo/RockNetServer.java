package it.wtfcode.rocknet.pojo;

public class RockNetServer {

	private String serverAddress;
	private int serverPort;
	private String serverName;
	private String iconPath;
	private boolean preferred = false;
	
	
	public RockNetServer() {}

	public RockNetServer(String serverAddress, int serverPort, String serverName, String iconPath, boolean preferred) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.serverName = serverName;
		this.iconPath = iconPath;
		this.preferred = preferred;
	}

	
	
	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	public boolean isPreferred() {
		return preferred;
	}

	public void setPreferred(boolean preferred) {
		this.preferred = preferred;
	}
}
