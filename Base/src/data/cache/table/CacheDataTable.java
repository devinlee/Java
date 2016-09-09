package base.data.cache.table;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.data.DataFactory;
import base.data.cache.CacheEvent;
import base.event.Event;
import base.timer.Timer;
import base.timer.TimerController;
import base.types.CacheDataTableType;
import base.types.JavaType;
import base.util.DataUtil;
import base.util.SqlUtil;
import base.util.StringUtil;
import base.util.TypesUtil;

/**
 * 数据库缓存表
 * @author Devin
 *
 */
public class CacheDataTable extends CacheTable implements ICacheDataTable
{
	/**
	 * 数据库缓存表类型
	 */
	private CacheDataTableType cacheDataTableType = CacheDataTableType.NO_CAPACITY;

	/**
	 * 数据库表名
	 */
	private String sqlTableName;

	/**
	 * 数据库连接池名称
	 */
	private String sqlConnectionPoolName;

	/**
	 * 自定义的字段名数组
	 */
	private String[] customFieldNames;

	/**
	 * 对应的自定义的字段数据类型数组，其长度必须与customFieldNames相等且对应
	 */
	private JavaType[] customFieldJaveTypes;

	/**
	 * 自动更新已更改数据至数据库表的操作的计时器
	 */
	private Timer timerAutoUpdate;

	/**
	 * 是否开启定时保存至物理数据库
	 */
	private boolean isAutoUpdate=false;
	/**
	 * 是否开启定时保存至物理数据库
	 */
	public boolean getIsAutoUpdate()
	{
		return isAutoUpdate;
	}
	
	/**
	 * 自动更新已更改数据至数据库表的操作的计时器任务
	 */
	private CacheDataTableUpdateTask cacheDataTableUpdateTask;

	/**
	 * 数据库缓存表
	 */
	public CacheDataTable()
	{
	}
	
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
	public void init(String name, String primaryKeyFieldName, String[][] keyFieldNameGroups, int capacity, String sqlConnectionPoolName, String sqlTableName, String[] customFieldNames, JavaType[] customFieldJaveTypes)
	{
		init(name, primaryKeyFieldName, keyFieldNameGroups, capacity);
		this.sqlConnectionPoolName = sqlConnectionPoolName;
		this.sqlTableName = sqlTableName;
		this.customFieldNames=customFieldNames;
		this.customFieldJaveTypes=customFieldJaveTypes;
		if (capacity == -1)
		{
			this.cacheDataTableType = CacheDataTableType.NO_CAPACITY;
		}
		else
		{
			this.cacheDataTableType = CacheDataTableType.CAPACITY;
		}
	}
	
	/**
	 * 从数据表中取得所有数据填充缓存表，如果指定了数据行记录容量的大小 capacity，则只填充不大于capacity值的行大小数据
	 */
	public void fill()
	{
		fill(null);
	}
	
	/**
	 * 从数据表中取得所有数据填充缓存表，如果指定了数据行记录容量的大小 capacity，则只填充不大于capacity值的行大小数据
	 * @param subsequentSql 填充时的SQL后续Sql语句，如：WHERE 字段1>10 And 字段2<100；WHERE 字段1>10 And 字段2<100 ORDER BY Level DESC
	 */
	public void fill(String subsequentSql)
	{
		lock.writeLock().lock();
		String sqlText = "";
		switch (cacheDataTableType)
		{
			case CAPACITY:
				sqlText = "SELECT * FROM " + sqlTableName + (!StringUtil.isEmptyOrNull(subsequentSql) ? " " + subsequentSql : "") + " LIMIT " + capacity;
				break;
			case NO_CAPACITY:
			default:
				sqlText = "SELECT * FROM " + sqlTableName + (!StringUtil.isEmptyOrNull(subsequentSql) ? " " + subsequentSql : "");
				break;
		}
		
		try
		{
			ResultSet resultSet = DataFactory.sqlController().executeQuery(sqlConnectionPoolName, sqlText);
			if (resultSet != null)
			{
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				int i;
				while (resultSet.next())
				{
					ICacheTableRow cacheTableRow = DataUtil.cacheTableRow();
					cacheTableRow.setIsNewRow(false);
					for (i = 1; i <= columnCount; i++)
					{
						JavaType javaType = TypesUtil.sqlTypeToJavaType(rsmd.getColumnClassName(i));
						cacheTableRow.addField(rsmd.getColumnLabel(i), DataUtil.cacheTableField(SqlUtil.resultSetGet(resultSet, i, javaType), javaType));
					}

					//增加自定义字段，其字段初始值为null
					if(customFieldNames!=null && customFieldNames.length>0 && customFieldJaveTypes!=null && customFieldJaveTypes.length>0 && customFieldNames.length == customFieldJaveTypes.length)
					{
						for (i=0; i < customFieldNames.length; i++)
						{
							cacheTableRow.addField(customFieldNames[i], DataUtil.cacheTableField(TypesUtil.getJavaTypeDefaultValue(customFieldJaveTypes[i]), customFieldJaveTypes[i]).setIsSqlSave(false));
						}
					}
					addRow(cacheTableRow);
				}
				resultSet.close();
				resultSet = null;
			}
		}
		catch (SQLException e)
		{
			Logger.getLogger(CacheDataTable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 更新有更改过的数据至数据库表
	 * @return 如果全部执成功，返回true,否则返回false
	 */
	public boolean updateToDataByChange()
	{
		return updateToDataByChange(null, false);
	}

	/**
	 * 更新有更改过的数据至数据库表
	 * @param cacheTableRow 指定的仅需要保存的表行，指保存指定的表行，其它行不执行任何操作
	 * @return 如果全部执成功，返回true,否则返回false
	 */
	public boolean updateToDataByChange(ICacheTableRow cacheTableRow)
	{
		return updateToDataByChange(cacheTableRow, false);
	}

	/**
	 * 更新有更改过的数据至数据库表
	 * @param cacheTableRow 指定的仅需要保存的表行，指保存指定的表行，其它行不执行任何操作
	 * @param saveChangeSaveFlag 仅保存指定行其字段“isChangeSaveFlag”为true的字段
	 * @return 如果全部执成功，返回true,否则返回false
	 */
	public boolean updateToDataByChange(ICacheTableRow cacheTableRow, boolean saveChangeSaveFlag)
	{
		boolean result = false;
		lock.writeLock().lock();
		try
		{
			Vector<String> sqlTexts = new Vector<String>();
			if(cacheTableRow!=null)
			{
				sqlTexts = setTableFieldSqlText(sqlTexts, cacheTableRow, saveChangeSaveFlag);
			}
			else
			{
				while (changeRows != null && changeRows.size() > 0)
				{
					sqlTexts = setTableFieldSqlText(sqlTexts, changeRows.remove(0), saveChangeSaveFlag);
				}
			}

			if(sqlTexts.size()>0)
			{
				result = DataFactory.sqlController().executeUpdate(sqlConnectionPoolName, sqlTexts);
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CacheDataTable.class.getName()).log(Level.SEVERE, null, e);
			result = false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
		return result;
	}

	/**
	 * 设置Sql语句列表
	 * @param saveChangeSaveFlag 仅保存指定行其字段“isChangeSaveFlag”为true的字段
	 * @return 返回传入的Sql语句列表
	 */
	private Vector<String> setTableFieldSqlText(Vector<String> sqlTexts, ICacheTableRow cacheTableRow, boolean saveChangeSaveFlag)
	{
		if(cacheTableRow!=null)
		{
			StringBuffer sqlText = new StringBuffer();
			int i = 0;
			ConcurrentHashMap<String, ICacheTableField> fields = cacheTableRow.getFields();
			Collection<ICacheTableField> fieldValues = fields.values();
			if (fieldValues != null && fieldValues.size() > 0)
			{
				if(cacheTableRow.getIsNewRow())
				{//如果为新创建的行数据(对应的数据库不存在该条数据)，则需要对数据库进行插入数据操作
					StringBuffer strFieldNames = new StringBuffer();
					StringBuffer strValues = new StringBuffer();

					Iterator<ICacheTableField> cacheTableFields = fieldValues.iterator();
					while (cacheTableFields.hasNext())
					{
						ICacheTableField cacheTableField = cacheTableFields.next();
						if(cacheTableField.getIsSqlSave())
						{
							strFieldNames.append((i != 0 ? ", " : "") + cacheTableField.getFieldName());
							switch (cacheTableField.getJavaType())
							{
								case DATE:
									Date fieldData = (Date) cacheTableField.getData();
									DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									strValues.append((i != 0 ? ", " : "") + "'" + dateFormat.format(fieldData) + "'");
									break;
								case STRING:
									strValues.append((i != 0 ? ", " : "") + "'" + cacheTableField.getData() + "'");
									break;
								default:
									strValues.append((i != 0 ? ", " : "") + cacheTableField.getData());
									break;
							}
							i++;
						}
					}
//					sqlText.append("INSERT INTO " + sqlTableName + " (" + strFieldNames + ") values(" + strValues + ")");
					sqlText.append("REPLACE INTO " + sqlTableName + " (" + strFieldNames + ") values(" + strValues + ")");
					cacheTableRow.setIsNewRow(false);
					sqlTexts.add(sqlText.toString());
				}
				else
				{//对数据库进行更新数据操作
					boolean isExistUp=false;
					sqlText.append("UPDATE "+sqlTableName+" SET ");
					Iterator<ICacheTableField> cacheTableFields = fieldValues.iterator();
					while (cacheTableFields.hasNext())
					{
						ICacheTableField cacheTableField = cacheTableFields.next();
						if(cacheTableField.getIsSqlSave() && cacheTableField.getIsChange() && (saveChangeSaveFlag ? cacheTableField.getIsChangeSaveFlag() : true))
						{
							sqlText.append(i != 0 ? ","+cacheTableField.getFieldName() + "=" :  cacheTableField.getFieldName() + "=");
							switch (cacheTableField.getJavaType())
							{
								case DATE:
									Date fieldData = (Date) cacheTableField.getData();
									DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									sqlText.append("'" + dateFormat.format(fieldData) + "'");
									break;
								case STRING:
									sqlText.append("'" + cacheTableField.getData() + "'");
									break;
								default:
									sqlText.append(cacheTableField.getData().toString());
									break;
							}
							cacheTableField.setIsChange(false);
							cacheTableField.setIsChangeSaveFlag(false);
							i++;
							isExistUp=true;
						}
					}
					
					if(isExistUp)
					{//如果存在需要更新的数据
						sqlText.append(" WHERE "+this.primaryKeyFieldName+"=");
						ICacheTableField primaryKeyField = cacheTableRow.getField(this.primaryKeyFieldName);//取得主键
						switch (primaryKeyField.getJavaType())
						{
							case DATE:
								Date fieldData = (Date) primaryKeyField.getData();
								DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								sqlText.append("'"+dateFormat.format(fieldData)+"'");
								break;
							case STRING:
								sqlText.append("'"+primaryKeyField.getData()+"'");
								break;
							default:
								sqlText.append(primaryKeyField.getData().toString());
								break;
						}
						sqlTexts.add(sqlText.toString());
					}
				}
			}
			
//			if(!saveChangeSaveFlag)
//			{//如果仅保存指定行其字段“isChangeSaveFlag”为true的字段，就只保存指定的字段，有可能其它也有更改的字段没有进行保存，所以只有saveChangeSaveFlag为false值时才设置当前行数据改变状态为false，才能保证那些没有被保存的数据在下次更新时被保存
//				cacheTableRow.setIsChange(false);
//			}
		}
		return sqlTexts;
	}

	/**
	 * 启动自动更新已更改数据至数据库表的操作
	 * @param period 更新的间隔时间(毫秒)
	 */
	public void startAutoUpdate(int period)
	{
		isAutoUpdate=true;
		cacheDataTableUpdateTask = new CacheDataTableUpdateTask(this);
		timerAutoUpdate = TimerController.timer(cacheDataTableUpdateTask, 0, period);
	}

	/**
	 * 停止自动更新已更改数据至数据库表的操作
	 */
	public void stopAutoUpdate()
	{
		isAutoUpdate=false;
		if (timerAutoUpdate != null)
		{
			timerAutoUpdate.cancel();
			timerAutoUpdate = null;
		}
		if (cacheDataTableUpdateTask != null)
		{
			cacheDataTableUpdateTask.cancel();
			cacheDataTableUpdateTask = null;
		}
	}

	public void handleEvent(Event event)
	{
		switch (event.getType())
		{
			case CacheEvent.ROW_FIELD_DATA_CHANGE:
				lock.writeLock().lock();
				try
				{
					ICacheTableRow cacheTableRow = (ICacheTableRow) event.getCurrentTarget();
					if (!changeRows.contains(cacheTableRow))
					{
						changeRows.add(cacheTableRow);
					}
					boolean saveChangeSaveFlag = (boolean)event.getData();
					if(saveChangeSaveFlag)
					{
						updateToDataByChange(cacheTableRow, saveChangeSaveFlag);
					}
				}
				catch (Exception e)
				{
					Logger.getLogger(CacheDataTable.class.getName()).log(Level.SEVERE, null, e);
				}
				finally
				{
					lock.writeLock().unlock();
				}
				break;
			case CacheEvent.DATA_TO_SAVE:
				updateToDataByChange();
				break;
		}
		super.handleEvent(event);
	}

	public synchronized void dispose()
	{
		stopAutoUpdate();
		super.dispose();
	}
}
