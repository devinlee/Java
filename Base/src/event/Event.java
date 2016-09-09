package base.event;

/**
 * 事件基类
 * @author Devin
 *
 */
public class Event
{
	/**
	 * 事件类型
	 */
	protected String type;

	/**
	 * 事件类型
	 * @return types 事件类型
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * 处理事件对象的对象
	 */
	protected Object currentTarget;

	/**
	 * 处理事件对象的对象
	 * @return currentTarget 处理事件对象的对象
	 */
	public Object getCurrentTarget()
	{
		return currentTarget;
	}

	/**
	 * 处理事件对象的对象
	 * @param currentTarget 要设置的 currentTarget
	 */
	public void setCurrentTarget(Object currentTarget)
	{
		this.currentTarget = currentTarget;
	}

	/**
	 * 事件传递的数据
	 */
	protected Object data;

	/**
	 * 事件传递的数据
	 * @return data 事件传递的数据
	 */
	public Object getData()
	{
		return data;
	}

	/**
	 * 事件
	 * @param types 事件类型
	 */
	public Event(String type)
	{
		this.type = type;
	}
	
	/// <summary>
    /// 是否停止后续事件的执行
    /// </summary>
    public Boolean isStopImmediateEvent = false;

	/**
	 * 事件
	 * @param types 事件类型
	 * @param data 事件传递的数据
	 */
	public Event(String type, Object data)
	{
		this.type = type;
		this.data = data;
	}
	
	public void reset(String type, Object data)
	{
		this.type = type;
		this.data = data;
	}
}
