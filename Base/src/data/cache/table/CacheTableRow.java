package base.data.cache.table;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.Base;
import base.data.cache.CacheEvent;
import base.event.EventDispatcher;
import base.types.JavaType;
import base.util.DataUtil;
import base.util.TypesUtil;

/**
 * 缓存表行
 * @author Devin
 *
 */
public class CacheTableRow extends EventDispatcher implements ICacheTableRow
{
	/**
	 * 线程读写锁
	 */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * 表行字段集
	 */
	private ConcurrentHashMap<String, ICacheTableField> fields = new ConcurrentHashMap<String, ICacheTableField>();

//	/**
//	 * 隶属于的最后一级的CacheTableStructure的key
//	 */
//	private String key;
//
//	/**
//	 * 隶属于的最后一级的CacheTableStructure的key
//	 * @return key
//	 */
//	public String getKey()
//	{
//		lock.readLock().lock();
//		try
//		{
//			return key;
//		}
//		catch (Exception e)
//		{
//			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
//			return null;
//		}
//		finally
//		{
//			lock.readLock().unlock();
//		}
//	}
//
//	/**
//	 * 隶属于的最后一级的CacheTableStructure的key
//	 * @param key 要设置的 key
//	 */
//	public void setKey(String key)
//	{
//		lock.writeLock().lock();
//		try
//		{
//			this.key = key;
//		}
//		catch (Exception e)
//		{
//			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
//		}
//		finally
//		{
//			lock.writeLock().unlock();
//		}
//	}

//	/**
//	 * 隶属于的最后一级的CacheTableStructure
//	 */
//	private volatile CacheTableStructure parent;
//
//	/**
//	 * 隶属于的最后一级的CacheTableStructure
//	 * @return parent
//	 */
//	public CacheTableStructure getParent()
//	{
//		lock.readLock().lock();
//		try
//		{
//			return parent;
//		}
//		catch (Exception e)
//		{
//			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
//			return null;
//		}
//		finally
//		{
//			lock.readLock().unlock();
//		}
//	}
//
//	/**
//	 * 隶属于的最后一级的CacheTableStructure
//	 * @param parent 要设置的 parent
//	 */
//	public void setParent(CacheTableStructure parent)
//	{
//		lock.writeLock().lock();
//		try
//		{
//			this.parent = parent;
//		}
//		catch (Exception e)
//		{
//			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
//		}
//		finally
//		{
//			lock.writeLock().unlock();
//		}
//	}

	/**
	 * 过期时间
	 */
	private volatile long expiresTime;

	/**
	 * 过期时间
	 * @return expiresTime
	 */
	public long getExpiresTime()
	{
		return expiresTime;
	}

	/**
	 * 过期时间
	 * @param expiresTime 要设置的 expiresTime
	 */
	public void setExpiresTime(long expiresTime)
	{
		lock.writeLock().lock();
		try
		{
			this.expiresTime = expiresTime;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 链表上一个对象
	 */
	private volatile ICacheTableRow previous;
	
	/**
	 * 链表上一个对象
	 */
	public ICacheTableRow getPrevious()
	{
		return previous;
	}
	
	/**
	 * 链表上一个对象
	 */
	public void setPrevious(ICacheTableRow cacheTableRow)
	{
		this.previous=cacheTableRow;
	}

	/**
	 * 链表下一个对象
	 */
	private volatile ICacheTableRow next;
	
	/**
	 * 链表下一个对象
	 */
	public ICacheTableRow getNext()
	{
		return next;
	}
	
	/**
	 *链表下一个对象
	 */
	public void setNext(ICacheTableRow cacheTableRow)
	{
		this.next=cacheTableRow;
	}

	/**
	 * 表行字段集
	 * @return 表行字段集
	 */
	public ConcurrentHashMap<String, ICacheTableField> getFields()
	{
		return fields;
	}

//	/**
//	 * 行数据是否存在改变
//	 */
//	private volatile boolean isChange = false;

//	/**
//	 * 行数据是否存在改变
//	 * @return
//	 */
//	public boolean getIsChange()
//	{
//		lock.readLock().lock();
//		try
//		{
//			return this.isChange;
//		}
//		catch (Exception e)
//		{
//			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
//			return false;
//		}
//		finally
//		{
//			lock.readLock().unlock();
//		}
//	}
//	
//	/**
//	 * 行数据是否存在改变
//	 * @param value 要设置的值
//	 */
//	public void setIsChange(boolean value)
//	{
//		lock.writeLock().lock();
//		try
//		{
//			this.isChange = value;
//		}
//		catch (Exception e)
//		{
//			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
//		}
//		finally
//		{
//			lock.writeLock().unlock();
//		}
//	}
	
	/**
	 * 是否为新创建的行数据(对应的数据库不存在该条数据，或者没有向数据库存储过)
	 */
	private volatile boolean isNewRow=true;
	
	/**
	 * 是否为新创建的行数据(对应的数据库不存在该条数据，或者没有向数据库存储过)
	 * @return
	 */
	public boolean getIsNewRow()
	{
		lock.readLock().lock();
		try
		{
			return this.isNewRow;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}
	
	/**
	 * 是否为新创建的行数据(对应的数据库不存在该条数据，或者没有向数据库存储过)
	 * @param value 要设置的值
	 */
	public void setIsNewRow(boolean value)
	{
		lock.writeLock().lock();
		try
		{
			this.isNewRow = value;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * 行数据发生改变
	 * @return
	 */
	public void change()
	{
		change(false);
	}
	
	/**
	 * 行数据发生改变
	 * @param saveChangeSaveFlag 是否立即保存其行字段中“isChangeSaveFlag”为true的字段
	 */
	public void change(boolean saveChangeSaveFlag)
	{
//		setIsChange(true);
		dispatchEvent(new CacheEvent(CacheEvent.ROW_FIELD_DATA_CHANGE, saveChangeSaveFlag));
	}
	
	public CacheTableRow()
	{
	}

	/**
	 * 增加行字段单元数据
	 * @param fieldName 字段名，值不能为空
	 * @param fieldData 字段内容数据
	 * @param fieldJavaType 字段数据类型
	 * @return 如果增加成功返回true,否则返回false
	 */
	public <T> boolean addField(String fieldName, T fieldData)
	{
		if (fieldName == null || fieldName.isEmpty())
			return false;
		lock.writeLock().lock();
		try
		{
			JavaType fieldJavaType = TypesUtil.javaClassTypeToJavaType(fieldData.getClass().getSimpleName());
			return addField(fieldName, fieldData, fieldJavaType);
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 增加行字段单元数据
	 * @param fieldName 字段名，值不能为空
	 * @param fieldData 字段内容数据
	 * @param fieldJavaType 字段数据类型
	 * @return 如果增加成功返回true,否则返回false
	 */
	public <T> boolean addField(String fieldName, T fieldData, JavaType fieldJavaType)
	{
		if (fieldName == null || fieldName.isEmpty())
			return false;
		lock.writeLock().lock();
		try
		{
			ICacheTableField cacheTableField = DataUtil.cacheTableField(fieldData, fieldJavaType);
			return addField(fieldName, cacheTableField);
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 增加行字段单元数据
	 * @param fieldName 字段名，值不能为空
	 * @param cacheTableField 字段，值不能为空
	 * @return 如果增加成功返回true,否则返回false
	 */
	public boolean addField(String fieldName, ICacheTableField cacheTableField)
	{
		if (fieldName == null || fieldName.isEmpty() || cacheTableField == null)
			return false;
		lock.writeLock().lock();
		try
		{
			cacheTableField.setCacheTableRow(this);
			cacheTableField.setFieldName(fieldName);
			fields.put(fieldName, cacheTableField);
			return true;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 增加或更新行字段单元数据
	 * @param fieldName 字段名，值不能为空
	 * @param fieldData 字段内容数据
	 * @return 如果增加成功返回true,否则返回false
	 */
	public <T> boolean addOrUpdateField(String fieldName, T fieldData)
	{
		if (fieldName == null || fieldName.isEmpty())
			return false;
		lock.writeLock().lock();
		try
		{
			JavaType fieldJavaType = TypesUtil.javaClassTypeToJavaType(fieldData.getClass().getSimpleName());
			return addOrUpdateField(fieldName, fieldData, fieldJavaType);
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 增加或更新行字段单元数据
	 * @param fieldName 字段名，值不能为空
	 * @param fieldData 字段内容数据
	 * @param fieldJavaType 字段数据类型
	 * @return 如果增加成功返回true,否则返回false
	 */
	public <T> boolean addOrUpdateField(String fieldName, T fieldData, JavaType fieldJavaType)
	{
		if (fieldName == null || fieldName.isEmpty())
			return false;
		lock.writeLock().lock();
		try
		{
			if (fields.containsKey(fieldName))
			{// 如果存在字段则更新
				ICacheTableField ctField = getField(fieldName);
				ctField.setJavaType(fieldJavaType);
				ctField.setData(fieldData);
				return true;
			}
			else
			{// 不存在则加入
				return addField(fieldName, fieldData, fieldJavaType);
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 增加或更新行字段单元数据
	 * @param fieldName 字段名，值不能为空
	 * @param cacheTableField 字段
	 * @return 如果增加成功返回true,否则返回false
	 */
	public boolean addOrUpdateField(String fieldName, ICacheTableField cacheTableField)
	{
		if (fieldName == null || fieldName.isEmpty())
			return false;
		lock.writeLock().lock();
		try
		{
			if (fields.containsKey(fieldName))
			{// 如果存在字段则更新
				ICacheTableField ctField = getField(fieldName);
				ctField.setJavaType(cacheTableField.getJavaType());
				ctField.setData(cacheTableField.getData());
				return true;
			}
			else
			{// 不存在则加入
				return addField(fieldName, cacheTableField);
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 取得字段单元
	 * @param fieldName 字段名
	 * @return 字段单元，如果字段单元为null，则返回null
	 */
	public ICacheTableField getField(String fieldName)
	{
		return fields.get(fieldName);
	}

	/**
	 * 取得字段单元数据
	 * @param fieldName 字段名
	 * @return 字段单元数据，如果对应的字段单元为null，则返回null
	 */
	public <T> T getFieldData(String fieldName)
	{
		T data = null;
		lock.readLock().lock();
		try
		{
			ICacheTableField cacheTableField = getField(fieldName);
			if (cacheTableField != null)
			{
				data = cacheTableField.getData();
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		finally
		{
			lock.readLock().unlock();
		}
		return data;
	}

	/**
	 * 取得行字段数据是否被更改状态
	 * @param isChange 更改状态：true为有更改,false为没有更改
	 * @return 只要行其中存在一个字段被修改，返回true,行所有字段都未被修改过，则返回false
	 */
	public boolean getFieldChangeState()
	{
		lock.readLock().lock();
		boolean changeState = false;
		try
		{
			Collection<ICacheTableField> fieldValues = fields.values();
			for (ICacheTableField cacheTableField : fieldValues)
			{
				if (cacheTableField != null)
				{
					changeState = cacheTableField.getIsChange();
					if (changeState)
					{
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableRow.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.readLock().unlock();
		}
		return changeState;
	}

	/**
	 * 检查是否存在指定的字段
	 * @param fieldName 字段名
	 * @return 如果存返回true，否则返回false
	 */
	public boolean containsField(String fieldName)
	{
		return fields.containsKey(fieldName);
	}

	/**
	 * 立即通知表进行数据物理存储
	 */
	public void toSave()
	{
		dispatchEvent(new CacheEvent(CacheEvent.DATA_TO_SAVE));
	}
	
	/*
	 * 克隆，深度复制一份全新的数据,数据内容仅为行字段数据集，其它行属性则为默认值
	 */
	public ICacheTableRow clone()
	{
		lock.readLock().lock();
		try
		{
				ICacheTableRow cloneCacheTableRow = Base.newClass(CacheTableRow.class);
				for(ICacheTableField cacheTableField : fields.values())
				{
					ICacheTableField cloneCacheTableField = DataUtil.cacheTableField(cacheTableField.getData(), cacheTableField.getJavaType());
					cloneCacheTableField.setIsSqlSave(cacheTableField.getIsSqlSave());
					cloneCacheTableField.setIsChangeSaveFlag(cacheTableField.getIsChangeSaveFlag());
					cloneCacheTableRow.addField(cacheTableField.getFieldName(), cloneCacheTableField);
				}
			return cloneCacheTableRow;
		}
		catch (Exception e)
		{
			Logger.getLogger(ICacheTableField.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.readLock().unlock();
		}
		return  null;
	}
	
	/**
	 * 将相同字段的数据复制至目标行
	 * @param desCacheTableRow 目标行
	 */
	public void copy(ICacheTableRow desCacheTableRow)
	{
		if (fields != null)
		{
			for(ICacheTableField cacheTableField : fields.values())
			{
				if(desCacheTableRow.containsField(cacheTableField.getFieldName()))
				{
					desCacheTableRow.getField(cacheTableField.getFieldName()).setData(cacheTableField.getData());
				}
			}
		}
	}

	public synchronized void dispose()
	{
		if (fields != null)
		{
			Collection<ICacheTableField> fieldValues = fields.values();
			for (ICacheTableField cacheTableField : fieldValues)
			{
				if (cacheTableField != null)
				{
					cacheTableField.dispose();
					cacheTableField = null;
				}
			}
			fields.clear();
			fields = null;
		}
		previous = null;
		next = null;
//		parent = null;
	}
}
