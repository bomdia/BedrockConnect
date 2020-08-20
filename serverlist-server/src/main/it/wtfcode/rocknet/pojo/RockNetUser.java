package it.wtfcode.rocknet.pojo;

import java.util.List;

public class RockNetUser {

	private String xuid;
	private String userName;
	private List<RockNetServer> serverList;
	
	
	
	public RockNetUser() {}
	
	public RockNetUser(String xuid, String userName, List<RockNetServer> serverList) {
		this.xuid = xuid;
		this.userName = userName;
		this.serverList = serverList;
	}
	
	
	
	public String getXuid() {
		return xuid;
	}
	public void setXuid(String xuid) {
		this.xuid = xuid;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public List<RockNetServer> getServerList() {
		return serverList;
	}
	public void setServerList(List<RockNetServer> serverList) {
		this.serverList = serverList;
	}
}
