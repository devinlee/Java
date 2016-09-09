package base.data.cache.table;

import base.types.JavaType;

public interface ICacheTableField
{
	/**
	 * 隶属于的表行
	 * @return cacheTableRow 表行，如果未指定行，则返回null
	 */
	public ICacheTableRow getCacheTableRow();

	/**
	 * 字段名
	 * @return fieldName 字段名
	 */
	public String getFieldName();

	/**
	 * 字段名
	 * @param fieldName 字段名
	 */
	public void setFieldName(String fieldName);

	/**
	 * 字段值类型
	 * @return javaType 字段值类型
	 */
	public JavaType getJavaType();

	/**
	 * 字段值类型
	 * @return javaType 字段值类型
	 */
	public void setJavaType(JavaType javaType);

	/**
	 * 字段数据是否被更改
	 * @return isChange
	 */
	public boolean getIsChange();

	/**
	 * 字段数据是否被更改
	 * @return isChange
	 */
	public void setIsChange(boolean isChange);
	/**
	 * 是否为改变后可立即进行物理存储的标识，行在执行change(true)后将会立即物理存储
	 */
	public boolean getIsChangeSaveFlag();
	/**
	 * 是否为改变后可立即进行物理存储的标识，行在执行change(true)后将会立即物理存储
	 */
	public void setIsChangeSaveFlag(boolean isChangeSaveFlag);

	/**
	 * 隶属于的表行
	 * @param cacheTableRow 要设置的 表行
	 */
	public void setCacheTableRow(ICacheTableRow cacheTableRow);

	/**
	 * 字段单元数据
	 * @param <T>
	 * @return 
	 * @return data
	 */
	public <T> T getData();

	/**
	 * 字段单元数据
	 * @param data 要设置的 data
	 */
	public <T> void setData(T data);

	/**
	 * 字段单元数据
	 * @param data 要设置的 data
	 * @param isChangeSaveFlag 是否为改变后可立即进行物理存储的标识，行在执行change(true)后将会立即物理存储
	 */
	public <T> void setData(T data, boolean isChangeSaveFlag);

	/**
	 * 字段数据是否需要进行Sql物理存储(如自定义字段仅在内存中，不为物理数据库字段时不需要进行Sql物理存储的)
	 * @return isSqlSave
	 */
	public boolean getIsSqlSave();

	/**
	 * 字段数据是否需要进行Sql物理存储(如自定义字段仅在内存中，不为物理数据库字段时不需要进行Sql物理存储的)
	 * @return isSqlSave
	 */
	public ICacheTableField setIsSqlSave(boolean isSqlSave);

	/**
	 * 缓存表字段
	 * @param data 字段数据
	 * @param javaType 字段数据类型
	 */
	public <T> void init(T data);

	/**
	 * 缓存表字段
	 * @param data 字段数据
	 * @param javaType 字段数据类型
	 */
	public <T>void init(T data, JavaType javaType);
	
	public void dispose();
}
