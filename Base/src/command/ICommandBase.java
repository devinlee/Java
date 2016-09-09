package base.command;

public interface ICommandBase
{
	/**
	 * 命令ID
	 * @param id 要设置的 id
	 */
	public void setId(long id);

	/**
	 * 命令ID
	 * @return id
	 */
	public long getId();

	/**
	 * 命令目标对象
	 * @return targetObject
	 */
	public Object getTargetObject();

	/**
	 * 命令目标对象
	 * @param targetObject 要设置的 targetObject
	 */
	public void setTargetObject(Object targetObject);

	/**
	 * 初始化
	 */
	public void init(Object targetObject);
}
