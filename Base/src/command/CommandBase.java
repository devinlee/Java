package base.command;

public class CommandBase implements ICommandBase
{
	/**
	 * 命令ID
	 */
	private volatile long id;

	/**
	 * 命令ID
	 * @param id 要设置的 id
	 */
	public void setId(long id)
	{
		this.id = id;
	}

	/**
	 * 命令ID
	 * @return id
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * 命令目标对象
	 */
	private volatile Object targetObject;

	/**
	 * 命令目标对象
	 * @return targetObject
	 */
	public Object getTargetObject()
	{
		return targetObject;
	}

	/**
	 * 命令目标对象
	 * @param targetObject 要设置的 targetObject
	 */
	public void setTargetObject(Object targetObject)
	{
		this.targetObject = targetObject;
	}

	/**
	 * 命令
	 */
	public CommandBase()
	{
	}
	
	/**
	 * 初始化
	 */
	public void init(Object targetObject)
	{
		this.targetObject = targetObject;
	}
}
