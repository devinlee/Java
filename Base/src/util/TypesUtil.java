package base.util;

import java.sql.Types;

import base.types.JavaType;

public class TypesUtil
{
	/**
	 * 根据Sql Types类型转为自定义的JavaType
	 * @param sqlType Sql Types.xxx
	 * @return 对应的自定义JavaType
	 */
	public static JavaType sqlTypeToJavaType(int sqlType)
	{
		JavaType javaType = null;
		switch (sqlType)
		{
			case Types.NUMERIC:
			case Types.DECIMAL:
				javaType = JavaType.BIGDECIMAL;
				break;
			case Types.BIT:
				javaType = JavaType.BOOLEAN;
				break;
			case Types.TINYINT:
				javaType = JavaType.BYTE;
				break;
			case Types.SMALLINT:
				javaType = JavaType.SHORT;
				break;
			case Types.INTEGER:
				javaType = JavaType.INTEGER;
				break;
			case Types.BIGINT:
				javaType = JavaType.LONG;
				break;
			case Types.REAL:
				javaType = JavaType.FLOAT;
				break;
			case Types.FLOAT:
			case Types.DOUBLE:
				javaType = JavaType.DOUBLE;
				break;
			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
				javaType = JavaType.BYTES;
				break;
			case Types.CHAR:
			case Types.NCHAR:
				javaType = JavaType.CHAR;
				break;
			case Types.NVARCHAR:
			case Types.VARCHAR:
				javaType = JavaType.STRING;
				break;
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				javaType = JavaType.DATE;
				break;
		}
		return javaType;
	}

	/**
	 * 根据Sql Types类型转为自定义的JavaType
	 * @param sqlType Sql Types.xxx
	 * @return 对应的自定义JavaType
	 */
	public static JavaType sqlTypeToJavaType(String sqlType)
	{
		sqlType = sqlType.toLowerCase();
		JavaType javaType = null;
		switch (sqlType)
		{
		// case Types.NUMERIC:
		// case Types.DECIMAL:
		// javaType=JavaType.BIGDECIMAL;
		// break;
			case "java.lang.boolean":
				javaType = JavaType.BOOLEAN;
				break;
			case "java.lang.byte":
				javaType = JavaType.BYTE;
				break;
			case "java.lang.short":
				javaType = JavaType.SHORT;
				break;
			case "java.lang.integer":
				javaType = JavaType.INTEGER;
				break;
			case "java.lang.long":
				javaType = JavaType.LONG;
				break;
			case "java.lang.float":
				javaType = JavaType.FLOAT;
				break;
			case "java.lang.double":
				javaType = JavaType.DOUBLE;
				break;
			// case Types.BINARY:
			// case Types.VARBINARY:
			// case Types.LONGVARBINARY:
			// javaType=JavaType.BYTES;
			// break;
			// case Types.CHAR:
			// case Types.NCHAR:
			// javaType=JavaType.CHAR;
			// break;
			case "java.lang.string":
				javaType = JavaType.STRING;
				break;
			case "java.lang.date":
				javaType = JavaType.DATE;
				break;
			case "java.sql.timestamp":
				javaType = JavaType.DATE;
				break;
		}
		return javaType;
	}

	/**
	 * 根据Java Class类型转为自定义的JavaType
	 * @param javaClassType Java类的名称简称
	 * @return 对应的自定义JavaType
	 */
	public static JavaType javaClassTypeToJavaType(String javaClassType)
	{
		JavaType javaType = null;
		javaClassType = javaClassType.toLowerCase();
		switch (javaClassType)
		{
			case "string":
				javaType = JavaType.STRING;
				break;
			case "boolean":
				javaType = JavaType.BOOLEAN;
				break;
			case "byte":
				javaType = JavaType.BYTE;
				break;
			case "short":
				javaType = JavaType.SHORT;
				break;
			case "integer":
				javaType = JavaType.INTEGER;
				break;
			case "long":
				javaType = JavaType.LONG;
				break;
			case "float":
				javaType = JavaType.FLOAT;
				break;
			case "double":
				javaType = JavaType.DOUBLE;
				break;
			case "date":
				javaType = JavaType.DATE;
				break;
		}
		return javaType;
	}
	
	/**
	 * 根据JavaType类型取得其对应的默认值
	 * @param javaType JavaType
	 * @return 对应的默认值
	 */
	public static Object getJavaTypeDefaultValue(JavaType javaType)
	{
		Object value=null;
		switch(javaType)
		{
			case BOOLEAN:
				value=false;
				break;
			case BYTE:
			case SHORT:
			case INTEGER:
			case LONG:
			case FLOAT:
			case DOUBLE:
			case CHAR:
				value=0;
				break;
			case BYTES:
			case STRING:
			case BIGDECIMAL:
			case DATE:
			default:
				value=null;
				break;
		}
		return value;
	}
}
