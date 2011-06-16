/**
 * 
 */
package com.sql.dynamicquery;

import java.util.Properties;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author DirectXMan12
 *
 */
public class DynamicQueryDatabaseConfigurator implements Cloneable
{
	private static DynamicQueryDatabaseConfigurator _storedConfig;
	
	private Properties _connProps;
	private String _dbConnectionString;
	
	private DynamicQueryDatabaseConfigurator()
	{
		_connProps = new Properties();
	}
	
	public static String getDatabaseString()
	{
		if (_storedConfig == null) throw new RuntimeException("You haven't yet created a configuration!");
		return _storedConfig._dbConnectionString;
	}
	
	public static void setDatabaseString(String dbStr)
	{
		if (_storedConfig == null) _storedConfig = new DynamicQueryDatabaseConfigurator();
		_storedConfig._dbConnectionString = dbStr;
	}
	
	public static void setCredentials(String username, String password)
	{
		if (_storedConfig == null) _storedConfig = new DynamicQueryDatabaseConfigurator();
		_storedConfig._connProps.setProperty("user", username);
		_storedConfig._connProps.setProperty("password", password);
	}
	
	public static void setOtherProperty(String key, String value)
	{
		if (_storedConfig == null) _storedConfig = new DynamicQueryDatabaseConfigurator();
		_storedConfig._connProps.setProperty(key, value);
	}
	
	public static void replaceProperties(Properties props)
	{
		if (_storedConfig == null) _storedConfig = new DynamicQueryDatabaseConfigurator();
		_storedConfig._connProps = props;
	}
	
	public static Properties getProperties()
	{
		if (_storedConfig == null) throw new RuntimeException("You haven't yet created a configuration!");
		return _storedConfig._connProps;
	}
	
	public static String getUsername()
	{
		if (_storedConfig == null) throw new RuntimeException("You haven't yet created a configuration!");
		return _storedConfig._connProps.getProperty("user");
	}
	
	public static String getPassword()
	{
		if (_storedConfig == null) throw new RuntimeException("You haven't yet created a configuration!");
		return _storedConfig._connProps.getProperty("password");
	}
	
	public static DynamicQueryDatabaseConfigurator cloneConfiguration()
	{
		if (_storedConfig == null) throw new RuntimeException("You haven't yet created a configuration!");
		try
		{
			return (DynamicQueryDatabaseConfigurator) _storedConfig.clone();
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static DynamicQueryDatabaseConfigurator swapConfiguration(DynamicQueryDatabaseConfigurator config)
	{
		DynamicQueryDatabaseConfigurator oldConf = cloneConfiguration();
		_storedConfig = config;
		return oldConf;
	}
}
