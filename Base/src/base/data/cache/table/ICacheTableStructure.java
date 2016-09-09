package base.data.cache.table;

import java.util.Vector;

public interface ICacheTableStructure
{
	/**
	 * 标识Key
	 * @return key
	 */
	public String getKey();

	/**
	 * @return parent
	 */
	public ICacheTableStructure getParent();

	/**
	 * @param parent 要设置的 parent
	 */
	public void setParent(ICacheTableStructure parent);
	/**
	 * 结构行数据列表
	 */
	public Vector<ICacheTableRow> getRows();

	/**
	 * 缓存表结构
	 * @param parent 上级结构
	 * @param key 标识key
	 */
	public void init(ICacheTableStructure parent, String key);

	/**
	 * 加入
	 * @param key 标识key
	 * @param cacheTableStructure 缓存表结构
	 */
	public void put(String key, ICacheTableStructure cacheTableStructure);
	
	/**
	 * 加入
	 * @param cacheTableRow 行
	 */
	public void put(ICacheTableRow cacheTableRow);
	
	/**
	 * 插入
	 * @param cacheTableRow 行
	 * @param cacheTableRow 要插入的索引位置
	 */
	public void insert(ICacheTableRow cacheTableRow, int index);

	/**
	 * 取得
	 * @param key
	 * @param value
	 */
	public ICacheTableStructure get(String key);

	/**
	 * 移除
	 * @param key
	 * @return
	 */
	public ICacheTableStructure remove(String key);

	/**
	 * 是否存在指定key的对象
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key);
	
	/**
	 * 移除
	 * @param cacheTableRow 行
	 * @return
	 */
	public boolean remove(ICacheTableRow cacheTableRow);

	/**
	 * 大小
	 * @return
	 */
	public int size();

	public void dispose();
}