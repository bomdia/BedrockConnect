package it.wtfcode.rocknet;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.wtfcode.rocknet.pojo.RockNetConfig;

public class Main {

	public static final String CONFIGURATION = "config.json";
	
	public static void main(String[] args) {
		
		
	}

	private static RockNetConfig readConfiguration() {
		ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		try {
			return mapper.readValue(new File(CONFIGURATION), RockNetConfig.class);
		} catch (IOException e) {
			return new RockNetConfig();
		}
	}
}
