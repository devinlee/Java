package base.data.cache.table;

import java.util.Date;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.types.JavaType;
import base.util.TypesUtil;

/**
 * 缓存表字段
 * @author Devin
 *
 */
public class CacheTableField implements ICacheTableField
{
	/**
	 * 线程读写锁
	 */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * 隶属于的表行
	 */
	private volatile ICacheTableRow cacheTableRow;

	/**
	 * 隶属于的表行
	 * @return cacheTableRow 表行，如果未指定行，则返回null
	 */
	public ICacheTableRow getCacheTableRow()
	{
		return cacheTableRow;
	}

	/**
	 * 字段名
	 */
	private volatile String fieldName;

	/**
	 * 字段名
	 * @return fieldName 字段名
	 */
	public String getFieldName()
	{
		lock.readLock().lock();
		try
		{
			return fieldName;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableField.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 字段名
	 * @param fieldName 字段名
	 */
	public void setFieldName(String fieldName)
	{
		lock.writeLock().lock();
		try
		{
			this.fieldName = fieldName;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableField.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 字段值类型
	 */
	private volatile JavaType javaType;

	/**
	 * 字段值类型
	 * @return javaType 字段值类型
	 */
	public JavaType getJavaType()
	{
		lock.readLock().lock();
		try
		{
			return javaType;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableField.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 字段值类型
	 * @return javaType 字段值类型
	 */
	public void setJavaType(JavaType javaType)
	{
		lock.writeLock().lock();
		try
		{
			this.javaType = javaType;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableField.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 字段数据是否被更改
	 */
	private volatile boolean isChange = false;

	/**
	 * 字段数据是否被更改
	 * @return isChange
	 */
	public boolean getIsChange()
	{
		lock.readLock().lock();
		try
		{
			return isChange;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableField.class.getName()).log(Level.SEVERE, null, e);
			return true;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 字段数据是否被更改
	 * @return isChange
	 */
	public void setIsChange(boolean isChange)
	{
		lock.writeLock().lock();
		try
		{
			if (this.isChange == isChange)
				return;
			this.isChange = isChange;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableField.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * 是否为改变后可立即进行物理存储的标识，行在执行change(true)后将会立即物理存储
	 */
	private volatile boolean isChangeSaveFlag;
	/**
	 * 是否为改变后可立即进行物理存储的标识，行在执行change(true)后将会立即物理存储
	 */
	public boolean getIsChangeSaveFlag()
	{
		lock.readLock().lock();
		try
		{
			return isChangeSaveFlag;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableField.class.getName()).log(Level.SEVERE, null, e);
			return true;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}
	/**
	 * 是否为改变后可立即进行物理存储的标识，行在执行change(true)后将会立即物理存储
	 */
	public void setIsChangeSaveFlag(boolean isChangeSaveFlag)
	{
		lock.writeLock().lock();
		try
		{
			if (this.isChangeSaveFlag == isChangeSaveFlag)
				return;
			this.isChangeSaveFlag = isChangeSaveFlag;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableField.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 隶属于的表行
	 * @param cacheTableRow 要设置的 表行
	 */
	public void setCacheTableRow(ICacheTableRow cacheTableRow)
	{
		lock.writeLock().lock();
		try
		{
			this.cacheTableRow = cacheTableRow;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableField.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 字段单元数据
	 */
	private volatile Object data;

	/**
	 * 字段单元数据
	 * @param <T>
	 * @return 
	 * @return data
	 */
	@SuppressWarnings("unchecked")
	public <T> T getData()
	{
		lock.readLock().lock();
		try
		{
			return (T) data;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableField.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 字段单元数据
	 * @param data 要设置的 data
	 */
	public <T> void setData(T data)
	{
		setData(data, false);
	}

	/**
	 * 字段单元数据
	 * @param data 要设置的 data
	 * @param isChangeSaveFlag 是否为改变后可立即进行物理存储的标识，行在执行change(true)后将会立即物理存储
	 */
	public <T> void setData(T data, boolean isChangeSaveFlag)
	{
		lock.writeLock().lock();
		try
		{
			switch (this.javaType)
			{
				case DATE:
					Date fieldData = (Date) this.data;
					Date valueData = (Date) data;
					if(fieldData.compareTo(valueData)==0)return;
//					this.data = (Date)data;
					break;
				case STRING:
					String strFieldData = (String)this.data;
					String strValueData = (String) data;
					if(strFieldData.equals(strValueData))return;
//					this.data = (String)data;
					break;
				case LONG:
					long longFieldData = (long)this.data;
					long longValueData = (long) data;
					if(longFieldData==longValueData)return;
//					this.data = (long)data;
					break;
//				case BOOLEAN:
//					if(this.data.equals(data))return;
//					this.data = (boolean)data;
//					break;
//				case BYTE:
//					if(this.data.equals(data))return;
//					this.data = (byte)data;
//					break;
//				case DOUBLE:
//					if(this.data.equals(data))return;
//					this.data = (double)data;
//					break;
//				case SHORT:
//					if(this.data.equals(data))return;
//					this.data = (short)data;
//					break;
//				case INTEGER:
//					if(this.data.equals(data))return;
//					this.data = (int)data;
//					break;
//				case FLOAT:
//					if(this.data.equals(data))return;
//					this.data = (float)data;
//					break;
//				case CHAR:
//					if(this.data.equals(data))return;
//					this.data = (char)data;
//					break;
//				case BIGDECIMAL:
//					if(this.data.equals(data))return;
//					this.data = (BigDecimal)data;
//					break;
				default:
					if(this.data.equals(data))return;
//					if (this.data == data)return;
//					this.data = data;
					break;
			}
			this.data = data;
			this.isChangeSaveFlag=isChangeSaveFlag;
			this.setIsChange(true);
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableField.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * 字段数据是否需要进行Sql物理存储(如自定义字段仅在内存中，不为物理数据库字段时不需要进行Sql物理存储的)
	 */
	private volatile boolean isSqlSave = true;

	/**
	 * 字段数据是否需要进行Sql物理存储(如自定义字段仅在内存中，不为物理数据库字段时不需要进行Sql物理存储的)
	 * @return isSqlSave
	 */
	public boolean getIsSqlSave()
	{
		lock.readLock().lock();
		try
		{
			return isSqlSave;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableField.class.getName()).log(Level.SEVERE, null, e);
			return true;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 字段数据是否需要进行Sql物理存储(如自定义字段仅在内存中，不为物理数据库字段时不需要进行Sql物理存储的)
	 * @return isSqlSave
	 */
	public ICacheTableField setIsSqlSave(boolean isSqlSave)
	{
		lock.writeLock().lock();
		try
		{
			if (this.isSqlSave == isSqlSave)
				return this;
			this.isSqlSave = isSqlSave;
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheTableField.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
		return this;
	}

	/**
	 * 缓存表字段
	 */
	public CacheTableField()
	{
	}
	
	/**
	 * 缓存表字段
	 * @param data 字段数据
	 * @param javaType 字段数据类型
	 */
	public <T> void init(T data)
	{
		init(data, TypesUtil.javaClassTypeToJavaType(data.getClass().getSimpleName()));
	}

	/**
	 * 缓存表字段
	 * @param data 字段数据
	 * @param javaType 字段数据类型
	 */
	public <T>void init(T data, JavaType javaType)
	{
		this.data = data;
		this.javaType = javaType;
	}
	
	public synchronized void dispose()
	{
		cacheTableRow = null;
		data = null;
	}
}
