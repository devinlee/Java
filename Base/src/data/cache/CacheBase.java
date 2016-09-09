package base.data.cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Devin
 *缓存类
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class CacheBase<K, V> implements ICache<K, V>
{
	/**
	 * 缓存名称
	 */
	private String name;
	/**
	 * 装载缓存的HashMap
	 */
	private ConcurrentHashMap<K, V> caches = null;

	/**
	 * 装载缓存的HashMap
	 * @return 数据集
	 */
	public ConcurrentHashMap<K, V> getCaches()
	{
		return caches;
	}

	/**
	 * 线程读写锁
	 */
	private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

	/**
	 * 缓存类
	 */
	public CacheBase()
	{
	}
	
	/**
	 * 缓存类
	 * @param name 缓存名
	 */
	public void init(String name)
	{
		this.name = name;
		caches = new ConcurrentHashMap<K, V>();
	}

	/**
	 * 缓存类
	 * @param name 缓存名
	 * @param initialCapacity 初始容量
	 */
	public void init(String name, int initialCapacity)
	{
		this.name = name;
		caches = new ConcurrentHashMap<K, V>(initialCapacity);
	}

	@Override
	public String getName()
	{
		lock.readLock().lock();
		try
		{
			return name;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheBase.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public V get(K key)
	{
		try
		{
			return caches.get(key);
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheBase.class.getName()).log(Level.SEVERE, "取得CacheBase错误。", e);
			return null;
		}
	}

	@Override
	public boolean add(K key, V value)
	{
		try
		{
			caches.put(key, value);
			return true;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheBase.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
	}

	@Override
	public boolean addAll(Map<? extends K, ? extends V> items)
	{
		caches.putAll(items);
		return true;
	}

	@Override
	public boolean remove(K key)
	{
		if (key == null)
			return false;
		caches.remove(key);
		return true;
	}

	@Override
	public boolean removeAll(Iterator<? extends K> keys)
	{
		lock.writeLock().lock();
		try
		{
			while (keys.hasNext())
			{
				K k = keys.next();
				if (containsKey(k))
				{
					caches.remove(k);
				}
			}
			return true;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheBase.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean removeAll()
	{
		return clear();
	}

	@Override
	public boolean containsKey(K key)
	{
		if (key == null)
			return false;
		return caches.containsKey(key);
	}

	@Override
	public boolean isEmpty()
	{
		return caches.isEmpty();
	}

	@Override
	public int size()
	{
		return caches.size();
	}
	
	public Collection<V> values()
	{
		return caches.values();
	}

	@Override
	public synchronized boolean clear()
	{
		caches.clear();
		return true;
	}

	@Override
	public synchronized void dispose()
	{
		if (caches != null)
		{
			caches.clear();
			caches = null;
		}
	}
}
