package com.whyun.util.http;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.whyun.util.ConfigFileUtil;

public class ClientConfig {
	private static  Logger logger = Logger.getLogger(ClientConfig.class);
	private static final String CONFIG_NAME = "spconfig.properties";
	private static Properties p = null;
	static {
		p = new Properties();
		String configPath = ConfigFileUtil.getPath(ClientConfig.class,CONFIG_NAME);
		if (configPath != null) {			
			try {
				InputStream input = new FileInputStream(configPath);
				p.load(input);
			} catch (FileNotFoundException e) {
				logger.error("",e);
			} catch (IOException e) {
				logger.error("",e);
			}			
		} else {
			logger.warn("没有发现配置文件spconfig.properties");
		}
	}
	
	/**
	 * 根据配置文件中的键，返回其字符串类型的值
	 * 
	 * @param key the key
	 * 
	 * @return the value
	 */
	public static String getValue(String key) {
		String value = p.getProperty(key);
		return value;
	}
	
	/**
	 * 根据配置文件中的键，返回其整数类型的值，如果不能转化为整数，返回0.
	 * 
	 * @param key the key
	 * 
	 * @return the int
	 */
	public static int getInt(String key) {
		String str = getValue(key);
		int valueInt = 0;
		if (str != null) {
			try {
				valueInt = Integer.parseInt(str);
			} catch (Exception e) {
				logger.debug(e);
			}
		}
		logger.debug(key + "->" + valueInt);
		return valueInt;
	}
	
	public static void traceInfo(String key) {
		logger.info(key + "->" + p.getProperty(key));
	}
}
