package base.data.cache.table;

import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.types.JavaType;
import base.types.SortType;

public class CacheTableComparator implements Comparator<ICacheTableRow>
{
	private int fieldNamesIndex = 0;
	private int optionsIndex = 0;
	private int sortResult = 0;
	private JavaType javaType;
	private SortType sortType;
	private String fieldName;
	private Object fieldValue1;
	private Object fieldValue2;
	private Object[] fieldNames;
	private SortType[] options;
	private String tableName;

	public CacheTableComparator(String tableName, Object[] fieldNames, SortType[] options)
	{
		this.tableName=tableName;
		this.fieldNames=fieldNames;
		this.options=options;
	}
	
	/**
	 * 排序比较值大于时处理
	 * @param sortType 排序类型方式
	 * @return
	 */
	private int greaterHandle(SortType sortType)
	{
		int sortRst = 0;
		if (sortType == null)
			sortType = SortType.ASC;
		switch (sortType)
		{
			case ASC:
				sortRst = 1;
				break;
			case DESC:
				sortRst = -1;
				break;
			default:
				sortRst = 0;
				break;
		}
		return sortRst;
	}

	/**
	 * 排序比较值小于时处理
	 * @param sortType 排序类型方式
	 * @return
	 */
	private int lessHandle(SortType sortType)
	{
		int sortRst = 0;
		if (sortType == null)
			sortType = SortType.ASC;
		switch (sortType)
		{
			case ASC:
				sortRst = -1;
				break;
			case DESC:
				sortRst = 1;
				break;
		}
		return sortRst;
	}

	/**
	 * 取得排序比较值
	 * @param cacheTableRow1 比较字段1
	 * @param cacheTableRow2 比较字段2
	 * @return
	 */
	private int getSort(ICacheTableRow cacheTableRow1, ICacheTableRow cacheTableRow2)
	{
		sortResult = 0;
		if (fieldNames.length - 1 >= fieldNamesIndex)
		{
			javaType = (JavaType) fieldNames[fieldNamesIndex++];
			fieldName = (String) fieldNames[fieldNamesIndex++];
			if(!cacheTableRow1.containsField(fieldName) || !cacheTableRow2.containsField(fieldName))
			{
				Logger.getLogger(CacheTable.class.getName()).log(Level.SEVERE, null, new Exception("缓存表 "+ tableName +" 在执行排序时，遇到不存在的字段 "+fieldName));
				return sortResult;
			}

			fieldValue1 = cacheTableRow1.getFieldData(fieldName);
			fieldValue2 = cacheTableRow2.getFieldData(fieldName);
			sortType = options[optionsIndex++];

			switch (javaType)
			{
				case INTEGER:
				default:
					int iValue1 = (int) fieldValue1;
					int iValue2 = (int) fieldValue2;
					if (iValue1 < iValue2)
					{
						sortResult = lessHandle(sortType);
					}
					else if (iValue1 > iValue2)
					{
						sortResult = greaterHandle(sortType);
					}
					else
					{
						if (fieldNames.length - 1 >= fieldNamesIndex)
						{
							getSort(cacheTableRow1, cacheTableRow2);
						}
						else
						{
							sortResult = 0;
						}
					}
					break;
				case BOOLEAN:
					int bValue1 = (boolean) fieldValue1 ? 1 : 0;
					int bValue2 = (boolean) fieldValue2 ? 1 : 0;
					if (bValue1 < bValue2)
					{
						sortResult = lessHandle(sortType);
					}
					else if (bValue1 > bValue2)
					{
						sortResult = greaterHandle(sortType);
					}
					else
					{
						if (fieldNames.length - 1 >= fieldNamesIndex)
						{
							getSort(cacheTableRow1, cacheTableRow2);
						}
						else
						{
							sortResult = 0;
						}
					}
					break;
				case BYTE:
					byte btValue1 = (byte) fieldValue1;
					byte btValue2 = (byte) fieldValue2;
					if (btValue1 < btValue2)
					{
						sortResult = lessHandle(sortType);
					}
					else if (btValue1 > btValue2)
					{
						sortResult = greaterHandle(sortType);
					}
					else
					{
						if (fieldNames.length - 1 >= fieldNamesIndex)
						{
							getSort(cacheTableRow1, cacheTableRow2);
						}
						else
						{
							sortResult = 0;
						}
					}
					break;
				case SHORT:
					short sValue1 = (short) fieldValue1;
					short sValue2 = (short) fieldValue2;
					if (sValue1 < sValue2)
					{
						sortResult = lessHandle(sortType);
					}
					else if (sValue1 > sValue2)
					{
						sortResult = greaterHandle(sortType);
					}
					else
					{
						if (fieldNames.length - 1 >= fieldNamesIndex)
						{
							getSort(cacheTableRow1, cacheTableRow2);
						}
						else
						{
							sortResult = 0;
						}
					}
					break;
				case LONG:
					long lValue1 = (long) fieldValue1;
					long lValue2 = (long) fieldValue2;
					if (lValue1 < lValue2)
					{
						sortResult = lessHandle(sortType);
					}
					else if (lValue1 > lValue2)
					{
						sortResult = greaterHandle(sortType);
					}
					else
					{
						if (fieldNames.length - 1 >= fieldNamesIndex)
						{
							getSort(cacheTableRow1, cacheTableRow2);
						}
						else
						{
							sortResult = 0;
						}
					}
					break;
				case DOUBLE:
					double dValue1 = (double) fieldValue1;
					double dValue2 = (double) fieldValue2;
					if (dValue1 < dValue2)
					{
						sortResult = lessHandle(sortType);
					}
					else if (dValue1 > dValue2)
					{
						sortResult = greaterHandle(sortType);
					}
					else
					{
						if (fieldNames.length - 1 >= fieldNamesIndex)
						{
							getSort(cacheTableRow1, cacheTableRow2);
						}
						else
						{
							sortResult = 0;
						}
					}
					break;
				case FLOAT:
					float fValue1 = (float) fieldValue1;
					float fValue2 = (float) fieldValue2;
					if (fValue1 < fValue2)
					{
						sortResult = lessHandle(sortType);
					}
					else if (fValue1 > fValue2)
					{
						sortResult = greaterHandle(sortType);
					}
					else
					{
						if (fieldNames.length - 1 >= fieldNamesIndex)
						{
							getSort(cacheTableRow1, cacheTableRow2);
						}
						else
						{
							sortResult = 0;
						}
					}
					break;
				case CHAR:
					char cValue1 = (char) fieldValue1;
					char cValue2 = (char) fieldValue2;
					if (cValue1 < cValue2)
					{
						sortResult = lessHandle(sortType);
					}
					else if (cValue1 > cValue2)
					{
						sortResult = greaterHandle(sortType);
					}
					else
					{
						if (fieldNames.length - 1 >= fieldNamesIndex)
						{
							getSort(cacheTableRow1, cacheTableRow2);
						}
						else
						{
							sortResult = 0;
						}
					}
					break;
				case STRING:
					String strValue1 = (String) fieldValue1;
					String strValue2 = (String) fieldValue2;
					if (strValue1.compareTo(strValue2) < 0)
					{
						sortResult = lessHandle(sortType);
					}
					else if (strValue1.compareTo(strValue2) > 0)
					{
						sortResult = greaterHandle(sortType);
					}
					else
					{
						if (fieldNames.length - 1 >= fieldNamesIndex)
						{
							getSort(cacheTableRow1, cacheTableRow2);
						}
						else
						{
							sortResult = 0;
						}
					}
					break;
				case DATE:
					long dateValue1 = ((Date) fieldValue1).getTime();
					long dateValue2 = ((Date) fieldValue2).getTime();
					if (dateValue1 < dateValue2)
					{
						sortResult = lessHandle(sortType);
					}
					else if (dateValue1 > dateValue2)
					{
						sortResult = greaterHandle(sortType);
					}
					else
					{
						if (fieldNames.length - 1 >= fieldNamesIndex)
						{
							getSort(cacheTableRow1, cacheTableRow2);
						}
						else
						{
							sortResult = 0;
						}
					}
					break;
			}
		}
		return sortResult;
	}

	/**
	 * 排序比较方法
	 * @param cacheTableRow1 比较字段1
	 * @param cacheTableRow2 比较字段2
	 * @return 排序比较值
	 */
	public int compare(ICacheTableRow cacheTableRow1, ICacheTableRow cacheTableRow2)
	{
		fieldNamesIndex = 0;
		optionsIndex = 0;
		return getSort(cacheTableRow1, cacheTableRow2);
	}
}