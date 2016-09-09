package base.data.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Devin
 *缓存接口
 * @param <K> key对象类型
 * @param <V> value对象类型
 */
public interface ICache<K, V>
{
	/**
	 * 缓存类
	 * @param name 缓存名
	 */
	public void init(String name);

	/**
	 * 缓存类
	 * @param name 缓存名
	 * @param initialCapacity 初始容量
	 */
	public void init(String name, int initialCapacity);
	
	/**
	 * 装载缓存的HashMap
	 * @return 数据集
	 */
	public ConcurrentHashMap<K, V> getCaches();
	
	/**
	 * 取得缓存名
	 */
	public String getName();

	/**
	 * 根据key取得value
	 * @param key 键
	 * @return V 值
	 */
	public V get(K key);

	/**
	 * 增加项
	 * @param key 键
	 * @param vaule 值
	 * @return boolean 是否增加成功
	 */
	public boolean add(K key, V value);

	/**
	 * 批量增加项
	 * @param items 项集
	 * @return boolean 是否增加成功
	 */
	public boolean addAll(Map<? extends K, ? extends V> items);

	/**
	 * @param key 键
	 * @return boolean 是否移除成功
	 */
	public boolean remove(K key);

	/**
	 * 批量移除指定key的项
	 * @param keys 键集
	 * @return boolean 是否移除成功
	 */
	public boolean removeAll(Iterator<? extends K> keys);

	/**
	 * 移除所有项
	 * @return boolean 是否移除成功
	 */
	public boolean removeAll();

	/**
	 * 是否存在指定的键
	 * @param key 键
	 * @return boolean
	 */
	public boolean containsKey(K key);

	/**
	 * 是否为空
	 * @return boolean 是否为空
	 */
	public boolean isEmpty();

	/**
	 * 大小
	 * @return
	 */
	public int size();

	/**
	 * 清除缓存
	 */
	public boolean clear();

	/**
	 * 资源清理
	 */
	public void dispose();
}
