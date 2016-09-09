package base.utils;

import base.Base;
import base.data.cache.table.CacheTableField;
import base.data.cache.table.CacheTableRow;
import base.data.cache.table.ICacheTableField;
import base.data.cache.table.ICacheTableRow;
import base.types.JavaType;

public class DataUtil
{
	/**
	 * 创建新字段
	 * @param data
	 * @param javaType
	 * @return
	 */
	public static <T> ICacheTableField cacheTableField(T data, JavaType javaType)
	{
		ICacheTableField cacheTableField = Base.newClass(CacheTableField.class);
		cacheTableField.init(data, javaType);
		return cacheTableField;
	}
	
	/**
	 * 创建新字段
	 * @param data
	 * @param javaType
	 * @return
	 */
	public static <T> ICacheTableField cacheTableField(T data)
	{
		ICacheTableField cacheTableField = Base.newClass(CacheTableField.class);
		cacheTableField.init(data);
		return cacheTableField;
	}
	
	/**
	 * 创建新行
	 * @return
	 */
	public static ICacheTableRow cacheTableRow()
	{
		ICacheTableRow cacheTableRow = Base.newClass(CacheTableRow.class);
		return cacheTableRow;
	}
}