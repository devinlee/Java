package base.data.cache.table;

import java.util.Comparator;
import java.util.Vector;

import base.types.JavaType;
import base.types.SortType;

public interface ICacheDataTable
{
	/**
	 * 数据库缓存表
	 * @param name 缓存表名称
	 * @param primaryKeyFieldName 主键字段名(数据表中必须要将此字段设置为主键，不然会数据存储出错)
	 * @param keyFieldNameGroups 集合键字段名列表组，此值将做为行数据key，如果指定多个字段，中间以“_”连接，最终数据为：[0]字段值_[1]字段值_...
	 * @param capacity 数据行记录容量的大小，使用LRU算法淘汰机制，值为-1时则表示不限容量
	 * @param sqlConnectionPoolName 数据库连接池名称
	 * @param sqlTableName 数据库表名
	 * @param customFieldNames 自定义的字段名数组
	 * @param customFieldJaveTypes 对应的自定义的字段数据类型数组，其长度必须与customFieldNames相等且对应
	 */
	public void init(String name, String primaryKeyFieldName, String[][] keyFieldNameGroups, int capacity, String sqlConnectionPoolName, String sqlTableName, String[] customFieldNames, JavaType[] customFieldJaveTypes);
	
	/**
	 * 从数据表中取得所有数据填充缓存表，如果指定了数据行记录容量的大小 capacity，则只填充不大于capacity值的行大小数据
	 */
	public void fill();
	
	/**
	 * 从数据表中取得所有数据填充缓存表，如果指定了数据行记录容量的大小 capacity，则只填充不大于capacity值的行大小数据
	 * @param subsequentSql 填充时的SQL后续Sql语句，如：WHERE 字段1>10 And 字段2<100；WHERE 字段1>10 And 字段2<100 ORDER BY Level DESC
	 */
	public void fill(String subsequentSql);

	/**
	 * 更新有更改过的数据至数据库表
	 * @return 如果全部执成功，返回true,否则返回false
	 */
	public boolean updateToDataByChange();

	/**
	 * 更新有更改过的数据至数据库表
	 * @param cacheTableRow 指定的仅需要保存的表行，指保存指定的表行，其它行不执行任何操作
	 * @return 如果全部执成功，返回true,否则返回false
	 */
	public boolean updateToDataByChange(ICacheTableRow cacheTableRow);

	/**
	 * 更新有更改过的数据至数据库表
	 * @param cacheTableRow 指定的仅需要保存的表行，指保存指定的表行，其它行不执行任何操作
	 * @param saveChangeSaveFlag 仅保存指定行其字段“isChangeSaveFlag”为true的字段
	 * @return 如果全部执成功，返回true,否则返回false
	 */
	public boolean updateToDataByChange(ICacheTableRow cacheTableRow, boolean saveChangeSaveFlag);

	/**
	 * 启动自动更新已更改数据至数据库表的操作
	 * @param period 更新的间隔时间(毫秒)
	 */
	public void startAutoUpdate(int period);

	/**
	 * 停止自动更新已更改数据至数据库表的操作
	 */
	public void stopAutoUpdate();
	
	/**
	 * 是否开启定时保存至物理数据库
	 */
	public boolean getIsAutoUpdate();
	
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
	 * 检测是否包含有指定集合键值的行
	 * @param keyFieldNames 集合键字段列表
	 * @param keyFieldValues 集合键值列表
	 * @return 如果存在返回true, 否则返回false
	 */
	public boolean containsKeyField(String[] keyFieldNames, String[] keyFieldValues);
	
	/**
	 * 移除表所有行数据并释放行数据
	 * @return 移除成功返回true,否则返回false
	 */
	public boolean removeAll();
	
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

	public void dispose();
}
