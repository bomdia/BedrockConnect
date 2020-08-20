package it.wtfcode.rocknet.db.impl;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.wtfcode.rocknet.db.IRockNetDB;
import it.wtfcode.rocknet.pojo.RockNetServer;
import it.wtfcode.rocknet.pojo.RockNetUser;
import it.wtfcode.rocknet.utils.DBUtils;

public class RockNetMySQL extends RockNetSQLite {

	private static final Logger log = LogManager.getLogger(RockNetMySQL.class.getName());
	private static final String CHECK_QUERY = 
			"SELECT count(name) as \"check\" FROM sqlite_master WHERE type in ('table','view') " + 
			"and name in ('servers','globalServers','users','usersServers','usersWithServers')";
	private static final int CHECK_QUERY_RESULT = 5;
	private static final String POPULATE_FILE = "SQLite.populatedb.sql";

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
}
