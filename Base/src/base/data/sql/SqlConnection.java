package base.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.util.DateUtil;

import com.sun.rowset.CachedRowSetImpl;

public class SqlConnection implements ISqlConnection
{
	/**
	 * 线程读写锁
	 */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * 库连接
	 */
	private Connection connection = null;

	/**
	 * 最大间隔测试时间为6小时
	 */
	private int maxTestTimeInterval = 6 * 60 * 60 * 1000;

	/**
	 * 创建测试连接时间
	 */
	private long createTestTime=0;

	/**
	 * 数据连接
	 */
	public SqlConnection()
	{

	}
	/**
	 * 数据连接
	 * @param sqlConnectionPool 来源数据连接池
	 * @param conn 数据库物理连接
	 */
	public void init(Connection conn)
	{
		connection = conn;
		createTestTime = DateUtil.getCurrentTimeMillis();
	}

	/**
	 * 连接是否可用
	 * @return
	 */
	public boolean isAvailable()
	{
		boolean available = false;
		lock.readLock().lock();
		try
		{
			if (connection != null && !connection.isClosed() && testConnection())
			{
				available = true;
			}
		}
		catch (SQLException e)
		{
			Logger.getLogger(SqlConnection.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.readLock().unlock();
		}
		return available;
	}

	/**
	 * 测试连接是否正常
	 * @return
	 */
	private boolean testConnection()
	{
		if(DateUtil.getCurrentTimeMillis() - createTestTime<maxTestTimeInterval)
		{//如果创建的时间到现在没有超过最大间隔测试时间
			return true;
		}

		try
		{
			//执行表测试
			Statement stmt = connection.createStatement();
			if(stmt.execute("SELECT COUNT(*) FROM test_connection"))
			{
				stmt.close();
				stmt=null;
			}
			if(stmt!=null)
			{
				stmt.close();
				stmt=null;
			}
		}
		catch (SQLException e)
		{//如果执行异常，说明该连接已无效
			System.out.println("测试连接不正常");
			return false;
		}
		return true;
	}

	/**
	 * 执行SQL
	 * @param sql Sql语句
	 * @param sqlConnectionParameter 参数值集
	 * @return ResultSet
	 */
	public ResultSet executeQuery(String sql, ISqlConnectionParameter sqlConnectionParameter)
	{
		CachedRowSetImpl crs = null;
		lock.writeLock().lock();
		try
		{
			PreparedStatement preparedStatement = getPreparedStatement(sql, sqlConnectionParameter);
			if (preparedStatement != null)
			{
				crs = new CachedRowSetImpl();
				ResultSet rs = preparedStatement.executeQuery();
				crs.populate(rs);
				preparedStatement.close();
				preparedStatement = null;
				rs.close();
				rs = null;
			}

			if (sqlConnectionParameter != null)
			{
				sqlConnectionParameter.dispose();
				sqlConnectionParameter = null;
			}
		}
		catch (SQLException e)
		{
			Logger.getLogger(SqlConnection.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
		return crs;
	}

	/**
	 * 执行 SQL 语句
	 * 主要用于执行非查询语句
	 * @param sql SQL 语句
	 * @param sqlConnectionParameter 参数值集
	 * @return 正常执行返回true,否则返回false
	 */
	public boolean executeUpdate(String sql, ISqlConnectionParameter sqlConnectionParameter)
	{
		boolean result = false;
		lock.writeLock().lock();
		try
		{
			PreparedStatement preparedStatement = getPreparedStatement(sql, sqlConnectionParameter);
			if (preparedStatement != null)
			{
				result = preparedStatement.executeUpdate() >= 1;
				preparedStatement.close();
				preparedStatement = null;
			}

			if (sqlConnectionParameter != null)
			{
				sqlConnectionParameter.dispose();
				sqlConnectionParameter = null;
			}
		}
		catch (SQLException e)
		{
			Logger.getLogger(SqlConnection.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
		return result;
	}

	/**
	 * 执行 SQL 语句
	 * 主要用于执行非查询语句
	 * @param sqls SQL 语句列表，
	 * 比如：
	 * Vector<String> sqls=new Vector<String>();
	 *sqls.add("INSERT INTO 表名 (字段1, 字段1, 字段2, ...) values(字段1值, 字段2值, 字段3值, ...)");
	 *sqls.add("UPDATE 表名 set 字段1=字段1值, 字段2=字段2值, 字段3=字段3值 where 条件");
	 *sqls.add("INSERT INTO 表名 (字段1, 字段1, 字段2, ...) values(字段1值, 字段2值, 字段3值, ...)");
	 * @return 全部正常执行返回true,否则返回false，如果存在一个执行失败，会执行回滚事务
	 */
	public boolean executeUpdate(Vector<String> sqls)
	{
		boolean result = true;
		boolean oldAutoCommit = true;
		lock.writeLock().lock();
		try
		{
			if (sqls == null || sqls.size() <= 0)
				return false;
			oldAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			Statement statement = connection.createStatement();
			if (statement != null)
			{
				for (String sql : sqls)
				{
					statement.addBatch(sql);
				}
				int[] results = statement.executeBatch();
				for (int resultValue : results)
				{
					if (resultValue <= 0)
					{
						result = false;
						break;
					}
				}
			}

			if (result)
			{
				connection.commit();
			}
			else
			{
				connection.rollback();
			}
			connection.setAutoCommit(oldAutoCommit);
			if (statement != null)
			{
				statement.close();
				statement=null;
			}
		}
		catch (SQLException e)
		{
			String excSqls = "";
			for (String sql : sqls)
			{
				excSqls += sql + "\r\n";
			}

			try
			{
				connection.rollback();
				connection.setAutoCommit(oldAutoCommit);
			}
			catch (SQLException e2)
			{
				Logger.getLogger(SqlConnection.class.getName()).log(Level.SEVERE, "事务回滚时出错，执行SQL语句：\r\n" + excSqls, e2);
			}
			Logger.getLogger(SqlConnection.class.getName()).log(Level.SEVERE, "事务执行完成回滚。回滚原由为执行SQL语句出现错误：\r\n" + excSqls, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
		return result;
	}

	/**
	 * 取得PreparedStatement
	 * @param sql Sql语句
	 * @param sqlConnectionParameter 参数值集
	 * @return
	 */
	private PreparedStatement getPreparedStatement(String sql, ISqlConnectionParameter sqlConnectionParameter)
	{
		PreparedStatement preparedStatement = null;
		try
		{
			if (isAvailable())
			{
				if (sqlConnectionParameter != null)
				{
					ConcurrentHashMap<String, Object> parameters = sqlConnectionParameter.getParameters();
					Object[] values = new Object[parameters.size()];
					Pattern pattern = Pattern.compile("\\{(.+?)\\}");
					Matcher matcher = pattern.matcher(sql);
					int i = 0;
					while (matcher.find())
					{
						String parKey = matcher.group();
						if (!parameters.containsKey(parKey))
						{
							Logger.getLogger(SqlConnection.class.getName()).log(Level.SEVERE, null, new Exception("执行SQL语句“" + sql + "”时，所提供的值参数不匹配。"));
							return null;
						}
						else
						{
							sql = sql.replace(parKey, "?");
							values[i] = parameters.get(parKey);
						}
						i++;
					}

					preparedStatement = connection.prepareStatement(sql);
					int pi = 1;
					for (Object value : values)
					{
						String simpleName = value.getClass().getSimpleName().toLowerCase();
						switch (simpleName)
						{
							case "string":
								preparedStatement.setString(pi, (String) value);
								break;
							case "boolean":
								preparedStatement.setBoolean(pi, (boolean) value);
								break;
							case "byte":
								preparedStatement.setByte(pi, (byte) value);
								break;
							case "short":
								preparedStatement.setShort(pi, (short) value);
								break;
							case "integer":
								preparedStatement.setInt(pi, (int) value);
								break;
							case "long":
								preparedStatement.setLong(pi, (long) value);
								break;
							case "float":
								preparedStatement.setFloat(pi, (float) value);
								break;
							case "double":
								preparedStatement.setDouble(pi, (double) value);
								break;
							case "date":
								java.util.Date jDate = (java.util.Date) value;
								java.sql.Date sDate = new java.sql.Date(jDate.getTime());
								preparedStatement.setDate(pi, sDate);
								break;
						}
						pi++;
					}
				}
				else
				{
					preparedStatement = connection.prepareStatement(sql);
				}
			}
			else
			{
				Logger.getLogger(SqlConnection.class.getName()).log(Level.SEVERE, null, new Exception("执行SQL语句“" + sql + "”时，遇到数据库连接无效。"));
			}
		}
		catch (SQLException e)
		{
			Logger.getLogger(SqlConnection.class.getName()).log(Level.SEVERE, null, e);
		}
		return preparedStatement;
	}

	/**
	 * 关闭库物理连接
	 * @throws SQLException
	 */
	public synchronized void close()
	{
		try
		{
//			if(!connection.isClosed())
//			{
				connection.close();
//			}
			connection = null;
		}
		catch (SQLException e)
		{
			Logger.getLogger(SqlConnection.class.getName()).log(Level.SEVERE, null, e);
		}
	}
}
