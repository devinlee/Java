package base.data.sql;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SqlConnectionParameter implements ISqlConnectionParameter
{
	/**
	 * 参数值集
	 */
	private ConcurrentHashMap<String, Object> parameters = new ConcurrentHashMap<String, Object>();

	/**
	 * 参数值集
	 */
	public ConcurrentHashMap<String, Object> getParameters()
	{
		return this.parameters;
	}

	public SqlConnectionParameter()
	{

	}

	/**
	 * 加入一个参数值
	 * @param key 参数标识key
	 * @param value 值
	 */
	public <T> void addParameter(String key, T value)
	{
		try
		{
			parameters.put(key, value);
		}
		catch (Exception e)
		{
			Logger.getLogger(SqlConnectionParameter.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	public synchronized void dispose()
	{
		if (parameters != null)
		{
			parameters.clear();
			parameters = null;
		}
	}
}
