package it.wtfcode.rocknet.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DBUtils {

	private static final Logger log = LogManager.getLogger(DBUtils.class.getName());
	
	public static List<String> loadSQLFile(String resourcePath){
		InputStream resource = DBUtils.class.getClassLoader().getResourceAsStream(resourcePath);
		String loadedFile = new BufferedReader(new InputStreamReader(resource)).lines().collect(Collectors.joining("\n"));
		return Arrays.asList(StringUtils.split(loadedFile, ";"));
	}

	public static boolean isDbEmpty(Statement statement,  String checkQuery, int checkQueryExpectedResult) {
		return doInResultSet(statement, checkQuery, check -> {
			if(check.next() && check.getInt(1) == checkQueryExpectedResult)
				return false;
			else return true;
		});
	}

	public static void executeSQLResource(Statement statement, String resourcePath) throws SQLException {
		List<String> queries = loadSQLFile(resourcePath);
		for(String query:queries) {
			statement.addBatch(query);
		}
		statement.executeBatch();
	}
	
	public static void closeResultSet(ResultSet resultSet) {
		if(resultSet != null)
			try {
				resultSet.close();
			} catch (SQLException e) {
				log.error(e);
			}
	}

	public static void closeStatement(Statement statement) {
		if(statement != null)
			try {
				statement.close();
			} catch (SQLException e) {
				log.error(e);
			}
	}

	@SuppressWarnings("unchecked")
	public static <V> V doInStatement(Connection connection,StatementTask task) {
		if(connection != null && task != null) {
			Statement statement = null;
			try {
				statement = connection.createStatement();
				
				return (V) task.run(statement);
				
			} catch(SQLException e)	{
				log.error(e);
				closeStatement(statement);
			} finally {
				closeStatement(statement);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <V> V doInResultSet(Statement statement, String sqlQuery, ResultSetTask task) {
		if(statement != null && StringUtils.isNotBlank(sqlQuery) && task != null) {
			ResultSet rs = null;
			try {
				rs = statement.executeQuery(sqlQuery);
				
				return (V) task.run(rs);
				
			}catch(SQLException e) {
				log.error(e);
				closeResultSet(rs);
			}finally {
				closeResultSet(rs);
			}
		}
		return null;
	}
	
	public interface StatementTask {
		public Object run(Statement statement) throws SQLException;
	}
	public interface ResultSetTask {
		public Object run(ResultSet resultSet) throws SQLException;
	}
	
}