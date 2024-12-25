package com.stellantis.team.utility.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ExtractProperties {
	private static ExtractProperties extractProperties;
	private static String serverURLList;
	
	private static Properties externalProperties = new Properties();
	static{
		try {
			FileReader filereader = new FileReader("CustomExtension.properties");
			externalProperties.load(filereader);
			
			setServerURLList(externalProperties.getProperty(UtilityConstants.SELECT_SERVER_LIST));
		} catch (FileNotFoundException e) {
			CustomLogger.logException(e);
		} catch (IOException e) {
			CustomLogger.logException(e);
		}
	}
	
	public static ExtractProperties getInstance() {
		if (extractProperties == null)
			extractProperties = new ExtractProperties();
		return extractProperties;
	}

	private ExtractProperties() {
		
	}
	
	/* Business Entities */
	public static String getServerURLList() {
		return serverURLList;
	}

	public static void setServerURLList(String serverURLList) {
		ExtractProperties.serverURLList = serverURLList;
	}
}
