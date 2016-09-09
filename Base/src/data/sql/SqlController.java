package base.data.sql;

import java.sql.ResultSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 数据库连接池控制器
 * @author Devin
 *
 */
public class SqlController
{
	/**
	 * 池列表容器
	 */
	public final ConcurrentHashMap<String, SqlConnectionPool> pools = new ConcurrentHashMap<String, SqlConnectionPool>();

	/**
	 * 创建数据连接池
	 * @param sqlConnectionConfig 配置数据
	 */
	public SqlConnectionPool createPool(ISqlConnectionConfig sqlConnectionConfig)
	{
		SqlConnectionPool sqlConnectionPool = null;
		try
		{
			sqlConnectionPool = new SqlConnectionPool(sqlConnectionConfig.getSqlConnectionConfigBean());
			pools.put(sqlConnectionConfig.getSqlConnectionConfigBean().getPoolName(), sqlConnectionPool);
		}
		catch (Exception e)
		{
			Logger.getLogger(SqlController.class.getName()).log(Level.SEVERE, null, e);
		}
		return sqlConnectionPool;
	}

	/**
	 * 取得数据连接池
	 * @param poolName 数据库连接池名
	 * @return
	 */
	public SqlConnectionPool getPool(String poolName)
	{
		SqlConnectionPool sqlConnectionPool = null;
		try
		{
			if (pools.size() > 0)
			{
				sqlConnectionPool = pools.get(poolName);
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SqlController.class.getName()).log(Level.SEVERE, null, e);
		}
		return sqlConnectionPool != null && sqlConnectionPool.isAvailable() ? sqlConnectionPool : null;
	}

	/**
	 * 取得一个连接
	 * @param poolName 所在的数据库连接池名
	 * @return
	 */
	public ISqlConnection getConnection(String poolName)
	{
		ISqlConnection sqlConnection = null;
		try
		{
			if (pools.size() > 0 && pools.containsKey(poolName))
			{
				sqlConnection = getPool(poolName).getConnection();
				if(sqlConnection==null)
				{
					Logger.getLogger(SqlController.class.getName()).log(Level.SEVERE, null, new Exception("数据库连接为空： " + poolName));
				}
			}
			else
			{
				Logger.getLogger(SqlController.class.getName()).log(Level.SEVERE, null, new Exception("未找到可用的数据库连接池 " + poolName));
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SqlController.class.getName()).log(Level.SEVERE, null, e);
		}
		return sqlConnection;
	}

	/**
	 * 关闭数据库连接池中一个连接
	 * @param poolName 数据库连接池名
	 * @param conn 连接对象
	 */
	public void releaseConnection(String poolName, ISqlConnection sqlConnection)
	{
		SqlConnectionPool sqlConnectionPool = getPool(poolName);
		sqlConnectionPool.releaseConnection(sqlConnection);
	}

	/**
	 * 关闭数据库连接池
	 * @param poolName 数据库连接池名
	 */
	public void closePool(String poolName)
	{
		SqlConnectionPool sqlConnectionPool = getPool(poolName);
		if (sqlConnectionPool != null)
		{
			sqlConnectionPool.closeConnectionPool();
			pools.remove(poolName);
		}
	}

	/**
	 * 执行SQL
	 * @param poolName Sql连接池名称
	 * @param sql Sql语句
	 * @return ResultSet
	 */
	public ResultSet executeQuery(String poolName, String sql)
	{
		return executeQuery(poolName, sql, null);
	}

	/**
	 * 执行SQL
	 * @param poolName Sql连接池名称
	 * @param sql Sql语句
	 * @param sqlConnectionParameter 值集
	 * @return ResultSet
	 */
	public ResultSet executeQuery(String poolName, String sql, ISqlConnectionParameter sqlConnectionParameter)
	{
		ResultSet rs = null;
		try
		{
			ISqlConnection sqlConnection = getConnection(poolName);
			if(sqlConnection!=null)
			{
				rs = sqlConnection.executeQuery(sql, sqlConnectionParameter);
				releaseConnection(poolName, sqlConnection);
			}
			else
			{
				Logger.getLogger(SqlController.class.getName()).log(Level.SEVERE, null, new Exception("在 " + poolName + " 池中executeQuery执行Sql语句 " + sql + " 时遇到数据库连接为空。"));
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SqlController.class.getName()).log(Level.SEVERE, null, new Exception("在 " + poolName + " 池中executeQuery执行Sql语句 " + sql + " 遇到错误。", e));
		}
		return rs;
	}

	/**
	 * 执行SQL
	 * @param poolName Sql连接池名称
	 * @param sql Sql语句
	 * @param sqlConnectionParameter 值集
	 * @return boolean 执行结果，执行成功返回true,否则返回false
	 */
	public boolean executeUpdate(String poolName, String sql)
	{
		return executeUpdate(poolName, sql, null);
	}

	/**
	 * 执行SQL
	 * @param poolName Sql连接池名称
	 * @param sql Sql语句
	 * @param sqlConnectionParameter 值集
	 * @return boolean 执行结果，执行成功返回true,否则返回false
	 */
	public boolean executeUpdate(String poolName, String sql, ISqlConnectionParameter sqlConnectionParameter)
	{
		boolean result = false;
		try
		{
			ISqlConnection sqlConnection = getConnection(poolName);
			if(sqlConnection!=null)
			{
				result = sqlConnection.executeUpdate(sql, sqlConnectionParameter);
				releaseConnection(poolName, sqlConnection);
			}
			else
			{
				Logger.getLogger(SqlController.class.getName()).log(Level.SEVERE, null, new Exception("在 " + poolName + " 池中executeUpdate执行Sql语句 " + sql + " 时遇到数据库连接为空。"));
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SqlController.class.getName()).log(Level.SEVERE, null, new Exception("在 " + poolName + " 池中executeUpdate执行Sql语句 " + sql + " 遇到错误。", e));
		}
		return result;
	}

	/**
	 * 执行 SQL 语句
	 * 主要用于执行非查询语句
	 * @param sqls SQL 语句列表，
	 * 比如:
	 * Vector<String> sqls=new Vector<String>();
	 *sqls.add("INSERT INTO 表名 (字段1, 字段1, 字段2, ...) values(字段1值, 字段2值, 字段3值, ...)");
	 *sqls.add("UPDATE 表名 set 字段1=字段1值, 字段2=字段2值, 字段3=字段3值 where 条件");
	 *sqls.add("INSERT INTO 表名 (字段1, 字段1, 字段2, ...) values(字段1值, 字段2值, 字段3值, ...)");
	 * @return 全部正常执行返回true,否则返回false，如果存在一个执行失败，会执行回滚事务
	 */
	public boolean executeUpdate(String poolName, Vector<String> sqls)
	{
		boolean result = false;
		try
		{
			ISqlConnection sqlConnection = getConnection(poolName);
			if(sqlConnection!=null)
			{
				result = sqlConnection.executeUpdate(sqls);
				releaseConnection(poolName, sqlConnection);
			}
			else
			{
				String excSqls = "";
				for (String sql : sqls)
				{
					excSqls += sql + "\r\n";
				}
				Logger.getLogger(SqlController.class.getName()).log(Level.SEVERE, null, new Exception("在 " + poolName + " 池中executeUpdate执行Sql语句"+excSqls+"遇到数据库连接为空。"));
			}
		}
		catch (Exception e)
		{
			String excSqls = "";
			for (String sql : sqls)
			{
				excSqls += sql + "\r\n";
			}
			Logger.getLogger(SqlController.class.getName()).log(Level.SEVERE, null, new Exception("在 " + poolName + " 池中executeUpdate执行Sql语句 " + excSqls + " 遇到错误。", e));
		}
		return result;
	}
	
	/**
	 * 取得指定池的有效连接总数，返回-1则表示执行遇到错误
	 * @return
	 */
	public int getConnectionCount(String poolName)
	{
		try
		{
			SqlConnectionPool sqlConnectionPool = getPool(poolName);
			return sqlConnectionPool.getConnectionsCount();
		}
		catch (Exception e)
		{
			return -1;
		}
	}
}
