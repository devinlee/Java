package base.data.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public interface ISqlConnection
{
	/**
	 * 数据连接
	 * @param sqlConnectionPool 来源数据连接池
	 * @param conn 数据库物理连接
	 */
	public void init(Connection conn);

	/**
	 * 连接是否可用
	 * @return
	 */
	public boolean isAvailable();

	/**
	 * 执行SQL
	 * @param sql Sql语句
	 * @param sqlConnectionParameter 参数值集
	 * @return ResultSet
	 */
	public ResultSet executeQuery(String sql, ISqlConnectionParameter sqlConnectionParameter);

	/**
	 * 执行 SQL 语句
	 * 主要用于执行非查询语句
	 * @param sql SQL 语句
	 * @param sqlConnectionParameter 参数值集
	 * @return 正常执行返回true,否则返回false
	 */
	public boolean executeUpdate(String sql, ISqlConnectionParameter sqlConnectionParameter);

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
	public boolean executeUpdate(Vector<String> sqls);

	/**
	 * 关闭库物理连接
	 * @throws SQLException
	 */
	public void close();
}