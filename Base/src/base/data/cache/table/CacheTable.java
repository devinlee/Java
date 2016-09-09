package base.data.cache.table;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.Base;
import base.data.DataFactory;
import base.data.cache.CacheEvent;
import base.event.Event;
import base.event.IEventListener;
import base.types.SortType;
import base.util.DataUtil;
import base.util.DateUtil;
import base.util.StringUtil;

/**
 * 缓存表
 * @author Devin
 *
 */
public class CacheTable implements IEventListener, Cloneable, ICacheTable
{
	/**
	 * 线程读写锁
	 */
	protected final ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * 表名
	 */
	protected String name;

	/**
	 * 表名
	 * @return name
	 */
	public String getName()
	{
		lock.readLock().lock();
		try
		{
			return name;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 主键字段名(数据表中必须要将此字段设置为主键，不然会数据存储出错)
	 */
	protected String primaryKeyFieldName=null;

	/**
	 * 集合键字段名列表组
	 */
	protected String[][] keyFieldNameGroups;

	/**
	 * 集合键字段名列表组
	 * @return keyFieldNames
	 */
	public String[][] getKeyFieldNameGroups()
	{
		lock.readLock().lock();
		try
		{
			return this.keyFieldNameGroups;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 字段名称列表，该列表是表中所出现的所有的字段名
	 */
	protected Vector<String> fieldNames = new Vector<String>();

	/**
	 * 字段名称列表，该列表是表中所出现的所有的字段名
	 */
	public Vector<String> fieldNames()
	{
		lock.readLock().lock();
		try
		{
			return this.fieldNames;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 数据行记录容量的大小,-1则为不限
	 */
	protected int capacity;

	/**
	 * 表行数据列表
	 */
	protected Vector<ICacheTableRow> rows = new Vector<ICacheTableRow>();

	/**
	 * 表行数据列表
	 */
	protected ConcurrentHashMap<String, ICacheTableRow> rowPrimarys=null;

	/**
	 * 已更改过的表行数据列表
	 */
	protected Vector<ICacheTableRow> changeRows = new Vector<ICacheTableRow>();

	/**
	 * 当前CacheTable的指定字段的新CacheTable集
	 */
	private ConcurrentHashMap<String, ICacheTable> newCacheTables = new ConcurrentHashMap<String, ICacheTable>();

	/**
	 * 当前已过期的数据
	 */
	private Vector<ICacheTableRow> expiresCacheTableRows=new Vector<ICacheTableRow>();

	/**
	 * 按照集合键字段归类的结构集
	 */
	private ConcurrentHashMap<String, ICacheTableStructure> cacheTableStructures = new ConcurrentHashMap<String, ICacheTableStructure>();

	/**
	 * 头部热端
	 */
	protected ICacheTableRow headCacheTableRow;

	/**
	 * 尾部冷端
	 */
	protected ICacheTableRow endCacheTableRow;

	/**
	 * 缓存表
	 */
	public CacheTable()
	{
	}

	/**
	 * 缓存表
	 * @param name 表名
	 * @param primaryKeyFieldName 表主键字段名
	 * @param keyFieldNameGroups 集合键字段名集列表组，每一组值将做为行数据key，如果指定多个字段，中间以“_”连接，最终数据为：[0]字段值_[1]字段值_...
	 */
	public void init(String name, String primaryKeyFieldName, String[][] keyFieldNameGroups)
	{
		init(name, primaryKeyFieldName, keyFieldNameGroups, -1);
	}

	/**
	 * 缓存表
	 * @param name 表名
	 * @param primaryKeyFieldName 表主键字段名
	 * @param keyFieldNameGroups 集合键字段名集列表组，每一组值将做为行数据key，如果指定多个字段，中间以“_”连接，最终数据为：[0]字段值_[1]字段值_...
	 * @param capacity 数据行记录容量的大小，使用LRU算法淘汰机制，值为-1时则表示不限容量
	 */
	public void init(String name, String primaryKeyFieldName, String[][] keyFieldNameGroups, int capacity)
	{
		this.name = name;
		this.primaryKeyFieldName=primaryKeyFieldName;
		this.keyFieldNameGroups = keyFieldNameGroups;
		this.capacity = capacity;
		this.headCacheTableRow = DataUtil.cacheTableRow();
		this.endCacheTableRow = this.headCacheTableRow;

		if(primaryKeyFieldName!=null)
		{
			rowPrimarys=new ConcurrentHashMap<String, ICacheTableRow>();
		}

		if(keyFieldNameGroups!=null && keyFieldNameGroups.length>0)
		{
			for(String[] keyFieldNames : keyFieldNameGroups)
			{
				String strKey = getKeyFieldNameKey(keyFieldNames);
				ICacheTableStructure cacheTableStructure = Base.newClass(CacheTableStructure.class);
				cacheTableStructures.put(strKey, cacheTableStructure);
			}
		}
	}

	/**
	 * 增加表行
	 * @param cacheTableRow 行数据，值不允许为空
	 * @return 增加成功返回true，否则返回false
	 */
	public boolean addRow(ICacheTableRow cacheTableRow)
	{
		return addRow(cacheTableRow, -1);
	}

	/**
	 * 增加表行
	 * @param cacheTableRow 行数据，值不允许为空
	 * @param validTime 数据有效时间(毫秒),值<0时则表示不限
	 * @return 增加成功返回true，否则返回false
	 */
	public boolean addRow(ICacheTableRow cacheTableRow, long validTime)
	{
		lock.writeLock().lock();
		try
		{
			if (cacheTableRow == null)
				return false;

			String primaryKeyFieldValue=null;
			if(rowPrimarys!=null)
			{
				if(!StringUtil.isEmptyOrNull(primaryKeyFieldName))
				{
					if(cacheTableRow.containsField(primaryKeyFieldName))
					{
						primaryKeyFieldValue = cacheTableRow.getFieldData(primaryKeyFieldName).toString();
						if(rowPrimarys.containsKey(primaryKeyFieldValue))
						{
							Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception(this.name +" 缓存表在增加行数据时，遇到相同主键值的行："+primaryKeyFieldValue));
							return false;
						}
					}
					else
					{
						Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception(this.name +" 缓存表在增加行数据时，遇到行数据未指定表主键字段 "+primaryKeyFieldName+" 及值。"));
						return false;
					}
				}
			}

			if(keyFieldNameGroups!=null && keyFieldNameGroups.length>0)
			{
				for(String[] keyFieldNames : keyFieldNameGroups)
				{
					if(keyFieldNames!=null && keyFieldNames.length>0)
					{
						String strKey = getKeyFieldNameKey(keyFieldNames);
						if(cacheTableStructures.containsKey(strKey))
						{
							ICacheTableStructure rootCacheTableStructure = cacheTableStructures.get(strKey);
							for(String keyFieldName : keyFieldNames)
							{
								if(cacheTableRow.containsField(keyFieldName))
								{
									String keyFieldValue = cacheTableRow.getFieldData(keyFieldName).toString();
									if (!rootCacheTableStructure.containsKey(keyFieldValue))
									{
										ICacheTableStructure cacheTableStructure = Base.newClass(CacheTableStructure.class);
										cacheTableStructure.init(rootCacheTableStructure, keyFieldValue);
										rootCacheTableStructure.put(keyFieldValue, cacheTableStructure);
									}
									rootCacheTableStructure = rootCacheTableStructure.get(keyFieldValue);
									rootCacheTableStructure.put(cacheTableRow);
								}
								else
								{
									Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception(this.name +" 缓存表在增加行数据时，遇到行数据未指定表集合键字段 "+keyFieldName+" 及值。"));
									return false;
								}
							}
						}
						else
						{
							Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception(this.name +" 缓存表在增加行数据时，遇到不存在的集合键："+strKey));
						}
					}
				}
			}

			if (validTime < 0)
			{
				validTime = Long.MAX_VALUE;// 如果未指定有效时间，则指定一个最大时间
			}
			else
			{
				validTime = DateUtil.getCurrentTimeMillis() + validTime;
			}
			cacheTableRow.setExpiresTime(validTime);
			if (capacity>-1 && rows.size() >= capacity)
			{// 如果当前容量已经达到最大，则进行尾部冷端淘汰
				ICacheTableRow endRow = endCacheTableRow.getPrevious();
				removeRow(endRow);
			}
			cacheTableRow.addEventListener(CacheEvent.ROW_FIELD_DATA_CHANGE, this);
			cacheTableRow.addEventListener(CacheEvent.DATA_TO_SAVE, this);
			rows.add(cacheTableRow);
			if(rowPrimarys!=null)
			{
				//				if(cacheTableRow.containsField(primaryKeyFieldName) && cacheTableRow.getField(primaryKeyFieldName).getIsSqlSave())
				//				{
				rowPrimarys.put(primaryKeyFieldValue, cacheTableRow);
				//				}
			}

			// 进行字段名收集
			ConcurrentHashMap<String, ICacheTableField> cacheTableRowFields = cacheTableRow.getFields();
			Enumeration<String> cacheTableRowFieldsKeys = cacheTableRowFields.keys();
			while (cacheTableRowFieldsKeys.hasMoreElements())
			{
				String fieldName = cacheTableRowFieldsKeys.nextElement();
				if (!this.fieldNames.contains(fieldName))
				{// 如果不包含，说明是一个新的字段，则进行加入该表字段集
					this.fieldNames.add(fieldName);
				}
			}

			insertHead(cacheTableRow);
			newCacheTableAddRow(cacheTableRow);// 如果存在当前CacheTable的指定字段的新CacheTable，则在增加新的行时，需要往新CacheTable中执行增加，用以同步数据
			return true;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 插入表行
	 * @param cacheTableRow 行数据，值不允许为空
	 * @param index 表行索引，值不能小于0
	 * @return 增加成功返回true，否则返回false
	 */
	public boolean insertRow(ICacheTableRow cacheTableRow, int index)
	{
		return insertRow(cacheTableRow, index, -1);
	}

	/**
	 * 插入表行
	 * @param cacheTableRow 行数据，值不允许为空
	 * @param index 表行索引，值不能小于0
	 * @param validTime 数据有效时间(毫秒),值<0时则表示不限
	 * @return 增加成功返回true，否则返回false
	 */
	public boolean insertRow(ICacheTableRow cacheTableRow, int index, long validTime)
	{
		lock.writeLock().lock();
		try
		{
			if (cacheTableRow == null)
				return false;
			String primaryKeyFieldValue=null;
			if(rowPrimarys!=null)
			{
				//				if(cacheTableRow.containsField(primaryKeyFieldName) && cacheTableRow.getField(primaryKeyFieldName).getIsSqlSave())
				//				{
				primaryKeyFieldValue = cacheTableRow.getFieldData(primaryKeyFieldName).toString();
				if(rowPrimarys.containsKey(primaryKeyFieldValue))
				{
					Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception(this.name +" 缓存表在插入行数据时，遇到相同主键值的行："+primaryKeyFieldValue));
					return false;
				}
				//				}
			}

			if(keyFieldNameGroups!=null && keyFieldNameGroups.length>0)
			{
				for(String[] keyFieldNames : keyFieldNameGroups)
				{
					if(keyFieldNames!=null && keyFieldNames.length>0)
					{
						ICacheTableStructure rootCacheTableStructure = cacheTableStructures.get(getKeyFieldNameKey(keyFieldNames));
						for(String keyFieldName : keyFieldNames)
						{
							String keyFieldValue = cacheTableRow.getFieldData(keyFieldName).toString();
							if (!rootCacheTableStructure.containsKey(keyFieldValue))
							{
								ICacheTableStructure cacheTableStructure = Base.newClass(CacheTableStructure.class);
								cacheTableStructure.init(rootCacheTableStructure, keyFieldValue);
								rootCacheTableStructure.put(keyFieldValue, cacheTableStructure);
							}
							rootCacheTableStructure = rootCacheTableStructure.get(keyFieldValue);
							rootCacheTableStructure.insert(cacheTableRow, index);
						}
					}
				}
			}

			if (validTime < 0)
			{
				validTime = Long.MAX_VALUE;// 如果未指定有效时间，则指定一个最大时间
			}
			else
			{
				validTime = DateUtil.getCurrentTimeMillis() + validTime;
			}
			cacheTableRow.setExpiresTime(validTime);
			if (capacity>-1 && rows.size() >= capacity)
			{// 如果当前容量已经达到最大，则进行尾部冷端淘汰
				ICacheTableRow endRow = endCacheTableRow.getPrevious();
				removeRow(endRow);
			}
			cacheTableRow.addEventListener(CacheEvent.ROW_FIELD_DATA_CHANGE, this);
			cacheTableRow.addEventListener(CacheEvent.DATA_TO_SAVE, this);
			rows.insertElementAt(cacheTableRow, index);
			if(rowPrimarys!=null)
			{
				//				if(cacheTableRow.containsField(primaryKeyFieldName) && cacheTableRow.getField(primaryKeyFieldName).getIsSqlSave())
				//				{
				rowPrimarys.put(primaryKeyFieldValue, cacheTableRow);
				//				}
			}

			// 进行字段名收集
			ConcurrentHashMap<String, ICacheTableField> cacheTableRowFields = cacheTableRow.getFields();
			Enumeration<String> cacheTableRowFieldsKeys = cacheTableRowFields.keys();
			while (cacheTableRowFieldsKeys.hasMoreElements())
			{
				String fieldName = cacheTableRowFieldsKeys.nextElement();
				if (!this.fieldNames.contains(fieldName))
				{// 如果不包含，说明是一个新的字段，则进行加入该表字段集
					this.fieldNames.add(fieldName);
				}
			}

			insertHead(cacheTableRow);
			newCacheTableInsertRow(cacheTableRow, index);// 如果存在当前CacheTable的指定字段的新CacheTable，则在增加新的行时，需要往新CacheTable中执行插入，用以同步数据
			return true;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 取得所属对应键值列表行列表中的第一个行数据
	 * @param keyFieldNames 集合键字段名列表
	 * @param keyFieldValues 集合键值列表
	 * @return 取得的行数据
	 */
	public ICacheTableRow getRow(String[] keyFieldNames, String[] keyFieldValues)
	{
		lock.readLock().lock();
		try
		{
			Vector<ICacheTableRow> cacheTableRows =getRows(keyFieldNames, keyFieldValues);
			if (cacheTableRows != null && cacheTableRows.size()>0)
			{
				for(ICacheTableRow cacheTableRow : cacheTableRows)
				{
					if (DateUtil.getCurrentTimeMillis() > cacheTableRow.getExpiresTime())
					{// 如果当前行数据已过期
						expiresCacheTableRows.add(cacheTableRow);
					}
					else
					{
						moveToHead(cacheTableRow);
						removeAllExpiresRows();
						return cacheTableRow;
					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.readLock().unlock();
		}
		return null;
	}

	/**
	 * 行字段数据交换
	 * @param cacheTableRow1 行1
	 * @param cacheTableRow2 行2
	 */
	public void exchangeRow(ICacheTableRow cacheTableRow1, ICacheTableRow cacheTableRow2)
	{
		exchangeRow(cacheTableRow1, cacheTableRow2, null);
	}

	/**
	 * 行字段数据交换
	 * @param cacheTableRow1 行1
	 * @param cacheTableRow2 行2
	 * @param excludeFields 要排除的字段
	 */
	public void exchangeRow(ICacheTableRow cacheTableRow1, ICacheTableRow cacheTableRow2, String[] excludeFields)
	{
		if(cacheTableRow1==null || cacheTableRow2==null)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception("缓存表在进行行字段数据交换时，遇到指定的行为空行。"));
			return;
		}

		if(cacheTableRow1.getFields().size()!=cacheTableRow2.getFields().size())
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception("缓存表在进行行字段数据交换时，遇到行字段数量互相不匹配。"));
			return;
		}

		for(ICacheTableField cacheTableField1 : cacheTableRow1.getFields().values())
		{
			if(excludeFields!=null && excludeFields.length>0)
			{
				boolean isExist=false;
				for(String exFieldName : excludeFields)
				{
					if(exFieldName.equals(cacheTableField1.getFieldName()))
					{//如果存在于排除字段中
						isExist=true;
						break;
					}
				}
				if(isExist)
				{
					continue;
				}
			}

			if(cacheTableRow2.containsField(cacheTableField1.getFieldName()))
			{
				ICacheTableField cacheTableField2 = cacheTableRow2.getField(cacheTableField1.getFieldName());
				Object data1 = cacheTableField1.getData();
				Object data2 = cacheTableField2.getData();
				cacheTableField1.setData(data2);
				cacheTableField2.setData(data1);
			}
			else
			{
				Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception("缓存表在进行行字段数据交换时，遇到行字段互相不匹配。"));
				break;
			}
			
//			if(keyFieldNameGroups!=null && keyFieldNameGroups.length>0)
//			{
//				for(String[] keyFieldNames : keyFieldNameGroups)
//				{
//					if(keyFieldNames!=null && keyFieldNames.length>0)
//					{
//						String strKey = getKeyFieldNameKey(keyFieldNames);
//						if(cacheTableStructures.containsKey(strKey))
//						{
//							ICacheTableStructure rootCacheTableStructure = cacheTableStructures.get(strKey);
//							for(String keyFieldName : keyFieldNames)
//							{
//								if(cacheTableRow.containsField(keyFieldName))
//								{
//									String keyFieldValue = cacheTableRow.getFieldData(keyFieldName).toString();
//									if (!rootCacheTableStructure.containsKey(keyFieldValue))
//									{
//										ICacheTableStructure cacheTableStructure = Base.newClass(CacheTableStructure.class);
//										cacheTableStructure.init(rootCacheTableStructure, keyFieldValue);
//										rootCacheTableStructure.put(keyFieldValue, cacheTableStructure);
//									}
//									rootCacheTableStructure = rootCacheTableStructure.get(keyFieldValue);
//									rootCacheTableStructure.put(cacheTableRow);
//								}
//								else
//								{
//									Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception(this.name +" 缓存表在增加行数据时，遇到行数据未指定表集合键字段 "+keyFieldName+" 及值。"));
//									return false;
//								}
//							}
//						}
//						else
//						{
//							Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception(this.name +" 缓存表在增加行数据时，遇到不存在的集合键："+strKey));
//						}
//					}
//				}
//			}
		}
	}

	/**
	 * 取得所有行数据
	 * @return 取得的行数据
	 */
	public Vector<ICacheTableRow> getRows()
	{
		lock.readLock().lock();
		try
		{
			return rows;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.readLock().unlock();
		}
		return null;
	}

	/**
	 * 取得符合键值列表的数据列表
	 * @param keyFieldNames 集合键字段名列表
	 * @param keyFieldValues 集合键值列表
	 * @return 取得的行数据列表
	 */
	public Vector<ICacheTableRow> getRows(String[] keyFieldNames, String[] keyFieldValues)
	{
		return getRows(getKeyFieldNameKey(keyFieldNames), keyFieldValues);
	}

	/**
	 * 取得符合键值列表的数据列表
	 * @param keyFieldNameKey 集合键字段名组合key
	 * @param keyFieldValues 集合键值列表
	 * @return 取得的行数据列表
	 */
	private Vector<ICacheTableRow> getRows(String keyFieldNameKey, String[] keyFieldValues)
	{
		lock.readLock().lock();
		try
		{
			if(keyFieldNameKey==null || keyFieldValues==null || keyFieldValues.length<=0)return null;
			ICacheTableStructure rootCacheTableStructure = cacheTableStructures.get(keyFieldNameKey);
			if(rootCacheTableStructure==null)return null;

			for (int i = 0; i < keyFieldValues.length; i++)
			{
				rootCacheTableStructure = rootCacheTableStructure.get(keyFieldValues[i]);
				if(rootCacheTableStructure==null)
				{
					return null;
				}
			}
			Vector<ICacheTableRow> cacheTableRows = rootCacheTableStructure.getRows();
			if (cacheTableRows != null && cacheTableRows.size()>0)
			{
				for(ICacheTableRow cacheTableRow : cacheTableRows)
				{
					if (DateUtil.getCurrentTimeMillis() > cacheTableRow.getExpiresTime())
					{// 如果当前行数据已过期
						expiresCacheTableRows.add(cacheTableRow);
					}
				}
			}
			removeAllExpiresRows();
			return cacheTableRows;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.readLock().unlock();
		}
		return null;
	}

	/**
	 * 取得行
	 * @param rowIndex 行索引
	 * @return 取得的行数据，如果提供的索引超出范围，则返回null
	 */
	public ICacheTableRow getRow(int rowIndex)
	{
		lock.readLock().lock();
		try
		{
			if (rows.size() < rowIndex + 1)
			{
				return null;
			}
			ICacheTableRow cacheTableRow = rows.get(rowIndex);
			if (DateUtil.getCurrentTimeMillis() > cacheTableRow.getExpiresTime())
			{// 如果当前行数据已过期
				expiresCacheTableRows.add(cacheTableRow);
			}
			else
			{
				moveToHead(cacheTableRow);
				removeAllExpiresRows();
				return cacheTableRow;
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		finally
		{
			lock.readLock().unlock();
		}
		return null;
	}

	/**
	 * 移除表行
	 * @param rowIndex 行索引
	 * @return 移除成功或者指定的索引不存于列表返回true,否则返回false
	 */
	public boolean removeRow(int rowIndex)
	{
		lock.writeLock().lock();
		try
		{
			if (rows.size() < rowIndex + 1)
			{
				return true;
			}
			removeRow(rows.get(rowIndex));
			return true;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 移除行数据
	 * @param cacheTableRow 行数据
	 * @return 移除成功或者指定的行数据为空返回true,否则返回false
	 */
	public boolean removeRow(ICacheTableRow cacheTableRow)
	{
		lock.writeLock().lock();
		try
		{
			if (cacheTableRow == null)
				return true;
			if(rowPrimarys!=null)
			{
				//				if(cacheTableRow.containsField(primaryKeyFieldName) && cacheTableRow.getField(primaryKeyFieldName).getIsSqlSave())
				//				{
				String primaryKeyFieldValue = cacheTableRow.getFieldData(primaryKeyFieldName).toString();
				rowPrimarys.remove(primaryKeyFieldValue);
				//				}
			}
			cacheTableRow.removeEventListener(CacheEvent.ROW_FIELD_DATA_CHANGE, this);
			cacheTableRow.removeEventListener(CacheEvent.DATA_TO_SAVE, this);
			rows.remove(cacheTableRow);
			changeRows.remove(cacheTableRow);
			expiresCacheTableRows.remove(cacheTableRow);

			newCacheTableRemoveRow(cacheTableRow);// 向当前CacheTable的指定字段的新CacheTable中移除行数据
			if(cacheTableStructures!=null && cacheTableStructures.size()>0)
			{
				for(ICacheTableStructure cacheTableStructure : cacheTableStructures.values())
				{
					if (cacheTableStructure != null)
					{
						cacheTableStructure.remove(cacheTableRow);
						cacheTableStructureClear(cacheTableStructure);
					}
				}
			}

			if (cacheTableRow.getPrevious() != null)
				cacheTableRow.getPrevious().setNext(cacheTableRow.getNext());
			if (cacheTableRow.getNext() != null)
				cacheTableRow.getNext().setPrevious(cacheTableRow.getPrevious());
			cacheTableRow.dispose();
			cacheTableRow = null;
			return true;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 移除表所有行数据并释放行数据
	 * @return 移除成功返回true,否则返回false
	 */
	public boolean removeAll()
	{
		lock.writeLock().lock();
		try
		{
			if(rows!=null && rows.size()>0)
			{
				while(rows.size()>0)
				{
					ICacheTableRow cacheTableRow = rows.remove(0);
					removeRow(cacheTableRow);
					cacheTableRow=null;
				}
			}
			return true;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	private void cacheTableStructureClear(ICacheTableStructure cacheTableStructure)
	{
		if (cacheTableStructure == null)
			return;
		if (cacheTableStructure.size() <= 0)
		{
			ICacheTableStructure parentCacheTableStructure = cacheTableStructure.getParent();
			if (parentCacheTableStructure != null)
			{
				parentCacheTableStructure.remove(cacheTableStructure.getKey());
				cacheTableStructure.dispose();
				cacheTableStructure = null;
				cacheTableStructureClear(parentCacheTableStructure);
			}
		}
	}

	// private void removeRow(ConcurrentHashMap rowMap, CacheTableRow cacheTableRow)
	// {
	// rowMap.
	// Enumeration<String> keys = rowMap.keys();
	// while(keys.hasMoreElements())
	// {
	// String key = keys.nextElement();
	// Object obj = rowMap.get(key);
	// if(obj!=null)
	// {
	// if(obj.getClass().isAssignableFrom(ConcurrentHashMap.class))
	// {
	//
	// }
	// }
	// }
	// }

	/**
	 * 取得列表中首次出现的行索引
	 * @param keyFieldNames 集合键字段列表
	 * @param keyFieldValues 集合键值列表
	 * @return 行索引，如果指定的主键Key不存于列表中，则返回-1
	 */
	public int rowIndexOf(String[] keyFieldNames, String[] keyFieldValues)
	{
		lock.readLock().lock();
		try
		{
			Vector<ICacheTableRow> cacheTableRows =getRows(keyFieldNames, keyFieldValues);
			if (cacheTableRows != null && cacheTableRows.size()>0)
			{
				for(ICacheTableRow cacheTableRow : cacheTableRows)
				{
					if (DateUtil.getCurrentTimeMillis() > cacheTableRow.getExpiresTime())
					{// 如果当前行数据已过期
						expiresCacheTableRows.add(cacheTableRow);
					}
					else
					{
						moveToHead(cacheTableRow);
						removeAllExpiresRows();
						return rows.indexOf(cacheTableRow);
					}
				}
			}
			return -1;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
			return -1;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 取得列表中最后一次出现的行索引
	 * @param keyFieldNames 集合键字段列表
	 * @param keyFieldValues 集合键值列表
	 * @return 行索引，如果指定的主键Key不存于列表中，则返回-1
	 */
	public int rowLastIndexOf(String[] keyFieldNames, String[] keyFieldValues)
	{
		lock.readLock().lock();
		try
		{
			Vector<ICacheTableRow> cacheTableRows =getRows(keyFieldNames, keyFieldValues);
			if (cacheTableRows != null && cacheTableRows.size()>0)
			{
				for(ICacheTableRow cacheTableRow : cacheTableRows)
				{
					if (DateUtil.getCurrentTimeMillis() > cacheTableRow.getExpiresTime())
					{// 如果当前行数据已过期
						expiresCacheTableRows.add(cacheTableRow);
					}
					else
					{
						moveToHead(cacheTableRow);
						removeAllExpiresRows();
						return rows.lastIndexOf(cacheTableRow);
					}
				}
			}
			return -1;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
			return -1;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 检测是否包含有指定集合键值的行
	 * @param keyFieldNames 集合键字段列表
	 * @param keyFieldValues 集合键值列表
	 * @return 如果存在返回true, 否则返回false
	 */
	public boolean containsKeyField(String[] keyFieldNames, String[] keyFieldValues)
	{
		boolean result = false;
		try
		{
			Vector<ICacheTableRow> cacheTableRows = getRows(keyFieldNames, keyFieldValues);
			if(cacheTableRows!=null && cacheTableRows.size()>0)
			{
				result = true;
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
		}
		return result;
	}

	/**
	 * 插入到头部热端
	 * @param cacheTableRow CacheTableRow
	 */
	private void insertHead(ICacheTableRow cacheTableRow)
	{
		cacheTableRow.setPrevious(headCacheTableRow);
		cacheTableRow.setNext(headCacheTableRow.getNext());
		if (headCacheTableRow.getNext() != null)
			headCacheTableRow.getNext().setPrevious(cacheTableRow);
		headCacheTableRow.setNext(cacheTableRow);
	}

	/**
	 * 移动到头部热端
	 * @param cacheTableRow CacheTableRow
	 */
	private void moveToHead(ICacheTableRow cacheTableRow)
	{
		if (cacheTableRow.getPrevious() != null)
			cacheTableRow.getPrevious().setNext(cacheTableRow.getNext());
		if (cacheTableRow.getNext() != null)
			cacheTableRow.getNext().setPrevious(cacheTableRow.getPrevious());
		cacheTableRow.setNext(headCacheTableRow);
		cacheTableRow.setPrevious(null);
	}

	/**
	 * 移除所有已过期的数据行
	 */
	private void removeAllExpiresRows()
	{
		lock.readLock().lock();
		try
		{
			if(expiresCacheTableRows!=null && expiresCacheTableRows.size()>0)
			{
				while(expiresCacheTableRows.size()>0)
				{
					lock.readLock().unlock();
					removeRow(expiresCacheTableRows.remove(0));
					lock.readLock().lock();
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 排序表行数据
	 * @param fieldNames 排序字段，
	 * 格式：new Object[]{数据值类型(JavaType.xxx),字段名,数据值类型(JavaType.xxx),字段名,...}，需其数组长度应与options长度相等
	 * 说明：fieldName长度其[数据值类型(JavaType.xxx),字段名]计算为1个长度
	 * @param options 排序规则，
	 * 格式：new SortType[]{SortType.asc,SortType.desc,SortType.asc,SortType.desc,...}，需其数组长度应与fieldName长度相等，如不相等，其不相等部分将默认为SortType.asc。
	 * 说明：fieldName长度其[数据值类型(JavaType.xxx),字段名]计算为1个长度
	 * @示例
	 * cacheTable.sortOn(new Object[]{JavaType.intType, "id", JavaType.intType, "number"}, new SortType[]{SortType.asc, SortType.desc});
	 */
	public void sortOn(final Object[] fieldNames, final SortType[] options)
	{
		lock.writeLock().lock();
		try
		{
			CacheTableComparator sortOnComparator = new CacheTableComparator(name, fieldNames, options);
			Collections.sort(rows, sortOnComparator);
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 排序表行数据
	 * @param comparator 排序比较函数
	 */
	public void sort(Comparator<? super ICacheTableRow> comparator)
	{
		lock.writeLock().lock();
		try
		{
			Collections.sort(rows, comparator);
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 表行大小
	 * @return 表行大小
	 */
	public int getRowSize()
	{
		lock.readLock().lock();
		try
		{
			return rows.size();
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
			return 0;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 创建当前CacheTable的指定字段的新CacheTable，新CacheTable的数据将会自动同步更新于当前CacheTable，其同步项为当前CacheTable增加新行、移除行以及行字段的内容数据。
	 * @param name CacheTable表名
	 * @param primaryKeyFieldName 表主键字段名
	 * @param keyFieldNameGroups 集合键字段名列表组，此值将做为行数据key，如果指定多个字段，中间以“_”连接，最终数据为：[0]字段值_[1]字段值_…，值不允许为空
	 * @param fieldNames 指定字段名列表，字段需存在于当前的CacheTable中，如：new String[]{"ID", "Name", ...}
	 * @return 创建成功返回新创建的CacheTable，否则返回null
	 */
	public ICacheTable newCacheTable(String name, String primaryKeyFieldName, String[][] keyFieldNameGroups, String[] fieldNames)
	{
		lock.writeLock().lock();
		ICacheTable newCacheTable = null;
		try
		{
			if (newCacheTables.containsKey(name))
			{
				Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception("在已有的CacheTable新创建指定字段的CacheTable时出现已有相同名称错误，错误CacheTable：" + this.name + "，新建CacheTable：" + name));
				return null;
			}

			newCacheTable = DataFactory.cacheController().createCacheTable(name, primaryKeyFieldName, keyFieldNameGroups);
			for (ICacheTableRow cacheTableRow : rows)
			{
				ICacheTableRow newCacheTableRow = DataUtil.cacheTableRow();
				for (String fieldName : fieldNames)
				{
					if (cacheTableRow.containsField(fieldName))
					{
						newCacheTableRow.addField(fieldName, cacheTableRow.getField(fieldName));
					}
					else
					{
						Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception("在已有的CacheTable新创建指定字段的CacheTable时出现行不存在相应字段名的错误，错误CacheTable：" + this.name + "，新建CacheTable：" + name + "，字段名：" + fieldName));
						return null;
					}
				}
				newCacheTable.addRow(newCacheTableRow);
			}
			newCacheTables.put(name, newCacheTable);
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
			newCacheTable = null;
		}
		finally
		{
			lock.writeLock().unlock();
		}
		return newCacheTable;
	}

	/**
	 * 向当前CacheTable的指定字段的新CacheTable中加入行数据
	 * @param cacheTableRow
	 */
	private void newCacheTableAddRow(ICacheTableRow cacheTableRow)
	{
		try
		{
			if (newCacheTables.size() > 0)
			{// 如果存在当前CacheTable的指定字段的新CacheTable，则在增加新的行时，需要往新CacheTable中执行增加，用以同步数据
				Collection<ICacheTable> cacheTables = newCacheTables.values();
				for (ICacheTable cacheTable : cacheTables)
				{
					ICacheTableRow newCacheTableRow = DataUtil.cacheTableRow();
					for (String fieldName : cacheTable.fieldNames())
					{
						if (cacheTableRow.containsField(fieldName))
						{
							newCacheTableRow.addField(fieldName, cacheTableRow.getField(fieldName));
						}
						else
						{
							Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception("在已有的CacheTable新创建指定字段的CacheTable中，加入新行时出现行不存在相应字段名的错误，错误CacheTable：" + this.name + "，新建CacheTable：" + cacheTable.getName() + "，字段名：" + fieldName));
						}
					}
					cacheTable.addRow(newCacheTableRow);
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 向当前CacheTable的指定字段的新CacheTable中插入行数据
	 * @param cacheTableRow
	 * @param index 插入的索引
	 */
	private void newCacheTableInsertRow(ICacheTableRow cacheTableRow, int index)
	{
		try
		{
			if (newCacheTables.size() > 0)
			{// 如果存在当前CacheTable的指定字段的新CacheTable，则在增加新的行时，需要往新CacheTable中执行增加，用以同步数据
				Collection<ICacheTable> cacheTables = newCacheTables.values();
				for (ICacheTable cacheTable : cacheTables)
				{
					ICacheTableRow newCacheTableRow = DataUtil.cacheTableRow();
					for (String fieldName : cacheTable.fieldNames())
					{
						if (cacheTableRow.containsField(fieldName))
						{
							newCacheTableRow.addField(fieldName, cacheTableRow.getField(fieldName));
						}
						else
						{
							Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception("在已有的CacheTable新创建指定字段的CacheTable中，加入新行时出现行不存在相应字段名的错误，错误CacheTable：" + this.name + "，新建CacheTable：" + cacheTable.getName() + "，字段名：" + fieldName));
						}
					}
					cacheTable.insertRow(newCacheTableRow, index);
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 向当前CacheTable的指定字段的新CacheTable中移除行数据
	 * @param cacheTableRow
	 */
	private void newCacheTableRemoveRow(ICacheTableRow cacheTableRow)
	{
		try
		{
			if (newCacheTables.size() > 0)
			{// 如果存在当前CacheTable的指定字段的新CacheTable，则在移除行时，需要往新CacheTable中执行移除，用以同步数据
				Collection<ICacheTable> cacheTables = newCacheTables.values();
				for (ICacheTable cacheTable : cacheTables)
				{
					cacheTable.removeRow(cacheTableRow);
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	private String getKeyFieldNameKey(String[] keyFieldNames)
	{
		try
		{
			if(keyFieldNames==null || keyFieldNames.length<=0)return null;
			String key="";
			for(String keyFieldName : keyFieldNames)
			{
				key+="_"+keyFieldName;
			}
			return key;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
		}
		return null;
	}

	/**
	 * 清除数据(不对行数据做释放处理)
	 */
	public void clear()
	{
		dispose(false);
	}

	public void handleEvent(Event event)
	{
	}

	/*
	 * 克隆，深度复制一份全新的数据
	 */
	public ICacheTable clone()
	{
		lock.readLock().lock();
		try
		{
			ICacheTable cloneCacheTable = Base.newClass(CacheTable.class);
			cloneCacheTable.init(name, primaryKeyFieldName, keyFieldNameGroups, capacity);
			for(ICacheTableRow cacheTableRow : rows)
			{
				ICacheTableRow cloneCacheTableRow = DataUtil.cacheTableRow();
				for(ICacheTableField cacheTableField : cacheTableRow.getFields().values())
				{
					ICacheTableField cloneCacheTableField = DataUtil.cacheTableField(cacheTableField.getData(), cacheTableField.getJavaType());
					cloneCacheTableField.setIsSqlSave(cacheTableField.getIsSqlSave());
					cloneCacheTableField.setIsChangeSaveFlag(cacheTableField.getIsChangeSaveFlag());
					cloneCacheTableRow.addField(cacheTableField.getFieldName(), cloneCacheTableField);
				}
				cloneCacheTable.addRow(cloneCacheTableRow);
			}
			return cloneCacheTable;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.readLock().unlock();
		}
		return  null;
	}

	public synchronized void dispose()
	{
		dispose(true);
	}

	public synchronized void dispose(boolean isDispose)
	{
		if(cacheTableStructures!=null)
		{
			for(ICacheTableStructure cacheTableStructure : cacheTableStructures.values())
			{
				cacheTableStructure.dispose();
				if(isDispose)
				{
					cacheTableStructure = null;
				}
			}
			cacheTableStructures.clear();
			if(isDispose)
			{
				cacheTableStructures=null;
			}
		}
		if (changeRows != null)
		{
			changeRows.clear();
			if(isDispose)
			{
				changeRows = null;
			}
		}
		if(rowPrimarys!=null)
		{
			rowPrimarys.clear();
			if(isDispose)
			{
				rowPrimarys=null;
			}
		}
		if (rows != null)
		{
			while (rows.size() > 0)
			{
				ICacheTableRow cacheTableRow = rows.remove(0);
				cacheTableRow.removeEventListener(CacheEvent.ROW_FIELD_DATA_CHANGE, this);
				cacheTableRow.removeEventListener(CacheEvent.DATA_TO_SAVE, this);
				if(isDispose)
				{
					cacheTableRow.dispose();
				}
				cacheTableRow = null;
			}
			rows.clear();
			if(isDispose)
			{
				rows = null;
			}
		}
		if(isDispose)
		{
			keyFieldNameGroups=null;
		}
	}
}