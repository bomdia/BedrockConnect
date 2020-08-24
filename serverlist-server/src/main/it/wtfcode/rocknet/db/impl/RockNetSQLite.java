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

	private static final String POPULATE_FILE = "populateSqlDb.sql";
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
					String xuid = rs.getString("xuid");
					String userName = rs.getString("userName");
					String subQuery = "select * from (select " + 
							"a.serverAddress, " + 
							"a.serverPort, " + 
							"b.serverName, " + 
							"b.preferred, " +
							"a.iconPath " + 
							"from servers a " + 
							"inner join usersServers b on " + 
							"a.serverAddress = b.serverAddress and " + 
							"a.serverPort = b.serverPort where b.xuid = " + xuid + " " +
							"ORDER BY b.preferred, b.serverName ) c" +
							" UNION " + 
							"select serverAddress, serverPort, serverName, 0 as preferred, iconPath from globalServers";
	
					List<RockNetServer> serverList = DBUtils.doInResultSet(statement, subQuery, rs2 -> {
						ArrayList<RockNetServer> curServerList = new ArrayList<>();
						while(rs2.next()) {
							boolean preferred = false;
							if(rs.getInt("preferred") > 0) preferred = true;
							
							curServerList.add(new RockNetServer(rs2.getString("serverAddress"), rs2.getInt("serverPort"), rs2.getString("serverName"), rs2.getString("iconPath"), preferred));
						}
						return curServerList;
					});
					usersList.add(new RockNetUser(xuid, userName, serverList));
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
						String userName = rs.getString("userName");
						String subQuery = "select * from (select " + 
								"a.serverAddress, " + 
								"a.serverPort, " + 
								"b.serverName, " + 
								"b.preferred, " +
								"a.iconPath " + 
								"from servers a " + 
								"inner join usersServers b on " + 
								"a.serverAddress = b.serverAddress and " + 
								"a.serverPort = b.serverPort where b.xuid = " + xuid + " " +
								"ORDER BY b.preferred, b.serverName ) c" +
								" UNION " + 
								"select serverAddress, serverPort, serverName, 0 as preferred, iconPath from globalServers";
		
						List<RockNetServer> serverList = DBUtils.doInResultSet(statement, subQuery, rs2 -> {
							ArrayList<RockNetServer> curServerList = new ArrayList<>();
							while(rs2.next()) {
								boolean preferred = false;
								if(rs.getInt("preferred") > 0) preferred = true;
								curServerList.add(new RockNetServer(rs2.getString("serverAddress"), rs2.getInt("serverPort"), rs2.getString("serverName"), rs2.getString("iconPath"),preferred));
							}
							return curServerList;
						});
						
						return new RockNetUser(xuid, userName, serverList);
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
				String subQuery = "select serverAddress, serverPort, serverName, iconPath from globalServers";
				List<RockNetServer> serverList = DBUtils.doInResultSet(statement, subQuery, rs2 -> {
					ArrayList<RockNetServer> curServerList = new ArrayList<>();
					while(rs2.next()) 
						curServerList.add(new RockNetServer(rs2.getString("serverAddress"), rs2.getInt("serverPort"), rs2.getString("serverName"), rs2.getString("iconPath"),false));
					return curServerList;
				});
				return new RockNetUser(xuid, userName, serverList);
			});
		else return null;
	}

	@Override
	public void updateUserName(RockNetUser user) {
		if(user != null)
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
		if(user != null)
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
	public void attachUserToServer(String xuid, String serverAddress, int serverPort, String serverName, boolean preferred) {
		if(StringUtils.isNotBlank(xuid) && StringUtils.isNotBlank(serverAddress) && serverPort > 0 && serverPort <= 65535 && serverName != null)
			DBUtils.doInStatement(connection, statement -> {
				int pref = 0;
				if(preferred) pref = 1;
				statement.executeUpdate("insert into usersServers values ("+xuid+",'"+serverAddress+"',"+serverPort+",'"+serverName+"', "+pref+")");
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
		return DBUtils.doInStatement(connection, statement -> {
			String query = 	"select serverAddress, serverPort, '' as serverName, iconPath from servers";			
			return DBUtils.doInResultSet(statement, query, rs -> {
				
				ArrayList<RockNetServer> serverList = new ArrayList<>();
				while(rs.next()) {
					serverList.add(new RockNetServer(rs.getString("serverAddress"), rs.getInt("serverPort"), rs.getString("serverName"), rs.getString("iconPath"), false));
				}
				return serverList;
			});
		});
	}

	@Override
	public List<RockNetServer> getUserServers(String xuid) {
		return DBUtils.doInStatement(connection, statement -> {
			String query = "select " + 
					"a.serverAddress, " + 
					"a.serverPort, " + 
					"b.serverName, " + 
					"b.preferred, " +
					"a.iconPath " + 
					"from servers a " + 
					"inner join usersServers b on " + 
					"a.serverAddress = b.serverAddress and " + 
					"a.serverPort = b.serverPort where b.xuid = " + xuid + " " +
					"ORDER BY b.preferred, b.serverName ";
			return DBUtils.doInResultSet(statement, query, rs -> {
				
				ArrayList<RockNetServer> serverList = new ArrayList<>();
				while(rs.next()) {
					boolean preferred = false;
					if(rs.getInt("preferred") > 0) preferred = true;
					
					serverList.add(new RockNetServer(rs.getString("serverAddress"), rs.getInt("serverPort"), rs.getString("serverName"), rs.getString("iconPath"), preferred));
				}
				return serverList;
			});
		});
	}

	@Override
	public List<RockNetServer> getAllGlobalServers() {
		return DBUtils.doInStatement(connection, statement -> {
			String query = 	"select serverAddress, serverPort, serverName, iconPath from globalServers";
			return DBUtils.doInResultSet(statement, query, rs -> {
				
				ArrayList<RockNetServer> serverList = new ArrayList<>();
				while(rs.next()) {
					serverList.add(new RockNetServer(rs.getString("serverAddress"), rs.getInt("serverPort"), rs.getString("serverName"), rs.getString("iconPath"),false));
				}
				return serverList;
			});
		});
	}

	@Override
	public RockNetServer getServer(String serverAddress, int serverPort) {
		if(StringUtils.isNotBlank(serverAddress) && serverPort > 0 && serverPort <= 65535)
			return DBUtils.doInStatement(connection, statement -> {
				String query = 	"select serverAddress, serverPort, '' as serverName, iconPath from servers"
								+ "where serverAddress = '"+serverAddress+"' and serverPort = "+serverPort;
				return DBUtils.doInResultSet(statement, query, rs -> {
					if(rs.next())						
						return new RockNetServer(rs.getString("serverAddress"), rs.getInt("serverPort"), rs.getString("serverName"), rs.getString("iconPath"),false);
					else return null;
				});
			});
		else return null;
	}

	@Override
	public RockNetServer getGlobalServer(String serverAddress, int serverPort) {
		if(StringUtils.isNotBlank(serverAddress) && serverPort > 0 && serverPort <= 65535)
			return DBUtils.doInStatement(connection, statement -> {
				String query = 	"select serverAddress, serverPort, serverName, iconPath from globalServers"
								+ "where serverAddress = '"+serverAddress+"' and serverPort = "+serverPort;
				return DBUtils.doInResultSet(statement, query, rs -> {
					if(rs.next())						
						return new RockNetServer(rs.getString("serverAddress"), rs.getInt("serverPort"), rs.getString("serverName"), rs.getString("iconPath"), false);
					else return null;
				});
			});
		else return null;
	}

	@Override
	public RockNetServer createServer(String serverAddress, int serverPort, String iconPath, boolean preferred) {
		if(StringUtils.isNotBlank(serverAddress) && serverPort > 0 && serverPort <= 65535 && iconPath != null)
			return DBUtils.doInStatement(connection, statement -> {
				statement.executeUpdate("insert into servers values ('"+serverAddress+"',"+serverPort+",'"+iconPath+"')");
				return new RockNetServer(serverAddress, serverPort, "", iconPath, preferred);
			});
		else return null;
	}

	@Override
	public RockNetServer createGlobalServer(String serverAddress, int serverPort, String serverName, String iconPath) {
		if(StringUtils.isNotBlank(serverAddress) && serverPort > 0 && serverPort <= 65535 && iconPath != null && serverName != null)
			return DBUtils.doInStatement(connection, statement -> {
				statement.executeUpdate("insert into globalServers values ('"+serverAddress+"',"+serverPort+",'"+serverName+"','"+iconPath+"')");
				return new RockNetServer(serverAddress, serverPort, serverName, iconPath, false);
			});
		else return null;
	}

	@Override
	public void updateServer(String xuid,RockNetServer from, RockNetServer to) {
		if(StringUtils.isNotBlank(xuid) && from != null && 
			StringUtils.isNotBlank(from.getServerAddress()) && 
			from.getServerPort() > 0 && from.getServerPort() <= 65535 && 
			to != null && StringUtils.isNotBlank(to.getServerAddress()) && 
			to.getServerPort() > 0 && to.getServerPort() <= 65535 && 
			StringUtils.isNotBlank(to.getServerName()) && to.getIconPath() != null
			)
			DBUtils.doInStatement(connection, statement -> {
				int pref = 0;
				if(to.isPreferred()) pref = 1;
				statement.executeUpdate("update servers set iconPath = '"+to.getIconPath()+"' "
						+ "where serverAddress = '"+from.getServerAddress()+"' and serverPort = "+from.getServerPort());
				statement.executeUpdate("update usersServers set serverName = '"+to.getServerName()+"', preferred = " + pref + " "
						+ "where xuid = "+xuid+" and serverAddress = '"+from.getServerAddress()+"' and serverPort = "+from.getServerPort());
				return null;
			});
		
	}

	@Override
	public void updateGlobalServer(RockNetServer server) {
		if(server != null && StringUtils.isNotBlank(server.getServerAddress()) && 
			server.getServerPort() > 0 && server.getServerPort() <= 65535 && 
			StringUtils.isNotBlank(server.getServerName()) && server.getIconPath() != null)
			DBUtils.doInStatement(connection, statement -> {
				statement.executeUpdate("update globalServers set iconPath = '"+server.getIconPath()+"', serverName = '"+server.getServerName()+"' "
						+ "where serverAddress = '"+server.getServerAddress()+"' and serverPort = "+server.getServerPort());
				return null;
			});
		
	}

	@Override
	public void removeServer(RockNetServer server) {
		if(server != null)
			removeServer(server.getServerAddress(),server.getServerPort());
	}

	@Override
	public void removeServer(String serverAddress, int serverPort) {
		if(StringUtils.isNotBlank(serverAddress) && serverPort > 0 && serverPort <= 65535)
			DBUtils.doInStatement(connection, statement -> {
				statement.executeUpdate("delete from usersServers where serverAddress = '"+serverAddress+"' and serverPort = "+serverPort);
				statement.executeUpdate("delete from servers where serverAddress = '"+serverAddress+"' and serverPort = "+serverPort);
				return null;
			});
	}

	@Override
	public void removeGlobalServer(RockNetServer server) {
		if(server != null)
			removeGlobalServer(server.getServerAddress(),server.getServerPort());
	}

	@Override
	public void removeGlobalServer(String serverAddress, int serverPort) {
		if(StringUtils.isNotBlank(serverAddress) && serverPort > 0 && serverPort <= 65535)
			DBUtils.doInStatement(connection, statement -> {
				statement.executeUpdate("delete from globalServers where serverAddress = '"+serverAddress+"' and serverPort = "+serverPort);
				return null;
			});		
	}

}
