package base.thread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadPoolController
{
	/**
	 * 线程池集
	 */
	private ConcurrentHashMap<String, ExecutorService> pools = new ConcurrentHashMap<String, ExecutorService>();

	/**
	 * 创建可根据需要创建新线程的线程池
	 * @param name 线程池名称
	 * @return ExecutorService
	 */
	public ExecutorService create(String name)
	{
		if (name == null)
			return null;
		if (pools.containsKey(name))
		{// 如果已经存在同名称的线程池，不执行创建，返回null
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, new Exception("存在相同名称的线程池 " + name + "，无法创建，返回 null"));
			return null;
		}
		ExecutorService pool = Executors.newCachedThreadPool();
		pools.put(name, pool);
		return pool;
	}

	/**
	 * 创建可根据需要创建新线程的线程池
	 * @param name 线程池名称
	 * @param threadFactory ThreadFactory
	 * @return ExecutorService
	 */
	public ExecutorService create(String name, ThreadFactory threadFactory)
	{
		if (name == null)
			return null;
		if (pools.containsKey(name))
		{// 如果已经存在同名称的线程池，不执行创建，返回null
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, new Exception("存在相同名称的线程池 " + name + "，无法创建，返回 null"));
			return null;
		}
		ExecutorService pool = Executors.newCachedThreadPool(threadFactory);
		pools.put(name, pool);
		return pool;
	}

	/**
	 * 创建指定大小的线程池
	 * @param name 线程池名称
	 * @param poolSize 线程池大小
	 * @return ExecutorService
	 */
	public ExecutorService create(String name, int poolSize)
	{
		if (name == null)
			return null;
		if (pools.containsKey(name))
		{// 如果已经存在同名称的线程池，不执行创建，返回null
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, new Exception("存在相同名称的线程池 " + name + "，无法创建，返回 null"));
			return null;
		}
		ExecutorService pool = Executors.newFixedThreadPool(poolSize);
		pools.put(name, pool);
		return pool;
	}

	/**
	 * 创建指定大小的线程池
	 * @param name 线程池名称
	 * @param poolSize 线程池大小
	 * @param threadFactory ThreadFactory
	 * @return ExecutorService
	 */
	public ExecutorService create(String name, int poolSize, ThreadFactory threadFactory)
	{
		if (name == null || threadFactory == null)
			return null;
		if (pools.containsKey(name))
		{// 如果已经存在同名称的线程池，不执行创建，返回null
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, new Exception("存在相同名称的线程池 " + name + "，无法创建，返回 null"));
			return null;
		}
		ExecutorService _pool = Executors.newFixedThreadPool(poolSize, threadFactory);
		pools.put(name, _pool);
		return _pool;
	}

	/**
	 * 根据name取得一个线程池
	 * @param name 线程池名称
	 * @return ExecutorService
	 */
	public ExecutorService getPool(String name)
	{
		if (name == null)
			return null;
		return pools.get(name);
	}
}
