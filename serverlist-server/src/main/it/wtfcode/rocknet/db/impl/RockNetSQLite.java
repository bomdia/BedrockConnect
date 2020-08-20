package it.wtfcode.rocknet.db.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.wtfcode.rocknet.db.IRockNetDB;
import it.wtfcode.rocknet.pojo.RockNetServer;
import it.wtfcode.rocknet.pojo.RockNetUser;
import it.wtfcode.rocknet.utils.DBUtils;

public class RockNetSQLite implements IRockNetDB {

	private static final String POPULATE_FILE = "SQLite.populatedb.sql";
	private static final Logger log = LogManager.getLogger(RockNetSQLite.class.getName());
	protected Connection connection = null;
	
	private static final String CHECK_QUERY = 
			"SELECT count(name) as \"check\" FROM sqlite_master WHERE type in ('table','view') " + 
			"and name in ('servers','globalServers','users','usersServers','usersWithServers')";
	private static final int CHECK_QUERY_RESULT = 5;
	

	@Override
	public void initialize(Map<String, Object> dbVariable) {
		if(dbVariable == null) dbVariable = new HashMap<>();
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			log.error("SQLite JDBC driver not found (very strange, pls report to RockNet developer!)");
		}
		String filePath = (String) dbVariable.getOrDefault("filePath","RockNet.db");
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:"+filePath);
		} catch (SQLException e) {
			log.error(e);
		}		
		DBUtils.doInStatement(connection, statement -> {
			if(DBUtils.isDbEmpty(statement,CHECK_QUERY,CHECK_QUERY_RESULT))			
				DBUtils.executeSQLResource(statement, POPULATE_FILE);
			return null;
		});

	}

	@Override
	public void deinitialize() {
		if(connection != null)
			try	{
				connection.close();
			} catch(SQLException e){
				log.error(e);
			}
	}

	@Override
	public List<RockNetUser> getAllUsers() {
		return DBUtils.doInStatement(connection, statement -> {
			String query = "select * from users";
			return DBUtils.doInResultSet(statement, query, rs -> {
				ArrayList<RockNetUser> usersList = new ArrayList<>();
				while(rs.next()) {
					String subQuery = "select " + 
							"a.serverAddress, " + 
							"a.serverPort, " + 
							"b.serverName, " + 
							"a.iconPath " + 
							"from servers a " + 
							"inner join usersServers b on " + 
							"a.serverAddress = b.serverAddress and " + 
							"a.serverPort = b.serverPort where b.xuid = " + rs.getLong("xuid") + 
							" UNION ALL " + 
							"select serverAddress, serverPort, serverName, iconPath from globalServers";
	
					List<RockNetServer> serverList = DBUtils.doInResultSet(statement, subQuery, rs2 -> {
						ArrayList<RockNetServer> curServerList = new ArrayList<>();
						while(rs2.next()) {
							curServerList.add(new RockNetServer(rs2.getString("serverAddress"), rs2.getInt("serverPort"), rs2.getString("serverName"), rs2.getString("iconPath")));
						}
						return curServerList;
					});
					usersList.add(new RockNetUser(rs.getString("xuid"), rs.getString("userName"), serverList));
				}
				return usersList;
			});
		});
	}

	@Override
	public RockNetUser getUser(String xuid) {
		if(StringUtils.isNotBlank(xuid))
			return DBUtils.doInStatement(connection, statement -> {
				String query = "select * from users where xuid = " + xuid;
				return DBUtils.doInResultSet(statement, query, rs -> {
					if(rs.next()) {
						String subQuery = "select " + 
								"a.serverAddress, " + 
								"a.serverPort, " + 
								"b.serverName, " + 
								"a.iconPath " + 
								"from servers a " + 
								"inner join usersServers b on " + 
								"a.serverAddress = b.serverAddress and " + 
								"a.serverPort = b.serverPort where b.xuid = " + xuid + 
								" UNION ALL " + 
								"select serverAddress, serverPort, serverName, iconPath from globalServers";
		
						List<RockNetServer> serverList = DBUtils.doInResultSet(statement, subQuery, rs2 -> {
							ArrayList<RockNetServer> curServerList = new ArrayList<>();
							while(rs2.next()) 
								curServerList.add(new RockNetServer(rs2.getString("serverAddress"), rs2.getInt("serverPort"), rs2.getString("serverName"), rs2.getString("iconPath")));
							return curServerList;
						});
						
						return new RockNetUser(rs.getString("xuid"), rs.getString("userName"), serverList);
					} else return null;
				});
			});
		else return null;
	}

	@Override
	public RockNetUser createUser(String xuid, String userName) {
		if(StringUtils.isNotBlank(xuid) && userName != null)
			return DBUtils.doInStatement(connection, statement -> {
				statement.executeUpdate("insert into users values ("+xuid+",'"+userName+"')");
				String subQuery = "select serverAddress, serverPort, serverName, iconPath from globalServer";
				List<RockNetServer> serverList = DBUtils.doInResultSet(statement, subQuery, rs2 -> {
					ArrayList<RockNetServer> curServerList = new ArrayList<>();
					while(rs2.next()) 
						curServerList.add(new RockNetServer(rs2.getString("serverAddress"), rs2.getInt("serverPort"), rs2.getString("serverName"), rs2.getString("iconPath")));
					return curServerList;
				});
				return new RockNetUser(xuid, userName, serverList);
			});
		else return null;
	}

	@Override
	public void updateUserName(RockNetUser user) {
		if(user != null && StringUtils.isNotBlank(user.getXuid()) && user.getUserName() != null)
			updateUserName(user.getXuid(),user.getUserName());
	}

	@Override
	public void updateUserName(String xuid, String userName) {
		if(StringUtils.isNotBlank(xuid) && userName != null)
			DBUtils.doInStatement(connection, statement -> {
				statement.executeUpdate("update users set userName = '"+userName+"' where xuid = "+xuid);
				return null;
			});
	}

	@Override
	public void removeUser(RockNetUser user) {
		if(user != null && StringUtils.isNotBlank(user.getXuid()))
			removeUser(user.getXuid());
	}

	@Override
	public void removeUser(String xuid) {
		if(StringUtils.isNotBlank(xuid))
			DBUtils.doInStatement(connection, statement -> {
				statement.executeUpdate("delete from usersServers where xuid = "+xuid);
				statement.executeUpdate("delete from users where xuid = "+xuid);
				return null;
			});
	}

	@Override
	public void attachUserToServer(String xuid, String serverAddress, int serverPort, String serverName) {
		if(StringUtils.isNotBlank(xuid) && StringUtils.isNotBlank(serverAddress) && serverPort > 0 && serverPort <= 65535 && serverName != null)
			DBUtils.doInStatement(connection, statement -> {
				statement.executeUpdate("insert into usersServers values ("+xuid+",'"+serverAddress+"',"+serverPort+",'"+serverName+"')");
				return null;
			});		
	}

	@Override
	public void detachUserFromServer(String xuid, String serverAddress, int serverPort) {
		if(StringUtils.isNotBlank(xuid) && StringUtils.isNotBlank(serverAddress) && serverPort > 0 && serverPort <= 65535)
			DBUtils.doInStatement(connection, statement -> {
				statement.executeUpdate("delete from usersServers where xuid = "+xuid+ " and serverAddress = '"+serverAddress+"' and serverPort = "+serverPort);
				return null;
			});	
	}

	@Override
	public List<RockNetServer> getAllServers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RockNetServer> getAllGlobalServers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RockNetServer getServer(String serverAddress, int serverPort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RockNetServer getGlobalServer(String serverAddress, int serverPort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RockNetServer createServer(String serverAddress, int serverPort, String iconPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RockNetServer createGlobalServer(String serverAddress, int serverPort, String serverName, String iconPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateServer(String xuid,RockNetServer server) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateGlobalServer(RockNetServer server) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeServer(RockNetServer server) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeServer(String serverAddress, int serverPort) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeGlobalServer(RockNetServer server) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeGlobalServer(String serverAddress, int serverPort) {
		// TODO Auto-generated method stub
		
	}

}
