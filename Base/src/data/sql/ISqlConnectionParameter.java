package base.data.sql;

import java.util.concurrent.ConcurrentHashMap;

public interface ISqlConnectionParameter
{
	/**
	 * 参数值集
	 */
	public ConcurrentHashMap<String, Object> getParameters();

	/**
	 * 加入一个参数值
	 * @param key 参数标识key
	 * @param value 值
	 */
	public <T> void addParameter(String key, T value);

	public void dispose();
}