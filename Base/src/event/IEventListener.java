package base.event;

/**
 * 事件帧听接口
 * @author Devin
 *
 */
public interface IEventListener
{
	/**
	 * 事件处理
	 * @param event 事件对象
	 */
	public void handleEvent(Event event);
}
