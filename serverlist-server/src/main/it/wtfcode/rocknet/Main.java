package it.wtfcode.rocknet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.wtfcode.rocknet.db.IRockNetDB;
import it.wtfcode.rocknet.db.impl.RockNetSQLite;
import it.wtfcode.rocknet.pojo.RockNetConfig;
import it.wtfcode.rocknet.utils.MainUtil;
import it.wtfcode.rocknet.utils.PaletteManager;

public class Main {

	private static final String RETURNING_TO_MAIN_MENU = "Returning to Main Menù";
	private static final String RETURN_TO_MAIN_MENU = "Return(r) 	- return to main menù";
	private static final Logger log = LogManager.getLogger(Main.class.getName());
	public static final ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	public static final String CONFIGURATION = "config.json";
	private static boolean loops = true;
	private static RockNetConfig config;
	private static PaletteManager paletteManager = new PaletteManager();
	private static RockNetServerInstance rockNetServer;
	
	public static void main(String[] args) {
		
		System.out.println("Inizialing RockNet Server.");
		System.out.println("Reading Config file.");
		config = readConfiguration();
		
		System.out.println(config.isEnableDebug() ? "Enabling Debug.":"Disabling Debug.");
		if(!config.isEnableDebug())
			LogManager.shutdown();
		
		System.out.println("Starting RockNetServer");
		initialize();
			
		System.out.println("Good Morning RockNetter!!");
		System.out.println();
		System.out.println();
		System.out.println();
    	while(loops) {
        	try {
	        	switch(showMenu()) {
	        	case "config":
	        	case "c":
	        		System.out.println("Entering Config Management!");
	        		configMain();
	        	break;
	        	case "users":
	        	case "u":
	        		System.out.println("Entering Users Management!");
	        		usersMain();
	        	break;
	        	case "servers":
	        	case "s":
	        		System.out.println("Entering Servers Management!");
	        		serversMain();
	        	break;
	        	case "quit":
	        	case "q":
	        		loops = false;
	        		break;
	        	}
        	}catch(Exception e) {
        		log.error("generic error: ",e);
        	}
        }
		System.out.println("Bye Bye, i'll wait you forever! <3");
	}
	
	private static void configMain() {
		boolean inloops = true;
		while(inloops ) {
        	try {
	        	switch(showConfigMenu()) {
	        	case "return":
	        	case "r":
	        		inloops = false;
	        		break;
	        	}
        	}catch(Exception e) {
        		log.error("generic error: ",e);
        	}
        }
		System.out.println(RETURNING_TO_MAIN_MENU);
	}

	private static void usersMain() {
		boolean inloops = true;
		while(inloops ) {
        	try {
	        	switch(showUsersMenu()) {
	        	case "return":
	        	case "r":
	        		inloops = false;
	        		break;
	        	}
        	}catch(Exception e) {
        		log.error("generic error: ",e);
        	}
        }
		System.out.println(RETURNING_TO_MAIN_MENU);
	}

	private static void serversMain() {
		boolean inloops = true;
		while(inloops ) {
        	try {
	        	switch(showServersMenu()) {
	        	case "return":
	        	case "r":
	        		inloops = false;
	        		break;
	        	}
        	}catch(Exception e) {
        		log.error("generic error: ",e);
        	}
        }
		System.out.println(RETURNING_TO_MAIN_MENU);
	}

	private static String showMenu() {
		System.out.println("				[ MAIN MENÙ ]");
		System.out.println("Config(c)	- manage RockNet config");
		System.out.println("Users(u) 	- manage registered users");
		System.out.println("Servers(s) 	- manage registered servers");
		System.out.println("Quit(q) 	- exit RockNet");
		
		return MainUtil.readLine().toLowerCase();
	}
	
	private static String showConfigMenu() {
		System.out.println("				[ CONFIG MENÙ ]");
		System.out.println(RETURN_TO_MAIN_MENU);
		
		return MainUtil.readLine().toLowerCase();
	}
	
	private static String showUsersMenu() {
		System.out.println("				[ USERS MENÙ ]");
		System.out.println("Clear(c)	- remove all registered users");
		System.out.println("Show(s)		- show all registered users");
		System.out.println(RETURN_TO_MAIN_MENU);
		
		return MainUtil.readLine().toLowerCase();
	}
	
	private static String showServersMenu() {
		System.out.println("				[ SERVERS MENÙ ]");
		System.out.println("Clear(c) 	- remove all registered servers");
		System.out.println("Show(s) 	- show all registered servers");
		System.out.println(RETURN_TO_MAIN_MENU);
		
		return MainUtil.readLine().toLowerCase();
	}
	
	private static void initialize() {
		// load database driver jar
		loadDBDriverJar();
		
		// initialize RockNetDB Interface from config
		// and start RockNet Server
		rockNetServer = new RockNetServerInstance(initializeRockNetDB());
		
		//add the call of deinitialize if is initialized and we are closing
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				deinitalize();
			}
		});
	}

	private static IRockNetDB initializeRockNetDB() {
		IRockNetDB dbInterfaceInstance = null;
		boolean trySuccesfull = false;
		try {
			Class<?> dbi = Class.forName(config.getDbDriverClass());
			if(IRockNetDB.class.isAssignableFrom(dbi)) {
				try {
					dbInterfaceInstance = (IRockNetDB) dbi.newInstance();
					trySuccesfull = true;
				} catch (InstantiationException | IllegalAccessException e) {
					log.error(e);
				}
			}
		} catch (ClassNotFoundException e) {
			log.error(e);
		}
		if(!trySuccesfull || dbInterfaceInstance == null) dbInterfaceInstance = new RockNetSQLite();
		
		dbInterfaceInstance.initialize(config.getDbDriverData());
		return dbInterfaceInstance;
	}
	
	private static void deinitalize() {
		rockNetServer.stop();
		writeConfiguration(config);
	}
	
	private static void loadDBDriverJar() {
		
	}
	
	private static RockNetConfig readConfiguration() {
		try {
			return mapper.readValue(new File(CONFIGURATION), RockNetConfig.class);
		} catch (FileNotFoundException e) { 
			RockNetConfig rockNetConfig = new RockNetConfig();
			writeConfiguration(rockNetConfig);
			return rockNetConfig;
		} catch (IOException e1) {
			log.error("can't read config file loading default",e1);
			RockNetConfig rockNetConfig = new RockNetConfig();
			writeConfiguration(rockNetConfig);
			return rockNetConfig;
		}
	}
	
	private static void writeConfiguration(RockNetConfig config) {
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(CONFIGURATION), config);
		} catch (IOException e) {
			try {
				mapper.writeValue(new File(CONFIGURATION), new RockNetConfig());
			} catch (IOException e1) {
				log.error("can't write the configuration file!",e1);
			}
		}
	}

	public static RockNetConfig getConfig() {
		return config;
	}

	public static PaletteManager getPaletteManager() {
		return paletteManager;
	}
	
}
