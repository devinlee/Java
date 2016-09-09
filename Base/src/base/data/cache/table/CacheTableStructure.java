package base.data.cache.table;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 缓存表结构
 * @author Devin
 *
 */
public class CacheTableStructure implements ICacheTableStructure
{
	/**
	 * 线程读写锁
	 */
	protected final ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * 标识Key
	 */
	private String key;

	/**
	 * 标识Key
	 * @return key
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * 隶属于的上级结构
	 */
	private ICacheTableStructure parent;

	/**
	 * @return parent
	 */
	public ICacheTableStructure getParent()
	{
		return parent;
	}

	/**
	 * @param parent 要设置的 parent
	 */
	public void setParent(ICacheTableStructure parent)
	{
		lock.writeLock().lock();
		try
		{
			this.parent = parent;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableStructure.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	private ConcurrentHashMap<String, ICacheTableStructure> concurrentHashMap = new ConcurrentHashMap<String, ICacheTableStructure>();
	
	/**
	 * 结构行数据列表
	 */
	private Vector<ICacheTableRow> rows = new Vector<ICacheTableRow>();
	/**
	 * 结构行数据列表
	 */
	public Vector<ICacheTableRow> getRows()
	{
		return rows;
	}

	public CacheTableStructure()
	{
	}
	
	/**
	 * 缓存表结构
	 * @param parent 上级结构
	 * @param key 标识key
	 */
	public void init(ICacheTableStructure parent, String key)
	{
		this.parent = parent;
		this.key = key;
	}

	/**
	 * 加入
	 * @param key 标识key
	 * @param cacheTableStructure 缓存表结构
	 */
	public void put(String key, ICacheTableStructure cacheTableStructure)
	{
		concurrentHashMap.put(key, cacheTableStructure);
	}

//	/**
//	 * 加入
//	 * @param key
//	 * @param cacheTableRow 行
//	 */
//	public void put(String key, CacheTableRow cacheTableRow)
//	{
//		lock.writeLock().lock();
//		try
//		{
//			concurrentHashMap.put(key, cacheTableRow);
//			cacheTableRow.setParent(this);
//			cacheTableRow.setKey(key);
//		}
//		catch (Exception e)
//		{
//			Logger.getLogger(CacheTableStructure.class.getName()).log(Level.SEVERE, null, e);
//		}
//		finally
//		{
//			lock.writeLock().unlock();
//		}
//	}
	
	/**
	 * 加入
	 * @param cacheTableRow 行
	 */
	public void put(ICacheTableRow cacheTableRow)
	{
		lock.writeLock().lock();
		try
		{
//			cacheTableRow.setParent(this);
			rows.add(cacheTableRow);
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableStructure.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * 插入
	 * @param cacheTableRow 行
	 * @param cacheTableRow 要插入的索引位置
	 */
	public void insert(ICacheTableRow cacheTableRow, int index)
	{
		lock.writeLock().lock();
		try
		{
			rows.insertElementAt(cacheTableRow, index);
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableStructure.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 取得
	 * @param key
	 * @param value
	 */
	public ICacheTableStructure get(String key)
	{
		return concurrentHashMap.get(key);
	}

	/**
	 * 移除
	 * @param key
	 * @return
	 */
	public ICacheTableStructure remove(String key)
	{
		return concurrentHashMap.remove(key);
	}

	/**
	 * 是否存在指定key的对象
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key)
	{
		return concurrentHashMap.containsKey(key);
	}
	
	/**
	 * 移除
	 * @param cacheTableRow 行
	 * @return
	 */
	public boolean remove(ICacheTableRow cacheTableRow)
	{
		lock.writeLock().lock();
		try
		{
			if(cacheTableRow!=null)
			{
				rows.remove(cacheTableRow);
				for(ICacheTableStructure cacheTableStructure : concurrentHashMap.values())
				{
					if (cacheTableStructure != null)
					{
						cacheTableStructure.remove(cacheTableRow);
					}
				}
				return true;
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableStructure.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
		return false;
	}

//	/**
//	 * 取得列表
//	 * @return
//	 */
//	@SuppressWarnings("unchecked")
//	public <T> Collection<T> values()
//	{
//		lock.readLock().lock();
//		try
//		{
//			return (Collection<T>) concurrentHashMap.values();
//		}
//		catch (Exception e)
//		{
//			Logger.getLogger(CacheTableStructure.class.getName()).log(Level.SEVERE, null, e);
//			return null;
//		}
//		finally
//		{
//			lock.readLock().unlock();
//		}
//	}

	/**
	 * 大小
	 * @return
	 */
	public int size()
	{
		return rows.size();
	}

	public synchronized void dispose()
	{
		if (concurrentHashMap != null)
		{
			concurrentHashMap.clear();
			concurrentHashMap = null;
		}
		if(rows!=null)
		{
			rows.clear();
			rows=null;
		}
		parent = null;
	}
}
