package base.data.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.Base;

/**
 * 数据库连接池
 * @author Devin
 *
 */
public class SqlConnectionPool
{
	/**
	 * 线程读写锁
	 */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * 写锁条件
	 */
	private final Condition writeCondition = lock.writeLock().newCondition();

	/**
	 * 池是否可用
	 */
	private volatile boolean isAvailable;

	public boolean isAvailable()
	{
		return isAvailable;
	}

	/**
	 * 连接配置
	 */
	private ISqlConnectionConfigBean connectionConfigBean;

	/**
	 * 空闲连接
	 */
	private BlockingQueue<ISqlConnection> freeConnections = null;

	/**
	 * 活动连接
	 */
	private BlockingQueue<ISqlConnection> activeConnections = null;

	/**
	 * 实例新连接池
	 * @param connectionConfigBean 连接池配置
	 */
	public SqlConnectionPool(ISqlConnectionConfigBean connectionConfigBean)
	{
		this.connectionConfigBean = connectionConfigBean;
		freeConnections = new ArrayBlockingQueue<ISqlConnection>(connectionConfigBean.getMaxFreeConnections());
		activeConnections = new ArrayBlockingQueue<ISqlConnection>(connectionConfigBean.getMaxActiveConnections());
		init();
	}

	/**
	 * 初始化
	 */
	private void init()
	{
		try
		{
			for (int i = 0; i < connectionConfigBean.getInitConnections(); i++)
			{
				ISqlConnection sqlConnection = createConnection();
				if (sqlConnection != null)
				{
					if (!freeConnections.offer(sqlConnection))
					{
						closeConnection(sqlConnection);
					}
				}
			}
			isAvailable = true;
		}
		catch (ClassNotFoundException | SQLException e)
		{
			Logger.getLogger(SqlConnectionPool.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 创建连接
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private ISqlConnection createConnection() throws ClassNotFoundException, SQLException
	{
		ISqlConnection sqlConnection = null;
		Connection conn = null;
		try
		{
			conn = DriverManager.getConnection(connectionConfigBean.getUrl(), connectionConfigBean.getUserName(), connectionConfigBean.getPassword());
			sqlConnection = Base.newClass(SqlConnection.class);
			sqlConnection.init(conn);
		}
		catch (Exception e)
		{
			if (conn != null)
			{
				conn.close();
				conn = null;
			}
			closeConnection(sqlConnection);
			Logger.getLogger(SqlConnectionPool.class.getName()).log(Level.SEVERE, null, e);
		}
		return sqlConnection;
	}

	/**
	 * 取得连接
	 * @return
	 */
	public ISqlConnection getConnection()
	{
		ISqlConnection sqlConnection = null;
		lock.writeLock().lock();
		try
		{
			if (activeConnections.size() < connectionConfigBean.getMaxActiveConnections() && freeConnections.size() > 0)
			{// 当前活动连接数小于最大活动连接数时
				sqlConnection = freeConnections.poll();// 从空闲连接集里取到一个连接
				if (isValid(sqlConnection))
				{
					activeConnections.add(sqlConnection);
				}
				else
				{
					closeConnection(sqlConnection);
					sqlConnection = createConnection();
					activeConnections.add(sqlConnection);
				}
				return sqlConnection;
			}
			else
			{// 等待，直到有空闲连接
				writeCondition.await(connectionConfigBean.getConnectionTimeOut(), TimeUnit.MILLISECONDS);
				return getConnection();
			}
		}
		catch (ClassNotFoundException | SQLException | InterruptedException e)
		{
			Logger.getLogger(SqlConnectionPool.class.getName()).log(Level.SEVERE, null, e);
			return  null;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 回收连接
	 * @param sqlConnection 要回收的sql连接
	 * @throws SQLException
	 */
	public void releaseConnection(ISqlConnection sqlConnection)
	{
		lock.writeLock().lock();
		try
		{
			activeConnections.remove(sqlConnection);
			if (isValid(sqlConnection))
			{
				// 回收至空闲连接集
				if (!freeConnections.offer(sqlConnection))
				{
					closeConnection(sqlConnection);
				}
			}
			else
			{
				closeConnection(sqlConnection);
			}
			writeCondition.signalAll();
		}
		catch (Exception e)
		{
			Logger.getLogger(SqlConnectionPool.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 关闭连接
	 * @param conn
	 */
	private void closeConnection(ISqlConnection sqlConnection)
	{
		if (sqlConnection == null)return;
		sqlConnection.close();
		sqlConnection=null;
	}

	/**
	 * 检查连接是否可用
	 * @param sqlConnection 数据连接
	 * @return
	 */
	private boolean isValid(ISqlConnection sqlConnection)
	{
		if (sqlConnection == null || !sqlConnection.isAvailable())
		{
			return false;
		}
		return true;
	}

	/**
	 * 取得连接总数
	 * @return
	 */
	public int getConnectionsCount()
	{
		lock.readLock().lock();
		try
		{
			return freeConnections.size() + activeConnections.size();
		}
		catch (Exception e)
		{
			Logger.getLogger(SqlConnectionPool.class.getName()).log(Level.SEVERE, null, e);
			return 0;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 关闭当前连接池(会关闭池中所有的连接)
	 * @throws SQLException
	 */
	public void closeConnectionPool()
	{
		lock.writeLock().lock();
		try
		{
			if (freeConnections != null)
			{
				for (ISqlConnection sqlConnection : freeConnections)
				{
					closeConnection(sqlConnection);
				}
				freeConnections.clear();
			}
			if (activeConnections != null)
			{
				for (ISqlConnection sqlConnection : activeConnections)
				{
					closeConnection(sqlConnection);
				}
				activeConnections.clear();
			}
		}
		finally
		{
			lock.writeLock().unlock();
		}
		isAvailable = false;
	}
}
