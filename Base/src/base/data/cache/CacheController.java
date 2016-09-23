package base.data.cache;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.Base;
import base.data.cache.table.CacheDataTable;
import base.data.cache.table.CacheTable;
import base.data.cache.table.ICacheDataTable;
import base.data.cache.table.ICacheTable;
import base.types.JavaType;
import base.utils.StringUtil;

/**
 * @author Devin
 *缓存控制器
 */
public class CacheController
{
	/**
	 * 缓存项存储器
	 */
	private ConcurrentHashMap<String, Object> caches = new ConcurrentHashMap<String, Object>();
	/**
	 * 缓存表项存储器
	 */
	private ConcurrentHashMap<String, ICacheTable> cacheTables = new ConcurrentHashMap<String, ICacheTable>();
	/**
	 * 数据库缓存表项存储器
	 */
	private ConcurrentHashMap<String, ICacheDataTable> cacheDataTables = new ConcurrentHashMap<String, ICacheDataTable>();

	/**
	 * 创建缓存
	 * @param name 缓存名
	 * @return CacheBase<K, V> 创建后的缓存，如果遇到错误则返回null
	 */
	public <K, V> ICache<K, V> createCacheBase(String name)
	{
		try
		{
			if (caches.containsKey(name))
			{
				Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, new Exception("将要创建的缓存 " + name + " 已经存在。"));
				return null;
			}
			ICache<K, V> chche = Base.newClass(CacheBase.class);
			chche.init(name);
			caches.put(name, chche);
			return chche;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
	}

	/**
	 * 创建缓存
	 * @param name 缓存名
	 * @param initialCapacity 初始容量大小
	 * @return CacheBase<K, V> 创建后的缓存，如果遇到错误则返回null
	 */
	public <K, V> ICache<K, V> createCacheBase(String name, int initialCapacity)
	{
		try
		{
			if (caches.containsKey(name))
			{
				Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, new Exception("将要创建的缓存 " + name + " 已经存在。"));
				return null;
			}
			ICache<K, V> chche = Base.newClass(CacheBase.class);
			chche.init(name, initialCapacity);
			caches.put(name, chche);
			return chche;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
	}

	/**
	 * 取得缓存
	 * @param name 缓存名
	 * @return CacheBase<K, V> 缓存表，如果遇到错误则返回null
	 */
	@SuppressWarnings("unchecked")
	public <K, V> ICache<K, V> getCacheBase(String name)
	{
		try
		{
			if (caches.containsKey(name))
			{
				return (ICache<K, V>) caches.get(name);
			}
			return null;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
	}

	/**
	 * 指定的缓存表是否存在
	 * @param name　缓存名
	 * @return　如果存在返回true,否则返回false
	 */
	public boolean containsCacheBase(String name)
	{
		if (StringUtil.isEmptyOrNull(name))
			return false;
		return caches.containsKey(name);
	}

	/**
	 * 移除缓存项
	 * @param <K>
	 * @param <V>
	 * @param name 缓存名
	 */
	@SuppressWarnings("unchecked")
	public <K, V> void removeCacheBase(String name)
	{
		try
		{
			if (caches.containsKey(name))
			{
				ICache<K, V> cache = (ICache<K, V>) caches.remove(name);
				if (cache != null)
				{
					cache.dispose();
					cache = null;
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 创建缓存表
	 * @param tableName 缓存表名
	 * @param primaryKeyFieldName 表主键字段名
	 * @param keyFieldNameGroups 键字段名列表组，值不允许为空
	 * @return CacheTable 创建后的缓存表，如果遇到错误则返回null
	 */
	public ICacheTable createCacheTable(String tableName, String primaryKeyFieldName, String[][] keyFieldNameGroups)
	{
		try
		{
			if (cacheTables.containsKey(tableName))
			{
				Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, new Exception("将要创建的缓存表 " + tableName + " 已经存在。"));
				return null;
			}
			ICacheTable cacheTable = Base.newClass(CacheTable.class);
			cacheTable.init(tableName, primaryKeyFieldName, keyFieldNameGroups);
			cacheTables.put(tableName, cacheTable);
			return cacheTable;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
	}

	/**
	 * 取得缓存表
	 * @param tableName 缓存表名
	 * @return CacheTable 缓存表，如果遇到错误则返回null
	 */
	public ICacheTable getCacheTable(String tableName)
	{
		try
		{
			return cacheTables.get(tableName);
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
	}

	/**
	 * 移除缓存表项
	 * @param <K>
	 * @param <V>
	 * @param name 缓存表名
	 */
	public void removeCacheTable(String tableName)
	{
		try
		{
			if (cacheTables.containsKey(tableName))
			{
				ICacheTable cacheTable = cacheTables.remove(tableName);
				if (cacheTable != null)
				{
					cacheTable.dispose();
					cacheTable = null;
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 创建数据库缓存表
	 * @param name 缓存表名称
	 * @param primaryKey 主键字段名(数据表中必须要将此字段设置为主键，不然会数据存储出错)
	 * @param keyFieldNameGroups 集合键字段名列表组，此值将做为行数据key，如果指定多个字段，中间以“_”连接，最终数据为：[0]字段值_[1]字段值_...
	 * @param sqlConnectionPoolName 数据库连接池名称
	 * @param sqlTableName 数据库表名
	 * @return CacheTable 创建后的缓存表，如果遇到错误则返回null
	 */
	public ICacheDataTable createCacheDataTable(String name, String primaryKey, String[][] keyFieldNameGroups, String sqlConnectionPoolName, String sqlTableName)
	{
		return createCacheDataTable(name, primaryKey, keyFieldNameGroups, -1, sqlConnectionPoolName, sqlTableName, null, null);
	}

	/**
	 * 创建数据库缓存表
	 * @param name 缓存表名称
	 * @param primaryKey 主键字段名(数据表中必须要将此字段设置为主键，不然会数据存储出错)
	 * @param keyFieldNameGroups 集合键字段名列表，此值将做为行数据key，如果指定多个字段，中间以“_”连接，最终数据为：[0]字段值_[1]字段值_...
	 * @param sqlConnectionPoolName 数据库连接池名称
	 * @param sqlTableName 数据库表名
	 * @param customFieldNames 自定义的字段名数组
	 * @param customFieldJaveTypes 对应的自定义的字段数据类型数组，其长度必须与customFieldNames相等且对应
	 * @return CacheTable 创建后的缓存表，如果遇到错误则返回null
	 */
	public ICacheDataTable createCacheDataTable(String name, String primaryKey, String[][] keyFieldNameGroups, String sqlConnectionPoolName, String sqlTableName, String[] customFieldNames, JavaType[] customFieldJaveTypes)
	{
		return createCacheDataTable(name, primaryKey, keyFieldNameGroups, -1, sqlConnectionPoolName, sqlTableName, customFieldNames, customFieldJaveTypes);
	}
	
	/**
	 * 创建数据库缓存表
	 * @param name 缓存表名称
	 * @param primaryKey 主键字段名(数据表中必须要将此字段设置为主键，不然会数据存储出错)
	 * @param keyFieldNameGroups 集合键字段名列表组，此值将做为行数据key，如果指定多个字段，中间以“_”连接，最终数据为：[0]字段值_[1]字段值_...
	 * @param capacity 数据行记录容量的大小，使用LRU算法淘汰机制，值为-1时则表示不限容量
	 * @param sqlConnectionPoolName 数据库连接池名称
	 * @param sqlTableName 数据库表名
	 * @param customFieldNames 自定义的字段名数组
	 * @param customFieldJaveTypes 对应的自定义的字段数据类型数组，其长度必须与customFieldNames相等且对应
	 * @return CacheTable 创建后的缓存表，如果遇到错误则返回null
	 */
	public ICacheDataTable createCacheDataTable(String name, String primaryKey, String[][] keyFieldNameGroups, int capacity, String sqlConnectionPoolName, String sqlTableName, String[] customFieldNames, JavaType[] customFieldJaveTypes)
	{
		try
		{
			if (cacheDataTables.containsKey(name))
			{
				Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, new Exception("将要创建的数据库缓存表 " + name + " 已经存在。"));
				return null;
			}
			ICacheDataTable cacheDataTable = Base.newClass(CacheDataTable.class);
			cacheDataTable.init(name, primaryKey, keyFieldNameGroups, capacity, sqlConnectionPoolName, sqlTableName, customFieldNames, customFieldJaveTypes);
			cacheDataTables.put(name, cacheDataTable);
			return cacheDataTable;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
	}

	/**
	 * 取得数据库缓存表
	 * @param name 缓存表名
	 * @return CacheDataTable 缓存表，如果未找到或遇到错误则返回null
	 */
	public ICacheDataTable getCacheDataTable(String name)
	{
		try
		{
			return cacheDataTables.get(name);
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
	}
	
	/**
	 * 保存所有缓存数据表数据至物理数据库
	 */
	public boolean saveAllCacheDataTable()
	{
		try
		{
			if(cacheDataTables!=null && cacheDataTables.size()>0)
			{
				Iterator<Entry<String, ICacheDataTable>> iterator = cacheDataTables.entrySet().iterator();
				while(iterator.hasNext())
				{
					Entry<String, ICacheDataTable> cdt = iterator.next();
					ICacheDataTable cacheDataTable = cdt.getValue();
					if(cacheDataTable.getIsAutoUpdate())
					{
						cacheDataTable.updateToDataByChange();
					}
				}
			}
			return true;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, e);
		}
		return false;
	}
}
