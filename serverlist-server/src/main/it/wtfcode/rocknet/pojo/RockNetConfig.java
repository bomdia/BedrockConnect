package it.wtfcode.rocknet.pojo;

import java.util.HashMap;
import java.util.Map;

public class RockNetConfig {

	private String dbDriverClass = "it.wtfcode.rocknet.db.impl.RockNetSQLite";
	
	private Map<String,Object> dbDriverData = new HashMap<>();
	
	private String address = "0.0.0.0";
	
	private int port = 19132;
	
	private int maxConnectedPlayers = 20;
	
	private String title = "";
	
	private String subTitle = "";
	
	
	private boolean enablePreferred = true;
	
	private boolean enableDebug = false;
	
	private boolean showServersIcon = true;
	
	
	public RockNetConfig() {}

	public RockNetConfig(
			String dbDriverClass, Map<String,Object> dbDriverData,
			String address, int port, 
			int maxConnectedPlayers, boolean enablePreferred, 
			boolean enableDebug, boolean showServersIcon
	) {
		this.dbDriverClass = dbDriverClass;
		this.dbDriverData = dbDriverData;
		this.address = address;
		this.port = port;
		this.maxConnectedPlayers = maxConnectedPlayers;
		this.enablePreferred = enablePreferred;
		this.enableDebug = enableDebug;
		this.showServersIcon = showServersIcon;
	}

	public String getDbDriverClass() {
		return dbDriverClass;
	}

	public void setDbDriverClass(String dbDriverClass) {
		this.dbDriverClass = dbDriverClass;
	}

	public Map<String, Object> getDbDriverData() {
		return dbDriverData;
	}

	public void setDbDriverData(Map<String, Object> dbDriverData) {
		this.dbDriverData = dbDriverData;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getMaxConnectedPlayers() {
		return maxConnectedPlayers;
	}

	public void setMaxConnectedPlayers(int maxConnectedPlayers) {
		this.maxConnectedPlayers = maxConnectedPlayers;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public boolean isEnablePreferred() {
		return enablePreferred;
	}

	public void setEnablePreferred(boolean enablePreferred) {
		this.enablePreferred = enablePreferred;
	}

	public boolean isEnableDebug() {
		return enableDebug;
	}

	public void setEnableDebug(boolean enableDebug) {
		this.enableDebug = enableDebug;
	}

	public boolean isShowServersIcon() {
		return showServersIcon;
	}

	public void setShowServersIcon(boolean showServersIcon) {
		this.showServersIcon = showServersIcon;
	}


	
}
