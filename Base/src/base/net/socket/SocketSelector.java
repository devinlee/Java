package base.net.socket;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketSelector extends Thread
{
	private Selector selector;

	public Selector getSelector()
	{
		return selector;
	}

	/**
	 * 线程读写锁
	 */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * 服务是否可用
	 */
	private boolean isAvailable = false;

	/**
	 * 服务是否可用
	 */
	public boolean getIsAvailable()
	{
		lock.readLock().lock();
		try
		{
			return isAvailable;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	public SocketSelector()
	{
		try
		{
			selector = Selector.open();
		}
		catch (IOException e)
		{
			Logger.getLogger(SocketSelector.class.getName()).log(Level.SEVERE, "SocketSelector运行错误。", e);
		}
	}

	@Override
	public void run()
	{
		startSelector();
	}

	public void startSelector()
	{
		try
		{
			if (isAvailable)
				return;
			isAvailable = true;
			while (isAvailable)
			{
				if (selector.select() > 0)
				{
					Set<SelectionKey> keys = selector.selectedKeys();
					for (SelectionKey selectionKey : keys)
					{
						try
						{
							SocketSelectorItem socketSelectorItem = (SocketSelectorItem) selectionKey.attachment();
							if (socketSelectorItem != null)
							{
								socketSelectorItem.handle(selectionKey);
							}
						}
						catch (Exception e)
						{
							Logger.getLogger(SocketSelector.class.getName()).log(Level.SEVERE, "SocketSelector运行错误。", e);
						}
					}
					keys.clear();
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketSelector.class.getName()).log(Level.SEVERE, "SocketSelector运行错误。", e);
		}
	}
}