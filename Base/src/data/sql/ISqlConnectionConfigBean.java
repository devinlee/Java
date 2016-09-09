package base.data.sql;

public interface ISqlConnectionConfigBean
{
	/**
	 * 驱动名
	 */
	public String getDriverName();

	/**
	 * @param driverName 驱动名
	 */
	public void setDriverName(String driverName);

	/**
	 * 库地址
	 */
	public String getUrl();

	/**
	 * @param url 库地址
	 */
	public void setUrl(String url);

	/**
	 * 库用户
	 */
	public String getUserName();

	/**
	 * @param userName 库用户
	 */
	public void setUserName(String userName);

	/**
	 * 库密码
	 */
	public String getPassword();

	/**
	 * @param password 库密码
	 */
	public void setPassword(String password);

	/**
	 * 池名称
	 */
	public String getPoolName();

	/**
	 * @param poolName 池名称
	 */
	public void setPoolName(String poolName);

	/**
	 *  初始化连接数 
	 */
	public int getInitConnections();

	/**
	 * @param initConnections  初始化连接数
	 */
	public void setInitConnections(int initConnections);

	/**
	 * 池最大空闲连接数
	 */
	public int getMaxFreeConnections();

	/**
	 * @param maxFreeConnections 池最大空闲连接数
	 */
	public void setMaxFreeConnections(int maxFreeConnections);

	/**
	 * 池最大活动连接数
	 */
	public int getMaxActiveConnections();

	/**
	 * @param maxActiveConnections 池最大活动连接数
	 */
	public void setMaxActiveConnections(int maxActiveConnections);

	/**
	 * 等待重连时间
	 */
	public long getConnectionTimeOut();

	/**
	 * @param connectionTimeOut 等待重连时间
	 */
	public void setConnectionTimeOut(long connectionTimeOut);
}