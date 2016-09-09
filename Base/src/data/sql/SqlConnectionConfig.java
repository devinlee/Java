package base.data.sql;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.Base;

/**
 * 数据库连接池配置
 * @author Devin
 */
public class SqlConnectionConfig implements ISqlConnectionConfig
{
	/**
	 * 配置实体
	 */
	private volatile ISqlConnectionConfigBean sqlConnectionConfigBean;

	/**
	 * 配置实体
	 */
	public ISqlConnectionConfigBean getSqlConnectionConfigBean()
	{
		return sqlConnectionConfigBean;
	}

	/**
	 * 数据库连接池配置
	 */
	public SqlConnectionConfig()
	{
	}
	
	/**
	 * 初始化
	 * @param properties 配置
	 * @param name 标识名称
	 */
	public void init(Properties properties, String name)
	{
		try
		{
			sqlConnectionConfigBean = Base.newClass(SqlConnectionConfigBean.class);
			sqlConnectionConfigBean.setPoolName(properties.getProperty(name+"_poolName"));
			sqlConnectionConfigBean.setDriverName(properties.getProperty(name+"_driverName"));
			sqlConnectionConfigBean.setUrl(properties.getProperty(name+"_url"));
			sqlConnectionConfigBean.setUserName(properties.getProperty(name+"_userName"));
			sqlConnectionConfigBean.setPassword(properties.getProperty(name+"_password"));
			sqlConnectionConfigBean.setInitConnections(Integer.parseInt(properties.getProperty(name+"_initConnections")));
			sqlConnectionConfigBean.setMaxFreeConnections(Integer.parseInt(properties.getProperty(name+"_maxFreeConnections")));
			sqlConnectionConfigBean.setMaxActiveConnections(Integer.parseInt(properties.getProperty(name+"_maxActiveConnections")));
			sqlConnectionConfigBean.setConnectionTimeOut(Integer.parseInt(properties.getProperty(name+"_connectionTimeOut")));
		}
		catch (Exception e)
		{
			Logger.getLogger(SqlConnectionConfig.class.getName()).log(Level.SEVERE, null, e);
		}
	}
}
