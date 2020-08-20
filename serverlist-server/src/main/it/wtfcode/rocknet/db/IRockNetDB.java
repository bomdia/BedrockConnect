package it.wtfcode.rocknet.db;

import java.util.List;
import java.util.Map;

import it.wtfcode.rocknet.pojo.RockNetServer;
import it.wtfcode.rocknet.pojo.RockNetUser;

public interface IRockNetDB {

	public void initialize(Map<String, Object> map);
	public void deinitialize();
	
	//user part (key = xuid)
	public List<RockNetUser> getAllUsers();
	public RockNetUser getUser(String xuid);
	public RockNetUser createUser(String xuid, String userName);
	public void updateUserName(RockNetUser user);
	public void updateUserName(String xuid,String userName);
	public void removeUser(RockNetUser user);
	public void removeUser(String xuid);
	
	public void attachUserToServer(String xuid, String serverAddress, int serverPort,String serverName);
	public void detachUserFromServer(String xuid, String serverAddress, int serverPort);
	
	//server part (key = serverUrl)
	public List<RockNetServer> getAllServers();
	public List<RockNetServer> getAllGlobalServers();
	public RockNetServer getServer(String serverAddress, int serverPort);
	public RockNetServer getGlobalServer(String serverAddress, int serverPort);
	public RockNetServer createServer(String serverAddress, int serverPort, String iconPath);
	public RockNetServer createGlobalServer(String serverAddress, int serverPort,String serverName, String iconPath);
	public void updateServer(String xuid,RockNetServer server);
	public void updateGlobalServer(RockNetServer server);
	public void removeServer(RockNetServer server);
	public void removeServer(String serverAddress, int serverPort);
	public void removeGlobalServer(RockNetServer server);
	public void removeGlobalServer(String serverAddress, int serverPort);

}
