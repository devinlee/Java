package base.event;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 事件派发器
 * @author Devin
 *
 */
public class EventDispatcher
{
	/**
	 * 线程读写锁
	 */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	/**
	 * 装载事件列表的HashMap
	 */
	private ConcurrentHashMap<String, LinkedBlockingQueue<EventHandler>> events = new ConcurrentHashMap<String, LinkedBlockingQueue<EventHandler>>();

	/**
	 * 类是否已销毁释放
	 */
	protected boolean isDispose=false;
	/**
	 * 类是否已销毁释放
	 */
	public boolean getIsDispose()
	{
		return isDispose;
	}

	/**
	 * 事件派发器
	 */
	public EventDispatcher()
	{
	}
	
	/**
	 * 事件派发
	 * 这个方法重用Event的对象
	 * @param type 事件类型
	 */
    public void dispatchEventWith(String type)
    {
    	dispatchEventWith(type,null);
    }
	
    /**
	 * 事件派发
	 * 这个方法重用Event的对象
	 * @param type 事件类型
	 * @param data 事件传递的数据
	 */
    public void dispatchEventWith(String type,Object data)
    {
    	try
		{
	        if (events == null || !events.containsKey(type)) return;
	        Event e = createEvent(type, data);
	        dispatchEvent(e);
	        e.reset("", null);
	        _eventObjlist.put(e);
		}
    	catch (Exception e)
		{
			Logger.getLogger(EventDispatcher.class.getName()).log(Level.SEVERE, "事件派发时发生错误。(dispatchEventWith)", e);
		}
    }

	/**
	 * 事件派发
	 * @param event 事件
	 */
	public void dispatchEvent(Event event)
	{
		try
		{
			if(events == null) return;
			String type = event.type;
            if (events.containsKey(type))
            {
            	LinkedBlockingQueue<EventHandler> eventListeners = events.get(type);
            	if (eventListeners != null)
				{
            		int l = eventListeners.size();
            		if(l == 0) return;
            		EventHandler[] list = new EventHandler[l];
					list = eventListeners.toArray(list);
					event.setCurrentTarget(this);
					
					for(EventHandler eventListener : list)
					{
						if(isDispose) break;
						//事件处理器已被销毁
	                    if (eventListener.getEventListener() == null)
	                    {
	                        continue;
	                    }
					    eventListener.getEventListener().handleEvent(event);
					    if (event.isStopImmediateEvent)
	                    {
	                        break;
	                    }
					}
				}
				// Object[] method=event.get(event.getType());
				// Class<? extends Object> classType=method[1].getClass();
				// invokeMethod(method[1], (String)method[0], event);
            }
		}
		catch (Exception e)
		{
			Logger.getLogger(EventDispatcher.class.getName()).log(Level.SEVERE, "事件派发时发生错误。", e);
		}
	}

	// public Object invokeMethod(Object owner, String methodName, IEvent event) throws Exception {
	// Class<? extends Object> ownerClass = owner.getClass();
	// Method method = ownerClass.getMethod(methodName,new Class[]{IEvent.class});
	// return method.invoke(owner, event);
	// }

	/**
	 * 增加事件监听
	 * @param type 事件类型
	 * @param eventListener 事件接收者
	 * @return
	 */
	public void addEventListener(String type, IEventListener eventListener)
	{
		addEventListener(type,eventListener,0);
	}
	
	/**
	 * 增加事件监听
	 * @param type 事件类型
	 * @param eventListener 事件接收者
	 * @param priority 事件处理优先级 值越大越先执行，后加入的后执行
	 * @return
	 */
	public void addEventListener(String type, IEventListener eventListener,int priority)
	{
		lock.writeLock().lock();
		try
		{
			if(events!=null)
			{
				EventHandler eh = getEventHandler(type,eventListener);
		        if(eh != null) return;//已经监听过此事件
				
				LinkedBlockingQueue<EventHandler> eventListeners = null;
				if(!events.containsKey(type))
				{
					eventListeners = new LinkedBlockingQueue<EventHandler>();
					events.put(type, eventListeners);
				}
				else
				{
					eventListeners = events.get(type);
				}
				eh = new EventHandler(eventListener,priority);
				addToEventHandlerList(eh,eventListeners);
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(EventDispatcher.class.getName()).log(Level.SEVERE, "事件注册时发生错误。", e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	
	/// <summary>
    /// 增加到列表并排序
    /// </summary>
    private void addToEventHandlerList(EventHandler addEH, LinkedBlockingQueue<EventHandler> eventHandlers)
    {
    	try
		{
	        int l = eventHandlers.size();
	        if (l == 0)
	        {
	        	eventHandlers.put(addEH);
	            return;
	        }
	        EventHandler[] list = new EventHandler[l];
	        list = eventHandlers.toArray(list);
	        EventHandler eh;
	        int addIndex = -1;
	        for (int i = 0; i < l; i++)
	        {
	            eh = list[i];
	            if (addEH.getPriority() > eh.getPriority())
	            {
	                addIndex = i;
	                break;
	            }
	        }
	        if (addIndex > -1)
	        {
	        	eventHandlers.clear();
	        	for(int i = 0;i<l;i++)
	        	{
	        		eh = list[i];
	        		if(i == addIndex)
	        		{
	        			eventHandlers.put(addEH);
	        		}
	        		eventHandlers.put(eh);
	        	}
	        }
	        else
	        {
	        	eventHandlers.put(addEH);
	        }
		}
    	catch (Exception e)
		{
			Logger.getLogger(EventDispatcher.class.getName()).log(Level.SEVERE, "增加到列表并排序 发生错误", e);
		}
    }
	
	/**
	 * 得到事件处理器对象
	 * @param type 事件类型
	 * @param eventListener 事件接收者
	 * @return
	 */
	 private EventHandler getEventHandler(String type, IEventListener eventListener){
         if (events.containsKey(type))
         {
        	 LinkedBlockingQueue<EventHandler> eventHandlers = events.get(type);
        	 EventHandler eh;
        	 for(Iterator<EventHandler> it = eventHandlers.iterator();it.hasNext();)
        	 {
        		 eh = it.next();
        		 if(eh.getEventListener() == eventListener)
        		 {
        			 return eh;
        		 }
        	 }
         }
         return null;
     }

	/**
	 * 是否存在指定的事件监听
	 * @param type 事件类型
	 * @param eventListener 事件接收者
	 * @return
	 */
	public boolean hasEventListener(String type, IEventListener eventListener)
	{
		if(events == null) return false;
		EventHandler eh = getEventHandler(type,eventListener);
        if(eh != null) return true;
		return false;
	}

	public void removeEventListener(String type, IEventListener eventListener)
	{
		try
		{
			if(events!=null)
			{
				EventHandler eh = getEventHandler(type,eventListener);
		        if(eh != null)
		        {
		        	 LinkedBlockingQueue<EventHandler> eventHandlers = events.get(type);
		        	 eventHandlers.remove(eh);
		        	 eh.dispose();
		        }
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(EventDispatcher.class.getName()).log(Level.SEVERE, "事件移除时发生错误。", e);
		}
	}

	public void dispose()
	{
		isDispose=true;
		if(events!=null)
		{
			LinkedBlockingQueue<EventHandler> eventHandlers;
			for(Iterator<LinkedBlockingQueue<EventHandler>> it = events.values().iterator();it.hasNext();)
			{
				eventHandlers = it.next();
				for(EventHandler eh : eventHandlers)
				{
					eh.dispose();
				}
				eventHandlers.clear();
			}
			events.clear();
			events = null;
		}
	}
	
	private static LinkedBlockingQueue<Event> _eventObjlist = new LinkedBlockingQueue<Event>();
    private static Event createEvent(String type, Object data)
    {
        Event e;
        if (_eventObjlist.size() > 0)
        {
            e = _eventObjlist.poll();
            e.reset(type, data);
        }
        else
        {
            e = new Event(type, data);
        }
        return e;
    }
}

final class EventHandler
{
    private IEventListener _eventListener;

	/**
	 * 得到事件接收者
	 */
    public IEventListener getEventListener()
    {
        return _eventListener;
    }
    private int _priority;
    public int getPriority()
    {
        return _priority;
    }
    public EventHandler(IEventListener eventListener, int priority)
    {
    	_eventListener = eventListener;
        _priority = priority;
    }
    public void dispose()
    {
    	_eventListener = null;
    }
}