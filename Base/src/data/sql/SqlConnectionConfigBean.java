package base.data.sql;

/**
 * 数据库连接池配置实体
 * @author Devin
 *
 */
public class SqlConnectionConfigBean implements ISqlConnectionConfigBean
{
	/**
	 * 驱动名
	 */
	private volatile String driverName;

	/**
	 * 驱动名
	 */
	public String getDriverName()
	{
		return driverName;
	}

	/**
	 * @param driverName 驱动名
	 */
	public void setDriverName(String driverName)
	{
		this.driverName = driverName;
	}

	/**
	 *库地址 
	 */
	private volatile String url;

	/**
	 * 库地址
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * @param url 库地址
	 */
	public void setUrl(String url)
	{
		this.url = url;
	}

	/**
	 * 库用户
	 */
	private volatile String userName;

	/**
	 * 库用户
	 */
	public String getUserName()
	{
		return userName;
	}

	/**
	 * @param userName 库用户
	 */
	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	/**
	 * 库密码
	 */
	private volatile String password;

	/**
	 * 库密码
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password 库密码
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * 池名称
	 */
	private volatile String poolName;

	/**
	 * 池名称
	 */
	public String getPoolName()
	{
		return poolName;
	}

	/**
	 * @param poolName 池名称
	 */
	public void setPoolName(String poolName)
	{
		this.poolName = poolName;
	}

	/**
	 *  初始化连接数 
	 */
	private volatile int initConnections = 5;

	/**
	 *  初始化连接数 
	 */
	public int getInitConnections()
	{
		return initConnections;
	}

	/**
	 * @param initConnections  初始化连接数
	 */
	public void setInitConnections(int initConnections)
	{
		this.initConnections = initConnections;
	}

	/**
	 * 池最大空闲连接数
	 */
	private volatile int maxFreeConnections = 20;

	/**
	 * 池最大空闲连接数
	 */
	public int getMaxFreeConnections()
	{
		return maxFreeConnections;
	}

	/**
	 * @param maxFreeConnections 池最大空闲连接数
	 */
	public void setMaxFreeConnections(int maxFreeConnections)
	{
		this.maxFreeConnections = maxFreeConnections;
	}

	/**
	 * 池最大活动连接数
	 */
	private volatile int maxActiveConnections = 20;

	/**
	 * 池最大活动连接数
	 */
	public int getMaxActiveConnections()
	{
		return maxActiveConnections;
	}

	/**
	 * @param maxActiveConnections 池最大活动连接数
	 */
	public void setMaxActiveConnections(int maxActiveConnections)
	{
		this.maxActiveConnections = maxActiveConnections;
	}

	/**
	* 等待重连时间
	*/
	private volatile long connectionTimeOut = 1000;

	/**
	 * 等待重连时间
	 */
	public long getConnectionTimeOut()
	{
		return connectionTimeOut;
	}

	/**
	 * @param connectionTimeOut 等待重连时间
	 */
	public void setConnectionTimeOut(long connectionTimeOut)
	{
		this.connectionTimeOut = connectionTimeOut;
	}
}
