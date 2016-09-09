package base.data.cache.table;

import java.util.Comparator;
import java.util.Vector;

import base.types.SortType;

public interface ICacheTable
{
	/**
	 * 表名
	 * @return name
	 */
	public String getName();

	/**
	 * 集合键字段名列表组
	 * @return keyFieldNames
	 */
	public String[][] getKeyFieldNameGroups();

	/**
	 * 字段名称列表，该列表是表中所出现的所有的字段名
	 */
	public Vector<String> fieldNames();
	/**
	 * 缓存表
	 * @param name 表名
	 * @param primaryKeyFieldName 表主键字段名
	 * @param keyFieldNameGroups 集合键字段名集列表组，每一组值将做为行数据key，如果指定多个字段，中间以“_”连接，最终数据为：[0]字段值_[1]字段值_...
	 */
	public void init(String name, String primaryKeyFieldName, String[][] keyFieldNameGroups);

	/**
	 * 缓存表
	 * @param name 表名
	 * @param primaryKeyFieldName 表主键字段名
	 * @param keyFieldNameGroups 集合键字段名集列表组，每一组值将做为行数据key，如果指定多个字段，中间以“_”连接，最终数据为：[0]字段值_[1]字段值_...
	 * @param capacity 数据行记录容量的大小，使用LRU算法淘汰机制，值为-1时则表示不限容量
	 */
	public void init(String name, String primaryKeyFieldName, String[][] keyFieldNameGroups, int capacity);

	/**
	 * 增加表行
	 * @param cacheTableRow 行数据，值不允许为空
	 * @return 增加成功返回true，否则返回false
	 */
	public boolean addRow(ICacheTableRow cacheTableRow);

	/**
	 * 增加表行
	 * @param cacheTableRow 行数据，值不允许为空
	 * @param validTime 数据有效时间(毫秒),值<0时则表示不限
	 * @return 增加成功返回true，否则返回false
	 */
	public boolean addRow(ICacheTableRow cacheTableRow, long validTime);

	/**
	 * 插入表行
	 * @param cacheTableRow 行数据，值不允许为空
	 * @param index 表行索引，值不能小于0
	 * @return 增加成功返回true，否则返回false
	 */
	public boolean insertRow(ICacheTableRow cacheTableRow, int index);

	/**
	 * 插入表行
	 * @param cacheTableRow 行数据，值不允许为空
	 * @param index 表行索引，值不能小于0
	 * @param validTime 数据有效时间(毫秒),值<0时则表示不限
	 * @return 增加成功返回true，否则返回false
	 */
	public boolean insertRow(ICacheTableRow cacheTableRow, int index, long validTime);

	/**
	 * 取得所属对应键值列表行列表中的第一个行数据
	 * @param keyFieldNames 集合键字段名列表
	 * @param keyFieldValues 集合键值列表
	 * @return 取得的行数据
	 */
	public ICacheTableRow getRow(String[] keyFieldNames, String[] keyFieldValues);
	
	/**
	 * 行字段数据交换
	 * @param cacheTableRow1 行1
	 * @param cacheTableRow2 行2
	 */
	public void exchangeRow(ICacheTableRow cacheTableRow1, ICacheTableRow cacheTableRow2);
	
	/**
	 * 行字段数据交换
	 * @param cacheTableRow1 行1
	 * @param cacheTableRow2 行2
	 * @param excludeFields 要排除的字段
	 */
	public void exchangeRow(ICacheTableRow cacheTableRow1, ICacheTableRow cacheTableRow2, String[] excludeFields);
	
	/**
	 * 取得所有行数据
	 * @return 取得的行数据
	 */
	public Vector<ICacheTableRow> getRows();

	/**
	 * 取得符合键值列表的数据列表
	 * @param keyFieldNames 集合键字段名列表
	 * @param keyFieldValues 集合键值列表
	 * @return 取得的行数据列表
	 */
	public Vector<ICacheTableRow> getRows(String[] keyFieldNames, String[] keyFieldValues);

	/**
	 * 取得行
	 * @param rowIndex 行索引
	 * @return 取得的行数据，如果提供的索引超出范围，则返回null
	 */
	public ICacheTableRow getRow(int rowIndex);

	/**
	 * 移除表行
	 * @param rowIndex 行索引
	 * @return 移除成功或者指定的索引不存于列表返回true,否则返回false
	 */
	public boolean removeRow(int rowIndex);

	/**
	 * 移除行数据
	 * @param cacheTableRow 行数据
	 * @return 移除成功或者指定的行数据为空返回true,否则返回false
	 */
	public boolean removeRow(ICacheTableRow cacheTableRow);

	/**
	 * 移除表所有行数据并释放行数据
	 * @return 移除成功返回true,否则返回false
	 */
	public boolean removeAll();

	/**
	 * 取得列表中首次出现的行索引
	 * @param keyFieldNames 集合键字段列表
	 * @param keyFieldValues 集合键值列表
	 * @return 行索引，如果指定的主键Key不存于列表中，则返回-1
	 */
	public int rowIndexOf(String[] keyFieldNames, String[] keyFieldValues);

	/**
	 * 取得列表中最后一次出现的行索引
	 * @param keyFieldNames 集合键字段列表
	 * @param keyFieldValues 集合键值列表
	 * @return 行索引，如果指定的主键Key不存于列表中，则返回-1
	 */
	public int rowLastIndexOf(String[] keyFieldNames, String[] keyFieldValues);

	/**
	 * 检测是否包含有指定集合键值的行
	 * @param keyFieldNames 集合键字段列表
	 * @param keyFieldValues 集合键值列表
	 * @return 如果存在返回true, 否则返回false
	 */
	public boolean containsKeyField(String[] keyFieldNames, String[] keyFieldValues);

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
	public void sortOn(final Object[] fieldNames, final SortType[] options);

	/**
	 * 排序表行数据
	 * @param comparator 排序比较函数
	 */
	public void sort(Comparator<? super ICacheTableRow> comparator);

	/**
	 * 表行大小
	 * @return 表行大小
	 */
	public int getRowSize();

	/**
	 * 创建当前CacheTable的指定字段的新CacheTable，新CacheTable的数据将会自动同步更新于当前CacheTable，其同步项为当前CacheTable增加新行、移除行以及行字段的内容数据。
	 * @param name CacheTable表名
	 * @param primaryKeyFieldName 表主键字段名
	 * @param keyFieldNameGroups 集合键字段名列表组，此值将做为行数据key，如果指定多个字段，中间以“_”连接，最终数据为：[0]字段值_[1]字段值_…，值不允许为空
	 * @param fieldNames 指定字段名列表，字段需存在于当前的CacheTable中，如：new String[]{"ID", "Name", ...}
	 * @return 创建成功返回新创建的CacheTable，否则返回null
	 */
	public ICacheTable newCacheTable(String name, String primaryKeyFieldName, String[][] keyFieldNameGroups, String[] fieldNames);

	/**
	 * 清除数据(不对行数据做释放处理)
	 */
	public void clear();

	/*
	 * 克隆，深度复制一份全新的数据
	 */
	public ICacheTable clone();

	public void dispose();

	public void dispose(boolean isDispose);
}
