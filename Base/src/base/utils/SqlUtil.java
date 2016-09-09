package base.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import base.types.JavaType;

public class SqlUtil
{
	/**
	 * 取得ResultSet的值
	 * @param resultSet ResultSet
	 * @param columnIndex 列索引
	 * @param javaType JavaType
	 * @return 如果成功读取返回读取到的值，否则返回null
	 * @throws SQLException
	 */
	public static Object resultSetGet(ResultSet resultSet, int columnIndex, JavaType javaType) throws SQLException
	{
		Object resultSetData = null;
		switch (javaType)
		{
			case BOOLEAN:
				resultSetData = resultSet.getBoolean(columnIndex);
				break;
			case BYTE:
				resultSetData = resultSet.getByte(columnIndex);
				break;
			case SHORT:
				resultSetData = resultSet.getShort(columnIndex);
				break;
			case INTEGER:
				resultSetData = resultSet.getInt(columnIndex);
				break;
			case LONG:
				resultSetData = resultSet.getLong(columnIndex);
				break;
			case FLOAT:
				resultSetData = resultSet.getFloat(columnIndex);
				break;
			case DOUBLE:
				resultSetData = resultSet.getDouble(columnIndex);
				break;
			case CHAR:
				resultSetData = resultSet.getByte(columnIndex);
				break;
			case STRING:
				resultSetData = resultSet.getString(columnIndex);
				break;
			case BIGDECIMAL:
				resultSetData = resultSet.getDouble(columnIndex);
				break;
			case DATE:
				resultSetData = resultSet.getDate(columnIndex);
				break;
			default:
				resultSetData = resultSet.getObject(columnIndex);
				break;
		}
		return resultSetData;
	}
	
	/**
	 * 判断查询结果集中是否存在某列
	 * @param rs 查询结果集
	 * @param columnName 列名
	 * @return true=存在; false=不存在
	 */
	public static boolean isExistColumn(ResultSet resultSet, String columnName) {
	    try {
	        if (resultSet.findColumn(columnName) > 0 ) {
	            return true;
	        } 
	    }
	    catch (SQLException e) {
	        return false;
	    }
	    return false;
	}
}