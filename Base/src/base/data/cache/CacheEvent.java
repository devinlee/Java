package base.data.cache;

import base.event.Event;

public class CacheEvent extends Event
{
	/**
	 * 行字段数据发生改变
	 */
	public final static String ROW_FIELD_DATA_CHANGE = "rowFieldDataChange";

	/**
	 * 通知表进行物理存储
	 */
	public final static String DATA_TO_SAVE = "toSave";

	public CacheEvent(String type)
	{
		super(type);
	}

	public CacheEvent(String type, Object data)
	{
		super(type, data);
	}
}
