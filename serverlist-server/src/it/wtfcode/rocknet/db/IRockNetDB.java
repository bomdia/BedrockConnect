package it.wtfcode.rocknet.db;

import java.util.List;

import it.wtfcode.rocknet.pojo.RockNetServer;
import it.wtfcode.rocknet.pojo.RockNetUser;

public interface IRockNetDB {

	//user part (key = xuid)
	public List<RockNetUser> getAllUsers();
	public RockNetUser getUser(String xuid);
	public RockNetUser createUser(String xuid, String userName);
	public void updateUser(RockNetUser user);
	public void removeUser(RockNetUser user);
	public void removeUser(String xuid);
	
	//server part (key = serverUrl)
	public List<RockNetServer> getAllServers();
	public RockNetServer getServer(String serverUrl);
	public RockNetServer createServer(String serverUrl, String name, List<RockNetUser> visibleTo);
	public void updateServer(RockNetServer server);
	public void removeServer(RockNetServer server);
	public void removeServer(String serverUrl);
}
