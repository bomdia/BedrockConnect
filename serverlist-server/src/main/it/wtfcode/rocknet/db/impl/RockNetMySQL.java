package it.wtfcode.rocknet.db.impl;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.wtfcode.rocknet.utils.DBUtils;

public class RockNetMySQL extends RockNetSQLite {

	private static final Logger log = LogManager.getLogger(RockNetMySQL.class.getName());
	private static final String CHECK_QUERY = 
			"select count(a.TABLE_NAME) from (" + 
			"SELECT TABLE_SCHEMA,TABLE_NAME FROM information_schema.TABLES " + 
			"union " + 
			"select TABLE_SCHEMA,TABLE_NAME from information_schema.VIEWS" + 
			") a where a.TABLE_NAME in ('servers','globalServers','users','usersServers','usersWithServers') and a.TABLE_SCHEMA = ";
	private static final int CHECK_QUERY_RESULT = 5;
	private static final String POPULATE_FILE = "populateSqlDb.sql";

	@Override
	public void initialize(Map<String, Object> dbVariable) {
		if(dbVariable == null) dbVariable = new HashMap<>();
		try {
			 Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			log.error("MYSQL JDBC driver not found (very strange, pls report to RockNet developer!)");
		}
        String address 	= (String) dbVariable.getOrDefault("address",	"localhost");
        String database = (String) dbVariable.getOrDefault("database",	"bedrock-connect");
        String username = (String) dbVariable.getOrDefault("username",	"root");
        String password = (String) dbVariable.getOrDefault("password",	"");
		
		try {
			connection = DriverManager.getConnection("jdbc:mysql://"+address+"/"+database+"?" +
                "user="+username+"&password="+password);
		} catch (SQLException e) {
			log.error(e);
		}
		
		DBUtils.doInStatement(connection, statement -> {
			if(DBUtils.isDbEmpty(statement,CHECK_QUERY+"'"+database+"'",CHECK_QUERY_RESULT))			
				DBUtils.executeSQLResource(statement, POPULATE_FILE);
			return null;
		});

	}
}
