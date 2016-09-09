package base.data.sql;

import java.util.Properties;


public interface ISqlConnectionConfig
{
	/**
	 * 初始化
	 * @param properties 配置
	 * @param name 标识名称
	 */
	public void init(Properties properties, String name);
	
	/**
	 * 配置实体
	 */
	public ISqlConnectionConfigBean getSqlConnectionConfigBean();
}
