package it.wtfcode.rocknet.db.impl;

import java.util.List;

import it.wtfcode.rocknet.db.IRockNetDB;
import it.wtfcode.rocknet.pojo.RockNetServer;
import it.wtfcode.rocknet.pojo.RockNetUser;

public class RockNetMySQL implements IRockNetDB{

	@Override
	public List<RockNetUser> getAllUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RockNetUser getUser(String xuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RockNetUser createUser(String xuid, String userName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateUser(RockNetUser user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeUser(RockNetUser user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeUser(String xuid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<RockNetServer> getAllServers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RockNetServer getServer(String serverUrl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RockNetServer createServer(String serverUrl, String name, List<RockNetUser> visibleTo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateServer(RockNetServer server) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeServer(RockNetServer server) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeServer(String serverUrl) {
		// TODO Auto-generated method stub
		
	}

}
