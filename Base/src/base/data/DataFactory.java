package base.data;

import base.data.cache.CacheController;
import base.data.sql.SqlController;

public class DataFactory
{
	/**
	 * 数据库连接池控制器
	 */
	private final static SqlController sqlController = new SqlController();
	/**
	 * 缓存控制器
	 */
	private final static CacheController cacheController = new CacheController();

	private DataFactory()
	{
	}

	/**
	 * 数据库连接池控制器
	 * @return sqlController
	 */
	public static SqlController sqlController()
	{
		return sqlController;
	}

	/**
	 * 缓存控制器
	 * @return
	 */
	public static CacheController cacheController()
	{
		return cacheController;
	}
}
