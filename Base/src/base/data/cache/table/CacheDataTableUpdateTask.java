package base.data.cache.table;

import base.timer.TimerTask;

public class CacheDataTableUpdateTask extends TimerTask
{
	private ICacheDataTable cacheDataTable;

	public CacheDataTableUpdateTask(ICacheDataTable cacheDataTable)
	{
		this.cacheDataTable = cacheDataTable;
	}

	@Override
	public void run()
	{
		if (this.cacheDataTable != null)
		{
			this.cacheDataTable.updateToDataByChange();
		}
	}

	public synchronized void dispose()
	{
		cacheDataTable = null;
	}
}
